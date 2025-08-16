package org.retrade.main.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.retrade.main.model.entity.PlatformFeeTierEntity;
import org.retrade.main.model.entity.PlatformSettingEntity;
import org.retrade.main.repository.jpa.PlatformFeeTierRepository;
import org.retrade.main.repository.jpa.PlatformSettingRepository;
import org.retrade.main.service.PlatformSettingService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
}
