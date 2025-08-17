package org.retrade.main.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.response.PlatformFeeTierResponse;
import org.retrade.main.model.entity.PlatformFeeTierEntity;
import org.retrade.main.model.entity.PlatformSettingEntity;
import org.retrade.main.repository.jpa.PlatformFeeTierRepository;
import org.retrade.main.repository.jpa.PlatformSettingRepository;
import org.retrade.main.service.PlatformSettingService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlatformSettingServiceImpl implements PlatformSettingService {
    private final PlatformSettingRepository platformSettingRepository;
    private final PlatformFeeTierRepository platformFeeTierRepository;

    @Cacheable(value = "platformSettings", key = "#key")
    public PlatformSettingEntity getSetting(String key) {
        return platformSettingRepository.findByKey(key)
                .orElseThrow(() -> new EntityNotFoundException("Setting not found: " + key));
    }

    @Cacheable(value = "platformSettingsValue", key = "#key")
    @Override
    public String getStringValue(String key) {
        return getSetting(key).getValue();
    }

    @Cacheable(value = "platformSettingsDecimal", key = "#key")
    @Override
    public BigDecimal getDecimalValue(String key) {
        return new BigDecimal(getStringValue(key));
    }

    @Cacheable(value = "platformSettingsBoolean", key = "#key")
    @Override
    public boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(getStringValue(key));
    }

    @CacheEvict(value = { "platformSettings", "platformSettingsValue", "platformSettingsDecimal" }, allEntries = true)
    public PlatformSettingEntity saveOrUpdate(PlatformSettingEntity setting) {
        return platformSettingRepository.save(setting);
    }

    @CacheEvict(value = { "platformSettings", "platformSettingsValue", "platformSettingsDecimal" }, allEntries = true)
    public void delete(String id) {
        platformSettingRepository.deleteById(id);
    }

    public List<PlatformFeeTierEntity> getAll() {
        return platformFeeTierRepository.findAll();
    }

    public PlatformFeeTierEntity saveOrUpdate(PlatformFeeTierEntity tier) {
        return platformFeeTierRepository.save(tier);
    }

    public void deletePlatformFeeTier(String id) {
        platformFeeTierRepository.deleteById(id);
    }

    @Override
    public BigDecimal findFeeRate(BigDecimal grandPrice) {
        return platformFeeTierRepository.findMatchingFeeRate(grandPrice).orElse(BigDecimal.ZERO);
    }

    @Override
    public PaginationWrapper<List<PlatformFeeTierResponse>> getAllPlatformFeeTierConfig(QueryWrapper queryWrapper) {
        return platformFeeTierRepository.query(queryWrapper, (param) -> ((root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            return getPredicate(param, root, criteriaBuilder, predicates);
        }), (items) -> {
            var list = items.map(this::wrapPlatformFeeTierResponse).stream().toList();
            return new PaginationWrapper.Builder<List<PlatformFeeTierResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<PlatformFeeTierEntity> root, CriteriaBuilder criteriaBuilder, List<jakarta.persistence.criteria.Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            jakarta.persistence.criteria.Predicate[] defaultPredicates = platformFeeTierRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private PlatformFeeTierResponse wrapPlatformFeeTierResponse(PlatformFeeTierEntity platformFeeTierEntity) {
        return PlatformFeeTierResponse.builder()
                .id(platformFeeTierEntity.getId())
                .minPrice(platformFeeTierEntity.getMinPrice())
                .maxPrice(platformFeeTierEntity.getMaxPrice())
                .feeRate(platformFeeTierEntity.getFeeRate())
                .description(platformFeeTierEntity.getDescription())
                .build();
    }
}
