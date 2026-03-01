package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.quotaquery.QuotaQueryRequest;
import com.bank.quota.core.dto.quotaquery.QuotaQueryResponse;
import com.bank.quota.core.service.QuotaQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 额度查询控制器
 * 
 * <p>提供集团额度、客户额度、批复额度等多层级查询接口。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Tag(name = "额度查询管理", description = "集团额度、客户额度、批复额度等查询接口")
@RestController
@RequestMapping("/api/v1/quota/query")
@RequiredArgsConstructor
public class QuotaQueryController {
    
    private final QuotaQueryService quotaQueryService;
    
    /**
     * 查询集团额度
     */
    @Operation(summary = "查询集团额度")
    @GetMapping("/group/{groupId}")
    public Result<QuotaQueryResponse> queryGroupQuota(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId) {
        QuotaQueryResponse response = quotaQueryService.queryGroupQuota(groupId);
        return Result.success(response);
    }
    
    /**
     * 查询客户额度
     */
    @Operation(summary = "查询客户额度")
    @GetMapping("/customer/{customerId}")
    public Result<QuotaQueryResponse> queryCustomerQuota(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId) {
        QuotaQueryResponse response = quotaQueryService.queryCustomerQuota(customerId);
        return Result.success(response);
    }
    
    /**
     * 查询批复额度
     */
    @Operation(summary = "查询批复额度")
    @GetMapping("/approval/{approvalId}")
    public Result<QuotaQueryResponse> queryApprovalQuota(
            @Parameter(description = "批复额度ID", required = true) @PathVariable Long approvalId) {
        QuotaQueryResponse response = quotaQueryService.queryApprovalQuota(approvalId);
        return Result.success(response);
    }
    
    /**
     * 查询细项额度
     */
    @Operation(summary = "查询细项额度")
    @GetMapping("/item/{parentId}/{parentType}")
    public Result<List<QuotaQueryResponse>> queryItemQuotas(
            @Parameter(description = "父级ID", required = true) @PathVariable Long parentId,
            @Parameter(description = "父级类型", required = true) @PathVariable String parentType) {
        List<QuotaQueryResponse> responses = quotaQueryService.queryItemQuotas(parentId, parentType);
        return Result.success(responses);
    }
    
    /**
     * 查询合同占用
     */
    @Operation(summary = "查询合同占用")
    @GetMapping("/contract/{quotaId}")
    public Result<List<QuotaQueryResponse>> queryContractOccupancy(
            @Parameter(description = "额度ID", required = true) @PathVariable Long quotaId) {
        List<QuotaQueryResponse> responses = quotaQueryService.queryContractOccupancy(quotaId);
        return Result.success(responses);
    }
    
    /**
     * 综合额度查询
     */
    @Operation(summary = "综合额度查询")
    @PostMapping("/search")
    public Result<QuotaQueryResponse> queryQuota(@Valid @RequestBody QuotaQueryRequest request) {
        QuotaQueryResponse response = quotaQueryService.queryQuota(request);
        return Result.success(response);
    }
    
    /**
     * 批量查询额度
     */
    @Operation(summary = "批量查询额度")
    @PostMapping("/batch")
    public Result<List<QuotaQueryResponse>> batchQueryQuotas(
            @RequestBody List<Long> quotaIds,
            @RequestParam String quotaType) {
        List<QuotaQueryResponse> responses = quotaQueryService.batchQueryQuotas(quotaIds, quotaType);
        return Result.success(responses);
    }
    
    /**
     * 额度使用率统计
     */
    @Operation(summary = "额度使用率统计")
    @PostMapping("/stats/usage-rate")
    public Result<List<QuotaQueryResponse>> getUsageRateStats(@Valid @RequestBody QuotaQueryRequest request) {
        List<QuotaQueryResponse> stats = quotaQueryService.getUsageRateStats(request);
        return Result.success(stats);
    }
    
    /**
     * 额度分布统计
     */
    @Operation(summary = "额度分布统计")
    @PostMapping("/stats/distribution")
    public Result<List<QuotaQueryResponse>> getDistributionStats(@Valid @RequestBody QuotaQueryRequest request) {
        List<QuotaQueryResponse> distribution = quotaQueryService.getDistributionStats(request);
        return Result.success(distribution);
    }
    
    /**
     * 额度趋势统计
     */
    @Operation(summary = "额度趋势统计")
    @PostMapping("/stats/trend")
    public Result<List<QuotaQueryResponse>> getTrendStats(@Valid @RequestBody QuotaQueryRequest request) {
        List<QuotaQueryResponse> trend = quotaQueryService.getTrendStats(request);
        return Result.success(trend);
    }
}