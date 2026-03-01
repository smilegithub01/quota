package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.riskwarning.*;
import com.bank.quota.core.service.RiskWarningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/risk-warning")
@RequiredArgsConstructor
@Tag(name = "风险预警管理", description = "风险预警规则创建、查询、配置接口")
public class RiskWarningController {
    
    private final RiskWarningService riskWarningService;
    
    @PostMapping("/rules")
    @Operation(summary = "创建预警规则", description = "创建新的风险预警规则")
    public Result<RiskWarningRuleResponse> createWarningRule(
            @Valid @RequestBody CreateWarningRuleRequest request) {
        log.info("Received create warning rule request: ruleCode={}, ruleName={}", 
                request.getRuleCode(), request.getRuleName());
        
        RiskWarningRuleResponse response = riskWarningService.createWarningRule(request);
        return Result.success(response);
    }
    
    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "更新预警规则", description = "更新风险预警规则信息")
    public Result<RiskWarningRuleResponse> updateWarningRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody UpdateWarningRuleRequest request) {
        log.info("Received update warning rule request: ruleId={}", ruleId);
        
        RiskWarningRuleResponse response = riskWarningService.updateWarningRule(ruleId, request);
        return Result.success(response);
    }
    
    @PostMapping("/rules/{ruleId}/enable")
    @Operation(summary = "启用预警规则", description = "启用指定的风险预警规则")
    public Result<RiskWarningRuleResponse> enableWarningRule(
            @PathVariable Long ruleId,
            @RequestParam String operator) {
        log.info("Received enable warning rule request: ruleId={}", ruleId);
        
        RiskWarningRuleResponse response = riskWarningService.enableWarningRule(ruleId, operator);
        return Result.success(response);
    }
    
    @PostMapping("/rules/{ruleId}/disable")
    @Operation(summary = "停用预警规则", description = "停用指定的风险预警规则")
    public Result<RiskWarningRuleResponse> disableWarningRule(
            @PathVariable Long ruleId,
            @RequestParam String operator) {
        log.info("Received disable warning rule request: ruleId={}", ruleId);
        
        RiskWarningRuleResponse response = riskWarningService.disableWarningRule(ruleId, operator);
        return Result.success(response);
    }
    
    @GetMapping("/rules/{ruleId}")
    @Operation(summary = "查询预警规则", description = "根据ID查询预警规则详情")
    public Result<RiskWarningRuleResponse> getWarningRule(@PathVariable Long ruleId) {
        log.info("Received get warning rule request: ruleId={}", ruleId);
        
        RiskWarningRuleResponse response = riskWarningService.getWarningRule(ruleId);
        return Result.success(response);
    }
    
    @GetMapping("/rules")
    @Operation(summary = "查询所有预警规则", description = "查询所有预警规则列表")
    public Result<List<RiskWarningRuleResponse>> getAllWarningRules() {
        log.info("Received get all warning rules request");
        
        List<RiskWarningRuleResponse> responses = riskWarningService.getEnabledWarningRules();
        return Result.success(responses);
    }
    
    @GetMapping("/rules/type/{ruleType}")
    @Operation(summary = "按类型查询预警规则", description = "根据规则类型查询预警规则")
    public Result<List<RiskWarningRuleResponse>> getWarningRulesByType(
            @PathVariable String ruleType) {
        log.info("Received get warning rules by type request: ruleType={}", ruleType);
        
        List<RiskWarningRuleResponse> responses = riskWarningService.getWarningRulesByType(ruleType);
        return Result.success(responses);
    }
    
    @GetMapping("/rules/level/{warningLevel}")
    @Operation(summary = "按级别查询预警规则", description = "根据预警级别查询预警规则")
    public Result<List<RiskWarningRuleResponse>> getWarningRulesByLevel(
            @PathVariable String warningLevel) {
        log.info("Received get warning rules by level request: warningLevel={}", warningLevel);
        
        List<RiskWarningRuleResponse> responses = riskWarningService.getWarningRulesByLevel(warningLevel);
        return Result.success(responses);
    }
    
    @GetMapping("/rules/object-type/{objectType}")
    @Operation(summary = "按对象类型查询预警规则", description = "根据对象类型查询预警规则")
    public Result<List<RiskWarningRuleResponse>> getWarningRulesByObjectType(
            @PathVariable String objectType) {
        log.info("Received get warning rules by objectType request: objectType={}", objectType);
        
        List<RiskWarningRuleResponse> responses = riskWarningService.getEnabledWarningRulesByObjectType(objectType);
        return Result.success(responses);
    }
    
    @PostMapping("/check")
    @Operation(summary = "检查预警", description = "手动触发预警检查")
    public Result<Void> checkWarnings(
            @RequestParam String objectType,
            @RequestParam Long objectId) {
        log.info("Received check warnings request: objectType={}, objectId={}", objectType, objectId);
        
        riskWarningService.checkAndTriggerWarnings(objectType, objectId);
        return Result.success();
    }
}
