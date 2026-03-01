package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.core.domain.AuditLog;
import com.bank.quota.core.domain.SystemConfig;
import com.bank.quota.core.dto.systemconfig.*;
import com.bank.quota.core.repository.SystemConfigRepository;
import com.bank.quota.core.service.AuditLogService;
import com.bank.quota.core.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {
    
    private final SystemConfigRepository systemConfigRepository;
    private final AuditLogService auditLogService;
    
    @Override
    @Transactional
    public SystemConfigResponse createSystemConfig(CreateSystemConfigRequest request) {
        log.info("Creating system config: configKey={}, category={}", 
                request.getConfigKey(), request.getCategory());
        
        if (systemConfigRepository.existsByConfigKey(request.getConfigKey())) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                    "配置Key已存在: " + request.getConfigKey());
        }
        
        SystemConfig config = SystemConfig.builder()
                .configKey(request.getConfigKey())
                .configValue(request.getConfigValue())
                .configName(request.getConfigName())
                .category(request.getCategory())
                .description(request.getDescription())
                .status(SystemConfig.ConfigStatus.ENABLED)
                .createBy(request.getCreateBy())
                .build();
        
        SystemConfig saved = systemConfigRepository.save(config);
        
        auditLogService.logOperation(
                AuditLog.OperationType.SYSTEM_CONFIG_UPDATE,
                AuditLog.AuditObjectType.SYSTEM_CONFIG,
                saved.getId().toString(),
                "创建系统配置: " + saved.getConfigKey(),
                request.getCreateBy(),
                "SUCCESS");
        
        log.info("System config created successfully: id={}, configKey={}", saved.getId(), saved.getConfigKey());
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public SystemConfigResponse updateSystemConfig(Long id, UpdateSystemConfigRequest request) {
        log.info("Updating system config: id={}", id);
        
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "系统配置不存在: id=" + id));
        
        if (request.getConfigValue() != null) {
            config.setConfigValue(request.getConfigValue());
        }
        
        if (request.getConfigName() != null) {
            config.setConfigName(request.getConfigName());
        }
        
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        
        config.setUpdateBy(request.getUpdateBy());
        SystemConfig saved = systemConfigRepository.save(config);
        
        auditLogService.logOperation(
                AuditLog.OperationType.SYSTEM_CONFIG_UPDATE,
                AuditLog.AuditObjectType.SYSTEM_CONFIG,
                saved.getId().toString(),
                "更新系统配置: " + saved.getConfigKey(),
                request.getUpdateBy(),
                "SUCCESS");
        
        log.info("System config updated successfully: id={}", id);
        
        return buildResponse(saved);
    }
    
    @Override
    public SystemConfigResponse getSystemConfig(Long id) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "系统配置不存在: id=" + id));
        
        return buildResponse(config);
    }
    
    @Override
    public SystemConfigResponse getSystemConfigByKey(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "系统配置不存在: configKey=" + configKey));
        
        return buildResponse(config);
    }
    
    @Override
    public List<SystemConfigResponse> getSystemConfigsByCategory(String category) {
        return systemConfigRepository.findByCategory(category)
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SystemConfigResponse> getAllSystemConfigs() {
        return systemConfigRepository.findAll()
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteSystemConfig(Long id) {
        log.info("Deleting system config: id={}", id);
        
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "系统配置不存在: id=" + id));
        
        systemConfigRepository.delete(config);
        
        auditLogService.logOperation(
                AuditLog.OperationType.SYSTEM_CONFIG_UPDATE,
                AuditLog.AuditObjectType.SYSTEM_CONFIG,
                id.toString(),
                "删除系统配置: " + config.getConfigKey(),
                "SYSTEM",
                "SUCCESS");
        
        log.info("System config deleted successfully: id={}", id);
    }
    
    private SystemConfigResponse buildResponse(SystemConfig config) {
        SystemConfigResponse response = new SystemConfigResponse();
        response.setId(config.getId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigValue(config.getConfigValue());
        response.setConfigName(config.getConfigName());
        response.setCategory(config.getCategory());
        response.setDescription(config.getDescription());
        response.setStatus(config.getStatus() != null ? config.getStatus().name() : null);
        response.setCreateBy(config.getCreateBy());
        response.setUpdateBy(config.getUpdateBy());
        response.setCreateTime(config.getCreateTime());
        response.setUpdateTime(config.getUpdateTime());
        return response;
    }
}
