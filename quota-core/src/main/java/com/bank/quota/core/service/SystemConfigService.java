package com.bank.quota.core.service;

import com.bank.quota.core.domain.SystemConfig;
import com.bank.quota.core.dto.systemconfig.*;

import java.util.List;

public interface SystemConfigService {
    
    SystemConfigResponse createSystemConfig(CreateSystemConfigRequest request);
    
    SystemConfigResponse updateSystemConfig(Long id, UpdateSystemConfigRequest request);
    
    SystemConfigResponse getSystemConfig(Long id);
    
    SystemConfigResponse getSystemConfigByKey(String configKey);
    
    List<SystemConfigResponse> getSystemConfigsByCategory(String category);
    
    List<SystemConfigResponse> getAllSystemConfigs();
    
    void deleteSystemConfig(Long id);
}
