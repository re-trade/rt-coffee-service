package org.retrade.main.repository.redis.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.retrade.common.model.constant.QueryOperatorEnum;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.main.config.provider.VietQRConfig;
import org.retrade.main.model.dto.response.VietQrBankListResponse;
import org.retrade.main.model.entity.VietQrBankEntity;
import org.retrade.main.repository.redis.VietQrBankRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VietQrBankRepositoryImpl implements VietQrBankRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final VietQRConfig vietQRConfig;
    private final ObjectMapper objectMapper;
    private static final String BANK_CACHE_KEY = "vietqr:banks";

    @Override
    public List<VietQrBankEntity> getAll() {
        var restTemplate = new RestTemplate();
        Object raw = null;
        log.info("Checking for cached banks...");
        try {
           raw = redisTemplate.opsForValue().get(BANK_CACHE_KEY);
        } catch (Exception e) {
            redisTemplate.delete(BANK_CACHE_KEY);
        }
        if (raw != null) {
            try {
                String json = objectMapper.writeValueAsString(raw);
                return objectMapper.readValue(json,
                        new TypeReference<>() {});
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse cached banks", e);
            }
        }
        log.info("Fetching banks from VietQR API...");
        VietQrBankListResponse response = restTemplate.getForObject(vietQRConfig.getBanksUrl(), VietQrBankListResponse.class);

        if (response == null || !"00".equals(response.getCode())) {
            throw new RuntimeException("Failed to fetch banks: " + (response != null ? response.getDesc() : "No response"));
        }
        List<VietQrBankEntity> banks = response.getData();
        redisTemplate.opsForValue().set(BANK_CACHE_KEY, banks, Duration.ofHours(24));
        return banks;
    }

    @Override
    public Page<VietQrBankEntity> getAll(Pageable pageable) {
        List<VietQrBankEntity> allBanks = getAll();
        int size = pageable.getPageSize();
        return getVietQrBankEntities(pageable, allBanks, size);
    }

    @Override
    public Page<VietQrBankEntity> search(QueryWrapper queryWrapper) {
        var pageable = queryWrapper.pagination();
        var search = queryWrapper.search();
        List<VietQrBankEntity> allBanks = getAll();
        var id = search.remove("id");
        var name = search.remove("name");
        var bin = search.remove("bin");
        int size = pageable.getPageSize();
        List<VietQrBankEntity> filteredBanks = allBanks.stream()
                .filter(bank -> {
                    boolean matches = true;
                    if (id != null) {
                        var value = id.getValue();
                        if (value instanceof List<?> ids && id.getOperator() == QueryOperatorEnum.IN) {
                            matches = ids.stream().map(Object::toString).toList().contains(String.valueOf(bank.getId()));
                        } else {
                            matches = id.getValue().toString().equals(String.valueOf(bank.getId()));
                        }
                    }
                    if (name != null) {
                        matches = matches && bank.getName() != null && bank.getName().toLowerCase().contains(name.getValue().toString().toLowerCase());
                    }
                    if (bin != null) {
                        matches = matches && bin.getValue().toString().equals(bank.getBin());
                    }
                    return matches;
                })
                .toList();

        return getVietQrBankEntities(pageable, filteredBanks, size);
    }

    @Override
    public Optional<VietQrBankEntity> getBankByBin(String bin) {
        return getAll().stream().filter(bank -> bank.getBin().equals(bin)).findFirst();
    }

    @Override
    public Map<String, VietQrBankEntity> getBankMap() {
        return getAll().stream()
                .collect(Collectors.toMap(
                        VietQrBankEntity::getBin,
                        Function.identity()
                ));
    }

    @Override
    public Map<String, VietQrBankEntity> getBankMapInBin(Set<String> bins) {
        if (bins == null || bins.isEmpty()) {
            return Map.of();
        }
        var allBanks = getAll();
        Map<String, VietQrBankEntity> binMap = allBanks.stream()
                .collect(Collectors.toMap(
                        VietQrBankEntity::getBin,
                        Function.identity(),
                        (a, b) -> a
                ));
        return bins.stream()
                .filter(binMap::containsKey)
                .collect(Collectors.toMap(
                        Function.identity(),
                        binMap::get
                ));
    }

    @NotNull
    private Page<VietQrBankEntity> getVietQrBankEntities(Pageable pageable, List<VietQrBankEntity> allBanks, int size) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + size, allBanks.size());
        if (start >= allBanks.size()) {
            return Page.empty(pageable);
        }
        List<VietQrBankEntity> pageContent = allBanks.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allBanks.size());
    }

}
