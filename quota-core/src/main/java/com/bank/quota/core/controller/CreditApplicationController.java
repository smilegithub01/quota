package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.credit.*;
import com.bank.quota.core.service.CreditApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 授信申请控制器
 * 
 * <p>提供银行级信贷额度管控平台的授信申请REST API接口。
 * 包括授信申请的创建、查询、审批等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 * @see CreditApplicationService
 */
@Tag(name = "授信申请管理", description = "授信申请创建、查询、审批接口")
@RestController
@RequestMapping("/api/v1/credit-application")
@RequiredArgsConstructor
public class CreditApplicationController {
    
    private final CreditApplicationService creditApplicationService;
    
    /**
     * 创建授信申请
     * 
     * <p>创建新的授信申请记录。</p>
     * 
     * @param request 授信申请请求
     * @return 授信申请响应
     */
    @Operation(summary = "创建授信申请", description = "创建新的授信申请记录")
    @PostMapping
    public Result<CreditApplicationResponse> createCreditApplication(@Valid @RequestBody CreditApplicationRequest request) {
        CreditApplicationResponse response = creditApplicationService.createCreditApplication(request);
        return Result.success(response);
    }
    
    /**
     * 查询授信申请
     * 
     * <p>根据申请ID查询授信申请详情。</p>
     * 
     * @param applicationId 申请ID
     * @return 授信申请响应
     */
    @Operation(summary = "查询授信申请", description = "根据申请ID查询授信申请详情")
    @GetMapping("/{applicationId}")
    public Result<CreditApplicationResponse> getCreditApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId) {
        CreditApplicationResponse response = creditApplicationService.getCreditApplication(applicationId);
        return Result.success(response);
    }
    
    /**
     * 查询客户授信申请列表
     * 
     * <p>查询指定客户的授信申请列表。</p>
     * 
     * @param customerId 客户ID
     * @return 授信申请响应列表
     */
    @Operation(summary = "查询客户授信申请列表", description = "查询指定客户的授信申请列表")
    @GetMapping("/customer/{customerId}")
    public Result<List<CreditApplicationResponse>> getApplicationsByCustomer(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId) {
        List<CreditApplicationResponse> responses = creditApplicationService.getApplicationsByCustomer(customerId);
        return Result.success(responses);
    }
    
    /**
     * 查询授信申请列表（按状态）
     * 
     * <p>根据状态查询授信申请列表。</p>
     * 
     * @param status 状态
     * @return 授信申请响应列表
     */
    @Operation(summary = "查询授信申请列表（按状态）", description = "根据状态查询授信申请列表")
    @GetMapping("/status/{status}")
    public Result<List<CreditApplicationResponse>> getApplicationsByStatus(
            @Parameter(description = "状态", required = true) @PathVariable String status) {
        List<CreditApplicationResponse> responses = creditApplicationService.getApplicationsByStatus(status);
        return Result.success(responses);
    }
    
    /**
     * 审批授信申请
     * 
     * <p>审批授信申请，更新申请状态和批准额度。</p>
     * 
     * @param applicationId 申请ID
     * @param request 授信申请审批请求
     * @return 授信申请响应
     */
    @Operation(summary = "审批授信申请", description = "审批授信申请，更新申请状态和批准额度")
    @PutMapping("/{applicationId}/approve")
    public Result<CreditApplicationResponse> approveCreditApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId,
            @Valid @RequestBody CreditApplicationApprovalRequest request) {
        request.setApplicationId(applicationId);
        CreditApplicationResponse response = creditApplicationService.approveCreditApplication(request);
        return Result.success(response);
    }
    
    /**
     * 更新授信申请
     * 
     * <p>更新授信申请信息。</p>
     * 
     * @param applicationId 申请ID
     * @param request 授信申请请求
     * @return 授信申请响应
     */
    @Operation(summary = "更新授信申请", description = "更新授信申请信息")
    @PutMapping("/{applicationId}")
    public Result<CreditApplicationResponse> updateCreditApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId,
            @Valid @RequestBody CreditApplicationRequest request) {
        CreditApplicationResponse response = creditApplicationService.updateCreditApplication(applicationId, request);
        return Result.success(response);
    }
    
    /**
     * 删除授信申请
     * 
     * <p>删除授信申请记录。</p>
     * 
     * @param applicationId 申请ID
     * @return 操作结果
     */
    @Operation(summary = "删除授信申请", description = "删除授信申请记录")
    @DeleteMapping("/{applicationId}")
    public Result<Void> deleteCreditApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId) {
        creditApplicationService.deleteCreditApplication(applicationId);
        return Result.success();
    }
    
    /**
     * 提交授信申请
     * 
     * <p>提交待审批的授信申请。</p>
     * 
     * @param applicationId 申请ID
     * @param submitter 提交人
     * @return 授信申请响应
     */
    @Operation(summary = "提交授信申请", description = "提交待审批的授信申请")
    @PostMapping("/{applicationId}/submit")
    public Result<CreditApplicationResponse> submitApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId,
            @Parameter(description = "提交人", required = true) @RequestParam String submitter) {
        CreditApplicationResponse response = creditApplicationService.submitApplication(applicationId, submitter);
        return Result.success(response);
    }
    
    /**
     * 查询待审批的授信申请列表
     * 
     * <p>查询所有待审批的授信申请列表。</p>
     * 
     * @return 授信申请响应列表
     */
    @Operation(summary = "查询待审批的授信申请列表", description = "查询所有待审批的授信申请列表")
    @GetMapping("/pending")
    public Result<List<CreditApplicationResponse>> getPendingApplications() {
        List<CreditApplicationResponse> responses = creditApplicationService.getPendingApplications();
        return Result.success(responses);
    }
    
    /**
     * 撤销授信申请
     * 
     * <p>撤销已提交但未审批的授信申请。</p>
     * 
     * @param applicationId 申请ID
     * @param reason 撤销原因
     * @return 授信申请响应
     */
    @Operation(summary = "撤销授信申请", description = "撤销已提交但未审批的授信申请")
    @PostMapping("/{applicationId}/cancel")
    public Result<CreditApplicationResponse> cancelApplication(
            @Parameter(description = "申请ID", required = true) @PathVariable Long applicationId,
            @Parameter(description = "撤销原因", required = true) @RequestParam String reason) {
        CreditApplicationResponse response = creditApplicationService.cancelApplication(applicationId, reason);
        return Result.success(response);
    }
}