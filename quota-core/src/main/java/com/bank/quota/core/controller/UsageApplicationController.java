package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.usage.*;
import com.bank.quota.core.service.UsageApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用信申请控制器
 * 
 * <p>提供银行级信贷额度管控平台的用信申请REST API接口。
 * 包括用信申请的创建、查询、审批等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 * @see UsageApplicationService
 */
@Tag(name = "用信申请管理", description = "用信申请创建、查询、审批接口")
@RestController
@RequestMapping("/api/v1/usage-application")
@RequiredArgsConstructor
public class UsageApplicationController {
    
    private final UsageApplicationService usageApplicationService;
    
    /**
     * 创建用信申请
     * 
     * <p>创建新的用信申请记录。</p>
     * 
     * @param request 用信申请请求
     * @return 用信申请响应
     */
    @Operation(summary = "创建用信申请", description = "创建新的用信申请记录")
    @PostMapping
    public Result<UsageApplicationResponse> createUsageApplication(@Valid @RequestBody UsageApplicationRequest request) {
        UsageApplicationResponse response = usageApplicationService.createUsageApplication(request);
        return Result.success(response);
    }
    
    /**
     * 查询用信申请
     * 
     * <p>根据申请ID查询用信申请详情。</p>
     * 
     * @param applicationId 申请ID
     * @return 用信申请响应
     */
    @Operation(summary = "查询用信申请", description = "根据申请ID查询用信申请详情")
    @GetMapping("/{applicationId}")
    public Result<UsageApplicationResponse> getUsageApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId) {
        UsageApplicationResponse response = usageApplicationService.getUsageApplication(applicationId);
        return Result.success(response);
    }
    
    /**
     * 查询客户用信申请列表
     * 
     * <p>查询指定客户的用信申请列表。</p>
     * 
     * @param customerId 客户ID
     * @return 用信申请响应列表
     */
    @Operation(summary = "查询客户用信申请列表", description = "查询指定客户的用信申请列表")
    @GetMapping("/customer/{customerId}")
    public Result<List<UsageApplicationResponse>> getApplicationsByCustomer(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId) {
        List<UsageApplicationResponse> responses = usageApplicationService.getApplicationsByCustomer(customerId);
        return Result.success(responses);
    }
    
    /**
     * 查询用信申请列表（按状态）
     * 
     * <p>根据状态查询用信申请列表。</p>
     * 
     * @param status 状态
     * @return 用信申请响应列表
     */
    @Operation(summary = "查询用信申请列表（按状态）", description = "根据状态查询用信申请列表")
    @GetMapping("/status/{status}")
    public Result<List<UsageApplicationResponse>> getApplicationsByStatus(
            @Parameter(description = "状态", required = true) @PathVariable String status) {
        List<UsageApplicationResponse> responses = usageApplicationService.getApplicationsByStatus(status);
        return Result.success(responses);
    }
    
    /**
     * 审批用信申请
     * 
     * <p>审批用信申请，更新申请状态。</p>
     * 
     * @param applicationId 申请ID
     * @param request 用信申请审批请求
     * @return 用信申请响应
     */
    @Operation(summary = "审批用信申请", description = "审批用信申请，更新申请状态")
    @PutMapping("/{applicationId}/approve")
    public Result<UsageApplicationResponse> approveUsageApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId,
            @Valid @RequestBody UsageApplicationApprovalRequest request) {
        request.setApplicationId(applicationId);
        UsageApplicationResponse response = usageApplicationService.approveUsageApplication(request);
        return Result.success(response);
    }
    
    /**
     * 更新用信申请
     * 
     * <p>更新用信申请信息。</p>
     * 
     * @param applicationId 申请ID
     * @param request 用信申请请求
     * @return 用信申请响应
     */
    @Operation(summary = "更新用信申请", description = "更新用信申请信息")
    @PutMapping("/{applicationId}")
    public Result<UsageApplicationResponse> updateUsageApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId,
            @Valid @RequestBody UsageApplicationRequest request) {
        UsageApplicationResponse response = usageApplicationService.updateUsageApplication(applicationId, request);
        return Result.success(response);
    }
    
    /**
     * 删除用信申请
     * 
     * <p>删除用信申请记录。</p>
     * 
     * @param applicationId 申请ID
     * @return 操作结果
     */
    @Operation(summary = "删除用信申请", description = "删除用信申请记录")
    @DeleteMapping("/{applicationId}")
    public Result<Void> deleteUsageApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId) {
        usageApplicationService.deleteUsageApplication(applicationId);
        return Result.success();
    }
    
    /**
     * 提交用信申请
     * 
     * <p>提交待审批的用信申请。</p>
     * 
     * @param applicationId 申请ID
     * @param submitter 提交人
     * @return 用信申请响应
     */
    @Operation(summary = "提交用信申请", description = "提交待审批的用信申请")
    @PostMapping("/{applicationId}/submit")
    public Result<UsageApplicationResponse> submitApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId,
            @Parameter(description = "提交人", required = true) @RequestParam String submitter) {
        UsageApplicationResponse response = usageApplicationService.submitApplication(applicationId, submitter);
        return Result.success(response);
    }
    
    /**
     * 查询待审批的用信申请列表
     * 
     * <p>查询所有待审批的用信申请列表。</p>
     * 
     * @return 用信申请响应列表
     */
    @Operation(summary = "查询待审批的用信申请列表", description = "查询所有待审批的用信申请列表")
    @GetMapping("/pending")
    public Result<List<UsageApplicationResponse>> getPendingApplications() {
        List<UsageApplicationResponse> responses = usageApplicationService.getPendingApplications();
        return Result.success(responses);
    }
    
    /**
     * 撤销用信申请
     * 
     * <p>撤销已提交但未审批的用信申请。</p>
     * 
     * @param applicationId 申请ID
     * @param reason 撤销原因
     * @return 用信申请响应
     */
    @Operation(summary = "撤销用信申请", description = "撤销已提交但未审批的用信申请")
    @PostMapping("/{applicationId}/cancel")
    public Result<UsageApplicationResponse> cancelApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId,
            @Parameter(description = "撤销原因", required = true) @RequestParam String reason) {
        UsageApplicationResponse response = usageApplicationService.cancelApplication(applicationId, reason);
        return Result.success(response);
    }
    
    /**
     * 核销用信申请
     * 
     * <p>核销已批准的用信申请，更新已用额度。</p>
     * 
     * @param applicationId 申请ID
     * @param usageAmount 核销金额
     * @param operator 操作人
     * @return 用信申请响应
     */
    @Operation(summary = "核销用信申请", description = "核销已批准的用信申请，更新已用额度")
    @PostMapping("/{applicationId}/write-off")
    public Result<UsageApplicationResponse> writeOffApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId,
            @Parameter(description = "核销金额", required = true) @RequestParam BigDecimal usageAmount,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        UsageApplicationResponse response = usageApplicationService.writeOffApplication(applicationId, usageAmount, operator);
        return Result.success(response);
    }
}