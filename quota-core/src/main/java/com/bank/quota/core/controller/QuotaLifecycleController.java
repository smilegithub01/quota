package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.domain.QuotaAdjustment;
import com.bank.quota.core.domain.QuotaFreeze;
import com.bank.quota.core.domain.QuotaLifecycleEvent;
import com.bank.quota.core.dto.lifecycle.*;
import com.bank.quota.core.service.QuotaLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "额度生命周期管理接口")
@RestController
@RequestMapping("/api/v1/quota/lifecycle")
@RequiredArgsConstructor
@Validated
public class QuotaLifecycleController {
    
    private final QuotaLifecycleService quotaLifecycleService;
    
    @Operation(summary = "创建额度")
    @PostMapping("/create")
    public Result<QuotaLifecycleResponse> createQuota(@Valid @RequestBody QuotaCreateRequest request) {
        QuotaLifecycleResponse response = quotaLifecycleService.createQuota(request);
        return Result.success(response);
    }
    
    @Operation(summary = "调整额度")
    @PostMapping("/adjust")
    public Result<QuotaLifecycleResponse> adjustQuota(@Valid @RequestBody QuotaAdjustRequest request) {
        QuotaLifecycleResponse response = quotaLifecycleService.adjustQuota(request);
        return Result.success(response);
    }
    
    @Operation(summary = "审批额度调整")
    @PostMapping("/adjust/{adjustmentNo}/approve")
    public Result<QuotaLifecycleResponse> approveAdjustment(
            @Parameter(description = "调整编号") @PathVariable String adjustmentNo,
            @Parameter(description = "是否通过") @RequestParam boolean approved,
            @Parameter(description = "审批备注") @RequestParam(required = false) String approveRemark,
            @Parameter(description = "审批人") @RequestParam String approver) {
        QuotaLifecycleResponse response = quotaLifecycleService.approveAdjustment(
            adjustmentNo, approved, approveRemark, approver);
        return Result.success(response);
    }
    
    @Operation(summary = "冻结额度")
    @PostMapping("/freeze")
    public Result<QuotaLifecycleResponse> freezeQuota(@Valid @RequestBody QuotaFreezeRequest request) {
        QuotaLifecycleResponse response = quotaLifecycleService.freezeQuota(request);
        return Result.success(response);
    }
    
    @Operation(summary = "解冻额度")
    @PostMapping("/unfreeze")
    public Result<QuotaLifecycleResponse> unfreezeQuota(@Valid @RequestBody QuotaUnfreezeRequest request) {
        QuotaLifecycleResponse response = quotaLifecycleService.unfreezeQuota(request);
        return Result.success(response);
    }
    
    @Operation(summary = "终止额度")
    @PostMapping("/terminate")
    public Result<QuotaLifecycleResponse> terminateQuota(@Valid @RequestBody QuotaTerminateRequest request) {
        QuotaLifecycleResponse response = quotaLifecycleService.terminateQuota(request);
        return Result.success(response);
    }
    
    @Operation(summary = "查询额度生命周期")
    @GetMapping("/{quotaId}")
    public Result<QuotaLifecycleResponse> getQuotaLifecycle(
            @Parameter(description = "额度ID") @PathVariable Long quotaId,
            @Parameter(description = "额度类型") @RequestParam String quotaType) {
        QuotaLifecycleResponse response = quotaLifecycleService.getQuotaLifecycle(quotaId, quotaType);
        return Result.success(response);
    }
    
    @Operation(summary = "查询额度生命周期事件")
    @GetMapping("/{quotaId}/events")
    public Result<List<QuotaLifecycleEvent>> getQuotaLifecycleEvents(
            @Parameter(description = "额度ID") @PathVariable Long quotaId) {
        List<QuotaLifecycleEvent> events = quotaLifecycleService.getQuotaLifecycleEvents(quotaId);
        return Result.success(events);
    }
    
    @Operation(summary = "查询额度调整历史")
    @GetMapping("/{quotaId}/adjustments")
    public Result<List<QuotaAdjustment>> getQuotaAdjustmentHistory(
            @Parameter(description = "额度ID") @PathVariable Long quotaId) {
        List<QuotaAdjustment> adjustments = quotaLifecycleService.getQuotaAdjustmentHistory(quotaId);
        return Result.success(adjustments);
    }
    
    @Operation(summary = "查询额度冻结历史")
    @GetMapping("/{quotaId}/freezes")
    public Result<List<QuotaFreeze>> getQuotaFreezeHistory(
            @Parameter(description = "额度ID") @PathVariable Long quotaId) {
        List<QuotaFreeze> freezes = quotaLifecycleService.getQuotaFreezeHistory(quotaId);
        return Result.success(freezes);
    }
    
    @Operation(summary = "查询有效冻结记录")
    @GetMapping("/{quotaId}/active-freezes")
    public Result<List<QuotaFreeze>> getActiveFreezes(
            @Parameter(description = "额度ID") @PathVariable Long quotaId) {
        List<QuotaFreeze> freezes = quotaLifecycleService.getActiveFreezes(quotaId);
        return Result.success(freezes);
    }
}
