package com.bank.quota.core.repository;

import com.bank.quota.core.domain.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    boolean existsByConfigKey(String configKey);
    
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    List<SystemConfig> findByCategory(String category);
    
    List<SystemConfig> findByStatus(SystemConfig.ConfigStatus status);
    
    List<SystemConfig> findByCategoryAndStatus(String category, SystemConfig.ConfigStatus status);
}
