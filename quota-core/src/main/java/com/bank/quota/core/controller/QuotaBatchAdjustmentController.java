package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.quota.*;
import com.bank.quota.core.service.QuotaBatchAdjustmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 额度批量调整控制器
 * 
 * <p>提供银行级信贷额度管控平台的额度批量调整REST API接口。
 * 支持批量额度调整操作，适用于定期批量处理场景。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 * @see QuotaBatchAdjustmentService
 */
@Tag(name = "额度批量调整", description = "额度批量调整管理接口")
@RestController
@RequestMapping("/api/v1/quota-batch-adjustment")
@RequiredArgsConstructor
public class QuotaBatchAdjustmentController {
    
    private final QuotaBatchAdjustmentService quotaBatchAdjustmentService;
    
    /**
     * 发起批量调整
     * 
     * <p>发起一个新的批量额度调整任务。</p>
     * 
     * @param request 批量调整请求
     * @return 批量调整响应
     */
    @Operation(summary = "发起批量调整", description = "发起一个新的批量额度调整任务")
    @PostMapping
    public Result<QuotaBatchAdjustmentResponse> initiateBatchAdjustment(@RequestBody QuotaBatchAdjustmentRequest request) {
        QuotaBatchAdjustmentResponse response = quotaBatchAdjustmentService.initiateBatchAdjustment(request);
        return Result.success(response);
    }
    
    /**
     * 执行批量调整
     * 
     * <p>执行指定的批量调整任务。</p>
     * 
     * @param batchId 批量ID
     * @return 执行结果
     */
    @Operation(summary = "执行批量调整", description = "执行指定的批量调整任务")
    @PostMapping("/execute/{batchId}")
    public Result<Boolean> executeBatchAdjustment(
            @Parameter(description = "批量ID", required = true) @PathVariable Long batchId) {
        boolean result = quotaBatchAdjustmentService.executeBatchAdjustment(batchId);
        return Result.success(result);
    }
    
    /**
     * 查询批量调整
     * 
     * <p>根据ID查询批量调整信息。</p>
     * 
     * @param batchId 批量ID
     * @return 批量调整响应
     */
    @Operation(summary = "查询批量调整", description = "根据ID查询批量调整信息")
    @GetMapping("/{batchId}")
    public Result<QuotaBatchAdjustmentResponse> getBatchAdjustment(
            @Parameter(description = "批量ID", required = true) @PathVariable Long batchId) {
        QuotaBatchAdjustmentResponse response = quotaBatchAdjustmentService.getBatchAdjustment(batchId);
        return Result.success(response);
    }
    
    /**
     * 查询批量调整明细
     * 
     * <p>根据批量ID查询其明细信息。</p>
     * 
     * @param batchId 批量ID
     * @return 批量调整明细响应列表
     */
    @Operation(summary = "查询批量调整明细", description = "根据批量ID查询其明细信息")
    @GetMapping("/{batchId}/details")
    public Result<List<QuotaBatchAdjustmentDetailResponse>> getBatchAdjustmentDetails(
            @Parameter(description = "批量ID", required = true) @PathVariable Long batchId) {
        List<QuotaBatchAdjustmentDetailResponse> details = quotaBatchAdjustmentService.getBatchAdjustmentDetails(batchId);
        return Result.success(details);
    }
    
    /**
     * 查询批量调整列表
     * 
     * <p>根据条件查询批量调整列表。</p>
     * 
     * @param status 状态
     * @param executorId 执行者ID
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 批量调整响应列表
     */
    @Operation(summary = "查询批量调整列表", description = "根据条件查询批量调整列表")
    @GetMapping
    public Result<List<QuotaBatchAdjustmentResponse>> getBatchAdjustments(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "执行者ID") @RequestParam(required = false) String executorId,
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页面大小", required = true) @RequestParam(defaultValue = "10") Integer pageSize) {
        List<QuotaBatchAdjustmentResponse> responses = quotaBatchAdjustmentService.getBatchAdjustments(status, executorId, pageNum, pageSize);
        return Result.success(responses);
    }
    
    /**
     * 取消批量调整
     * 
     * <p>取消指定的批量调整任务（仅限待处理状态）。</p>
     * 
     * @param batchId 批量ID
     * @return 是否取消成功
     */
    @Operation(summary = "取消批量调整", description = "取消指定的批量调整任务（仅限待处理状态）")
    @PostMapping("/cancel/{batchId}")
    public Result<Boolean> cancelBatchAdjustment(
            @Parameter(description = "批量ID", required = true) @PathVariable Long batchId) {
        boolean result = quotaBatchAdjustmentService.cancelBatchAdjustment(batchId);
        return Result.success(result);
    }
    
    /**
     * 重试失败的批量调整
     * 
     * <p>重试指定批次中失败的调整项。</p>
     * 
     * @param batchId 批量ID
     * @return 是否重试成功
     */
    @Operation(summary = "重试失败的批量调整", description = "重试指定批次中失败的调整项")
    @PostMapping("/retry/{batchId}")
    public Result<Boolean> retryFailedAdjustments(
            @Parameter(description = "批量ID", required = true) @PathVariable Long batchId) {
        boolean result = quotaBatchAdjustmentService.retryFailedAdjustments(batchId);
        return Result.success(result);
    }
}