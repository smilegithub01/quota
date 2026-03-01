package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.domain.CustomerQuota;
import com.bank.quota.core.dto.customerquota.*;
import com.bank.quota.core.enums.CustomerType;
import com.bank.quota.core.service.CustomerQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/customer-quota")
@RequiredArgsConstructor
@Tag(name = "客户额度管理", description = "客户额度创建、查询、调整接口")
public class CustomerQuotaController {
    
    private final CustomerQuotaService customerQuotaService;
    
    @PostMapping
    @Operation(summary = "创建客户额度", description = "创建新的客户额度记录")
    public Result<CustomerQuotaResponse> createCustomerQuota(
            @Valid @RequestBody CreateCustomerQuotaRequest request) {
        log.info("Received create customer quota request: customerId={}, customerName={}", 
                request.getCustomerId(), request.getCustomerName());
        
        CustomerQuotaResponse response = customerQuotaService.createCustomerQuota(request);
        return Result.success(response);
    }
    
    @PutMapping("/{customerId}")
    @Operation(summary = "更新客户额度", description = "更新客户额度信息")
    public Result<CustomerQuotaResponse> updateCustomerQuota(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerQuotaRequest request) {
        log.info("Received update customer quota request: customerId={}", customerId);
        
        CustomerQuotaResponse response = customerQuotaService.updateCustomerQuota(customerId, request);
        return Result.success(response);
    }
    
    @PostMapping("/{customerId}/freeze")
    @Operation(summary = "冻结客户额度", description = "冻结指定的客户额度")
    public Result<CustomerQuotaResponse> freezeCustomerQuota(
            @PathVariable Long customerId,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.info("Received freeze customer quota request: customerId={}, reason={}", customerId, reason);
        
        CustomerQuotaResponse response = customerQuotaService.freezeCustomerQuota(customerId, reason, operator);
        return Result.success(response);
    }
    
    @PostMapping("/{customerId}/unfreeze")
    @Operation(summary = "解冻客户额度", description = "解冻指定的客户额度")
    public Result<CustomerQuotaResponse> unfreezeCustomerQuota(
            @PathVariable Long customerId,
            @RequestParam String operator) {
        log.info("Received unfreeze customer quota request: customerId={}", customerId);
        
        CustomerQuotaResponse response = customerQuotaService.unfreezeCustomerQuota(customerId, operator);
        return Result.success(response);
    }
    
    @PostMapping("/{customerId}/disable")
    @Operation(summary = "停用客户额度", description = "停用指定的客户额度")
    public Result<CustomerQuotaResponse> disableCustomerQuota(
            @PathVariable Long customerId,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.info("Received disable customer quota request: customerId={}, reason={}", customerId, reason);
        
        CustomerQuotaResponse response = customerQuotaService.disableCustomerQuota(customerId, reason, operator);
        return Result.success(response);
    }
    
    @GetMapping("/{customerId}")
    @Operation(summary = "查询客户额度", description = "根据客户ID查询客户额度详情")
    public Result<CustomerQuotaResponse> getCustomerQuota(@PathVariable Long customerId) {
        log.info("Received get customer quota request: customerId={}", customerId);
        
        CustomerQuotaResponse response = customerQuotaService.getCustomerQuota(customerId);
        return Result.success(response);
    }
    
    @GetMapping("/group/{groupId}")
    @Operation(summary = "查询集团下客户额度", description = "根据集团ID查询所有客户额度")
    public Result<List<CustomerQuotaResponse>> getCustomerQuotasByGroupId(@PathVariable Long groupId) {
        log.info("Received get customer quotas by groupId request: groupId={}", groupId);
        
        List<CustomerQuotaResponse> responses = customerQuotaService.getCustomerQuotasByGroupId(groupId);
        return Result.success(responses);
    }
    
    @GetMapping("/enabled")
    @Operation(summary = "查询有效客户额度", description = "查询所有有效的客户额度列表")
    public Result<List<CustomerQuotaResponse>> getEnabledCustomerQuotas() {
        log.info("Received get enabled customer quotas request");
        
        List<CustomerQuotaResponse> responses = customerQuotaService.getEnabledCustomerQuotas();
        return Result.success(responses);
    }
    
    @GetMapping("/type/{customerType}")
    @Operation(summary = "按类型查询客户额度", description = "根据客户类型查询客户额度")
    public Result<List<CustomerQuotaResponse>> getCustomerQuotasByType(
            @PathVariable String customerType) {
        log.info("Received get customer quotas by type request: customerType={}", customerType);
        
        CustomerType type = CustomerType.valueOf(customerType.toUpperCase());
        List<CustomerQuotaResponse> responses = customerQuotaService.getCustomerQuotasByType(type);
        return Result.success(responses);
    }
    
    @GetMapping("/{customerId}/usage")
    @Operation(summary = "查询客户额度使用量", description = "查询客户额度的使用情况")
    public Result<CustomerQuotaUsageResponse> getCustomerQuotaUsage(@PathVariable Long customerId) {
        log.info("Received get customer quota usage request: customerId={}", customerId);
        
        CustomerQuotaUsageResponse response = customerQuotaService.getCustomerQuotaUsage(customerId);
        return Result.success(response);
    }
    
    @PostMapping("/{customerId}/adjust")
    @Operation(summary = "调整客户额度", description = "调整客户额度的金额")
    public Result<Void> adjustCustomerQuota(
            @PathVariable Long customerId,
            @RequestParam BigDecimal adjustmentAmount,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.info("Received adjust customer quota request: customerId={}, adjustmentAmount={}", 
                customerId, adjustmentAmount);
        
        customerQuotaService.adjustCustomerQuota(customerId, adjustmentAmount, reason, operator);
        return Result.success();
    }
    
    @PostMapping("/transfer")
    @Operation(summary = "额度转移", description = "在客户之间转移额度")
    public Result<Void> transferQuota(
            @RequestParam Long fromCustomerId,
            @RequestParam Long toCustomerId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.info("Received transfer quota request: fromCustomerId={}, toCustomerId={}, amount={}", 
                fromCustomerId, toCustomerId, amount);
        
        customerQuotaService.transferQuota(fromCustomerId, toCustomerId, amount, reason, operator);
        return Result.success();
    }
}
