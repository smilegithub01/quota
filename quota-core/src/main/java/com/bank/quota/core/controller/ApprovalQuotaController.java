package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.approvalquota.*;
import com.bank.quota.core.service.ApprovalQuotaService;
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
@RequestMapping("/api/v1/approval-quota")
@RequiredArgsConstructor
@Tag(name = "批复额度管理", description = "批复额度创建、查询、调整接口")
public class ApprovalQuotaController {
    
    private final ApprovalQuotaService approvalQuotaService;
    
    @PostMapping
    @Operation(summary = "创建批复额度", description = "创建新的批复额度记录")
    public Result<ApprovalQuotaResponse> createApprovalQuota(
            @Valid @RequestBody CreateApprovalQuotaRequest request) {
        log.info("Received create approval quota request: approvalNo={}, approvalType={}", 
                request.getApprovalNo(), request.getApprovalType());
        
        ApprovalQuotaResponse response = approvalQuotaService.createApprovalQuota(request);
        return Result.success(response);
    }
    
    @PutMapping("/{approvalId}")
    @Operation(summary = "更新批复额度", description = "更新批复额度信息")
    public Result<ApprovalQuotaResponse> updateApprovalQuota(
            @PathVariable Long approvalId,
            @Valid @RequestBody UpdateApprovalQuotaRequest request) {
        log.info("Received update approval quota request: approvalId={}", approvalId);
        
        ApprovalQuotaResponse response = approvalQuotaService.updateApprovalQuota(approvalId, request);
        return Result.success(response);
    }
    
    @PostMapping("/{approvalId}/freeze")
    @Operation(summary = "冻结批复额度", description = "冻结指定的批复额度")
    public Result<ApprovalQuotaResponse> freezeApprovalQuota(
            @PathVariable Long approvalId,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.info("Received freeze approval quota request: approvalId={}, reason={}", approvalId, reason);
        
        ApprovalQuotaResponse response = approvalQuotaService.freezeApprovalQuota(approvalId, reason, operator);
        return Result.success(response);
    }
    
    @PostMapping("/{approvalId}/unfreeze")
    @Operation(summary = "解冻批复额度", description = "解冻指定的批复额度")
    public Result<ApprovalQuotaResponse> unfreezeApprovalQuota(
            @PathVariable Long approvalId,
            @RequestParam String operator) {
        log.info("Received unfreeze approval quota request: approvalId={}", approvalId);
        
        ApprovalQuotaResponse response = approvalQuotaService.unfreezeApprovalQuota(approvalId, operator);
        return Result.success(response);
    }
    
    @PostMapping("/{approvalId}/disable")
    @Operation(summary = "停用批复额度", description = "停用指定的批复额度")
    public Result<ApprovalQuotaResponse> disableApprovalQuota(
            @PathVariable Long approvalId,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.info("Received disable approval quota request: approvalId={}, reason={}", approvalId, reason);
        
        ApprovalQuotaResponse response = approvalQuotaService.disableApprovalQuota(approvalId, reason, operator);
        return Result.success(response);
    }
    
    @GetMapping("/{approvalId}")
    @Operation(summary = "查询批复额度", description = "根据ID查询批复额度详情")
    public Result<ApprovalQuotaResponse> getApprovalQuota(@PathVariable Long approvalId) {
        log.info("Received get approval quota request: approvalId={}", approvalId);
        
        ApprovalQuotaResponse response = approvalQuotaService.getApprovalQuota(approvalId);
        return Result.success(response);
    }
    
    @GetMapping("/no/{approvalNo}")
    @Operation(summary = "根据批复编号查询", description = "根据批复编号查询批复额度详情")
    public Result<ApprovalQuotaResponse> getApprovalQuotaByNo(@PathVariable String approvalNo) {
        log.info("Received get approval quota by no request: approvalNo={}", approvalNo);
        
        ApprovalQuotaResponse response = approvalQuotaService.getApprovalQuotaByNo(approvalNo);
        return Result.success(response);
    }
    
    @GetMapping("/customer-quota-sub/{customerQuotaSubId}")
    @Operation(summary = "查询客户细项下批复额度", description = "根据客户细项额度ID查询所有批复额度")
    public Result<List<ApprovalQuotaResponse>> getApprovalQuotasByCustomerQuotaSubId(
            @PathVariable Long customerQuotaSubId) {
        log.info("Received get approval quotas by customerQuotaSubId request: customerQuotaSubId={}", 
                customerQuotaSubId);
        
        List<ApprovalQuotaResponse> responses = approvalQuotaService
                .getApprovalQuotasByCustomerQuotaSubId(customerQuotaSubId);
        return Result.success(responses);
    }
    
    @GetMapping("/enabled")
    @Operation(summary = "查询有效批复额度", description = "查询所有有效的批复额度列表")
    public Result<List<ApprovalQuotaResponse>> getEnabledApprovalQuotas() {
        log.info("Received get enabled approval quotas request");
        
        List<ApprovalQuotaResponse> responses = approvalQuotaService.getEnabledApprovalQuotas();
        return Result.success(responses);
    }
    
    @GetMapping("/type/{approvalType}")
    @Operation(summary = "按类型查询批复额度", description = "根据批复类型查询批复额度")
    public Result<List<ApprovalQuotaResponse>> getApprovalQuotasByType(
            @PathVariable String approvalType) {
        log.info("Received get approval quotas by type request: approvalType={}", approvalType);
        
        List<ApprovalQuotaResponse> responses = approvalQuotaService.getApprovalQuotasByType(approvalType);
        return Result.success(responses);
    }
    
    @GetMapping("/{approvalId}/usage")
    @Operation(summary = "查询批复额度使用量", description = "查询批复额度的使用情况")
    public Result<ApprovalQuotaUsageResponse> getApprovalQuotaUsage(@PathVariable Long approvalId) {
        log.info("Received get approval quota usage request: approvalId={}", approvalId);
        
        ApprovalQuotaUsageResponse response = approvalQuotaService.getApprovalQuotaUsage(approvalId);
        return Result.success(response);
    }
    
    @PostMapping("/{approvalId}/adjust")
    @Operation(summary = "调整批复额度", description = "调整批复额度的金额")
    public Result<Void> adjustApprovalQuota(
            @PathVariable Long approvalId,
            @RequestParam BigDecimal adjustmentAmount,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.info("Received adjust approval quota request: approvalId={}, adjustmentAmount={}", 
                approvalId, adjustmentAmount);
        
        approvalQuotaService.adjustApprovalQuota(approvalId, adjustmentAmount, reason, operator);
        return Result.success();
    }
    
    @PostMapping("/{approvalId}/withdraw")
    @Operation(summary = "撤回批复额度", description = "撤回已使用的批复额度")
    public Result<Void> withdrawApprovalQuota(
            @PathVariable Long approvalId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.info("Received withdraw approval quota request: approvalId={}, amount={}", 
                approvalId, amount);
        
        approvalQuotaService.withdrawApprovalQuota(approvalId, amount, reason, operator);
        return Result.success();
    }
}
