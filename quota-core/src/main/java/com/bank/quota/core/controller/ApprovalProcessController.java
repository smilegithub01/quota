package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.approval.*;
import com.bank.quota.core.service.ApprovalProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批流程控制器
 * 
 * <p>提供银行级信贷额度管控平台的审批流程REST API接口。
 * 包括审批流程的发起、查询、审批操作等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 * @see ApprovalProcessService
 */
@Tag(name = "审批流程管理", description = "审批流程发起、查询、审批操作接口")
@RestController
@RequestMapping("/api/v1/approval-process")
@RequiredArgsConstructor
public class ApprovalProcessController {
    
    private final ApprovalProcessService approvalProcessService;
    
    /**
     * 发起审批流程
     * 
     * <p>根据业务信息发起审批流程。</p>
     * 
     * @param request 审批流程请求
     * @return 审批流程响应
     */
    @Operation(summary = "发起审批流程", description = "根据业务信息发起审批流程")
    @PostMapping
    public Result<ApprovalProcessResponse> startApprovalProcess(@Valid @RequestBody ApprovalProcessRequest request) {
        ApprovalProcessResponse response = approvalProcessService.startApprovalProcess(request);
        return Result.success(response);
    }
    
    /**
     * 查询审批流程
     * 
     * <p>根据流程ID查询审批流程详情。</p>
     * 
     * @param processId 流程ID
     * @return 审批流程响应
     */
    @Operation(summary = "查询审批流程", description = "根据流程ID查询审批流程详情")
    @GetMapping("/{processId}")
    public Result<ApprovalProcessResponse> getApprovalProcess(
            @Parameter(description = "流程ID", required = true) @PathVariable Long processId) {
        ApprovalProcessResponse response = approvalProcessService.getApprovalProcess(processId);
        return Result.success(response);
    }
    
    /**
     * 查询用户待办审批任务
     * 
     * <p>查询指定用户的待办审批任务列表。</p>
     * 
     * @param approverId 审批人ID
     * @return 审批流程响应列表
     */
    @Operation(summary = "查询用户待办审批任务", description = "查询指定用户的待办审批任务列表")
    @GetMapping("/tasks/pending")
    public Result<List<ApprovalProcessResponse>> getUserPendingTasks(
            @Parameter(description = "审批人ID", required = true) @RequestParam String approverId) {
        ApprovalTaskQueryRequest request = ApprovalTaskQueryRequest.builder()
                .approverId(approverId)
                .build();
        List<ApprovalProcessResponse> responses = approvalProcessService.getUserPendingTasks(request);
        return Result.success(responses);
    }
    
    /**
     * 执行审批操作
     * 
     * <p>对审批节点执行审批操作。</p>
     * 
     * @param request 审批操作请求
     * @return 审批流程响应
     */
    @Operation(summary = "执行审批操作", description = "对审批节点执行审批操作")
    @PostMapping("/action")
    public Result<ApprovalProcessResponse> performApprovalAction(@Valid @RequestBody ApprovalActionRequest request) {
        ApprovalProcessResponse response = approvalProcessService.performApprovalAction(request);
        return Result.success(response);
    }
    
    /**
     * 查询用户历史审批记录
     * 
     * <p>查询指定用户的历史审批记录。</p>
     * 
     * @param approverId 审批人ID
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 审批流程响应列表
     */
    @Operation(summary = "查询用户历史审批记录", description = "查询指定用户的历史审批记录")
    @GetMapping("/historical")
    public Result<List<ApprovalProcessResponse>> getHistoricalApprovals(
            @Parameter(description = "审批人ID", required = true) @RequestParam String approverId,
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页面大小", required = true) @RequestParam(defaultValue = "10") Integer pageSize) {
        List<ApprovalProcessResponse> responses = approvalProcessService.getHistoricalApprovals(approverId, pageNum, pageSize);
        return Result.success(responses);
    }
    
    /**
     * 撤回审批流程
     * 
     * <p>撤回尚未完成的审批流程。</p>
     * 
     * @param processId 流程ID
     * @param reason 撤回原因
     * @return 审批流程响应
     */
    @Operation(summary = "撤回审批流程", description = "撤回尚未完成的审批流程")
    @PostMapping("/{processId}/withdraw")
    public Result<ApprovalProcessResponse> withdrawApprovalProcess(
            @Parameter(description = "流程ID", required = true) @PathVariable Long processId,
            @Parameter(description = "撤回原因", required = true) @RequestParam String reason) {
        ApprovalProcessResponse response = approvalProcessService.withdrawApprovalProcess(processId, reason);
        return Result.success(response);
    }
    
    /**
     * 转办审批任务
     * 
     * <p>将审批任务转交给其他人处理。</p>
     * 
     * @param processId 流程ID
     * @param nodeId 节点ID
     * @param newApproverId 新审批人ID
     * @param newApproverName 新审批人姓名
     * @param operator 操作人
     * @param reason 转办原因
     * @return 审批流程响应
     */
    @Operation(summary = "转办审批任务", description = "将审批任务转交给其他人处理")
    @PostMapping("/{processId}/delegate/{nodeId}")
    public Result<ApprovalProcessResponse> delegateApprovalTask(
            @Parameter(description = "流程ID", required = true) @PathVariable Long processId,
            @Parameter(description = "节点ID", required = true) @PathVariable Long nodeId,
            @Parameter(description = "新审批人ID", required = true) @RequestParam String newApproverId,
            @Parameter(description = "新审批人姓名", required = true) @RequestParam String newApproverName,
            @Parameter(description = "操作人", required = true) @RequestParam String operator,
            @Parameter(description = "转办原因", required = true) @RequestParam String reason) {
        ApprovalProcessResponse response = approvalProcessService.delegateApprovalTask(
                processId, nodeId, newApproverId, newApproverName, operator, reason);
        return Result.success(response);
    }
    
    /**
     * 查询审批流程统计信息
     * 
     * <p>查询审批流程的统计信息。</p>
     * 
     * @param approverId 审批人ID
     * @return 统计信息
     */
    @Operation(summary = "查询审批流程统计信息", description = "查询审批流程的统计信息")
    @GetMapping("/statistics")
    public Result<Object> getApprovalStatistics(
            @Parameter(description = "审批人ID", required = true) @RequestParam String approverId) {
        Object statistics = approvalProcessService.getApprovalStatistics(approverId);
        return Result.success(statistics);
    }
}