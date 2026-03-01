package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.domain.SystemConfig;
import com.bank.quota.core.dto.systemconfig.*;
import com.bank.quota.core.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Tag(name = "系统管理", description = "系统配置管理接口")
public class SystemConfigController {
    
    private final SystemConfigService systemConfigService;
    
    @PostMapping("/configs")
    @Operation(summary = "创建系统配置", description = "创建新的系统配置项")
    public Result<SystemConfigResponse> createSystemConfig(
            @Valid @RequestBody CreateSystemConfigRequest request) {
        log.info("Received create system config request: configKey={}", request.getConfigKey());
        
        SystemConfigResponse response = systemConfigService.createSystemConfig(request);
        return Result.success(response);
    }
    
    @PutMapping("/configs/{id}")
    @Operation(summary = "更新系统配置", description = "更新系统配置项")
    public Result<SystemConfigResponse> updateSystemConfig(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        log.info("Received update system config request: id={}", id);
        
        SystemConfigResponse response = systemConfigService.updateSystemConfig(id, request);
        return Result.success(response);
    }
    
    @GetMapping("/configs/{id}")
    @Operation(summary = "查询系统配置", description = "根据ID查询系统配置")
    public Result<SystemConfigResponse> getSystemConfig(@PathVariable Long id) {
        log.info("Received get system config request: id={}", id);
        
        SystemConfigResponse response = systemConfigService.getSystemConfig(id);
        return Result.success(response);
    }
    
    @GetMapping("/configs/key/{configKey}")
    @Operation(summary = "根据Key查询配置", description = "根据配置Key查询系统配置")
    public Result<SystemConfigResponse> getSystemConfigByKey(@PathVariable String configKey) {
        log.info("Received get system config by key request: configKey={}", configKey);
        
        SystemConfigResponse response = systemConfigService.getSystemConfigByKey(configKey);
        return Result.success(response);
    }
    
    @GetMapping("/configs/category/{category}")
    @Operation(summary = "按分类查询配置", description = "根据分类查询系统配置列表")
    public Result<List<SystemConfigResponse>> getSystemConfigsByCategory(@PathVariable String category) {
        log.info("Received get system configs by category request: category={}", category);
        
        List<SystemConfigResponse> responses = systemConfigService.getSystemConfigsByCategory(category);
        return Result.success(responses);
    }
    
    @GetMapping("/configs")
    @Operation(summary = "查询所有配置", description = "查询所有系统配置")
    public Result<List<SystemConfigResponse>> getAllSystemConfigs() {
        log.info("Received get all system configs request");
        
        List<SystemConfigResponse> responses = systemConfigService.getAllSystemConfigs();
        return Result.success(responses);
    }
    
    @DeleteMapping("/configs/{id}")
    @Operation(summary = "删除系统配置", description = "删除系统配置项")
    public Result<Void> deleteSystemConfig(@PathVariable Long id) {
        log.info("Received delete system config request: id={}", id);
        
        systemConfigService.deleteSystemConfig(id);
        return Result.success();
    }
}
