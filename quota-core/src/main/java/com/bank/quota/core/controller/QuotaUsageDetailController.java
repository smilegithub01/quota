package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.quota.*;
import com.bank.quota.core.service.QuotaUsageDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 额度使用明细控制器
 * 
 * <p>提供银行级信贷额度管控平台的额度使用明细REST API接口。
 * 包括额度使用明细的查询、统计等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 * @see QuotaUsageDetailService
 */
@Tag(name = "额度使用明细", description = "额度使用明细查询、统计接口")
@RestController
@RequestMapping("/api/v1/quota-usage-detail")
@RequiredArgsConstructor
public class QuotaUsageDetailController {
    
    private final QuotaUsageDetailService quotaUsageDetailService;
    
    /**
     * 查询额度使用明细
     * 
     * <p>根据查询条件查询额度使用明细列表。</p>
     * 
     * @param request 额度使用明细查询请求
     * @return 额度使用明细响应列表
     */
    @Operation(summary = "查询额度使用明细", description = "根据查询条件查询额度使用明细列表")
    @PostMapping("/query")
    public Result<List<QuotaUsageDetailResponse>> queryUsageDetails(@RequestBody QuotaUsageDetailQueryRequest request) {
        List<QuotaUsageDetailResponse> responses = quotaUsageDetailService.queryUsageDetailsWithPaging(request);
        return Result.success(responses);
    }
    
    /**
     * 查询额度使用明细总数
     * 
     * <p>根据查询条件获取符合条件的额度使用明细总数。</p>
     * 
     * @param request 额度使用明细查询请求
     * @return 符合条件的明细总数
     */
    @Operation(summary = "查询额度使用明细总数", description = "根据查询条件获取符合条件的额度使用明细总数")
    @PostMapping("/count")
    public Result<Long> getTotalUsageCount(@RequestBody QuotaUsageDetailQueryRequest request) {
        Long count = quotaUsageDetailService.getTotalUsageCount(request);
        return Result.success(count);
    }
    
    /**
     * 查询额度使用统计
     * 
     * <p>根据查询条件查询额度使用统计数据。</p>
     * 
     * @param request 额度使用明细查询请求
     * @return 额度使用统计响应
     */
    @Operation(summary = "查询额度使用统计", description = "根据查询条件查询额度使用统计数据")
    @PostMapping("/statistics")
    public Result<QuotaUsageStatisticsResponse> getUsageStatistics(@RequestBody QuotaUsageDetailQueryRequest request) {
        QuotaUsageStatisticsResponse response = quotaUsageDetailService.getUsageStatistics(request);
        return Result.success(response);
    }
    
    /**
     * 根据客户ID查询额度使用明细
     * 
     * <p>根据客户ID查询其额度使用明细。</p>
     * 
     * @param customerId 客户ID
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 额度使用明细响应列表
     */
    @Operation(summary = "根据客户ID查询额度使用明细", description = "根据客户ID查询其额度使用明细")
    @GetMapping("/customer/{customerId}")
    public Result<List<QuotaUsageDetailResponse>> getUsageDetailsByCustomer(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId,
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页面大小", required = true) @RequestParam(defaultValue = "10") Integer pageSize) {
        List<QuotaUsageDetailResponse> responses = quotaUsageDetailService.getUsageDetailsByCustomer(customerId, pageNum, pageSize);
        return Result.success(responses);
    }
    
    /**
     * 根据集团ID查询额度使用明细
     * 
     * <p>根据集团ID查询其额度使用明细。</p>
     * 
     * @param groupId 集团ID
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 额度使用明细响应列表
     */
    @Operation(summary = "根据集团ID查询额度使用明细", description = "根据集团ID查询其额度使用明细")
    @GetMapping("/group/{groupId}")
    public Result<List<QuotaUsageDetailResponse>> getUsageDetailsByGroup(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页面大小", required = true) @RequestParam(defaultValue = "10") Integer pageSize) {
        List<QuotaUsageDetailResponse> responses = quotaUsageDetailService.getUsageDetailsByGroup(groupId, pageNum, pageSize);
        return Result.success(responses);
    }
    
    /**
     * 根据关联ID查询额度使用明细
     * 
     * <p>根据关联ID查询其对应的额度使用明细。</p>
     * 
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @return 额度使用明细响应列表
     */
    @Operation(summary = "根据关联ID查询额度使用明细", description = "根据关联ID查询其对应的额度使用明细")
    @GetMapping("/related")
    public Result<List<QuotaUsageDetailResponse>> getUsageDetailsByRelatedId(
            @Parameter(description = "关联ID", required = true) @RequestParam String relatedId,
            @Parameter(description = "关联类型", required = true) @RequestParam String relatedType) {
        List<QuotaUsageDetailResponse> responses = quotaUsageDetailService.getUsageDetailsByRelatedId(relatedId, relatedType);
        return Result.success(responses);
    }
}