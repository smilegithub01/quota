package com.bank.quota.core.service;

import com.bank.quota.core.dto.approval.*;

import java.util.List;

/**
 * 审批流程服务
 * 
 * <p>提供银行级信贷额度管控平台的审批流程功能。
 * 包括审批流程的发起、查询、审批操作等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public interface ApprovalProcessService {
    
    /**
     * 发起审批流程
     * 
     * <p>根据业务信息发起审批流程。</p>
     * 
     * @param request 审批流程请求
     * @return 审批流程响应
     */
    ApprovalProcessResponse startApprovalProcess(ApprovalProcessRequest request);
    
    /**
     * 查询审批流程
     * 
     * <p>根据流程ID查询审批流程详情。</p>
     * 
     * @param processId 流程ID
     * @return 审批流程响应
     */
    ApprovalProcessResponse getApprovalProcess(Long processId);
    
    /**
     * 查询用户待办审批任务
     * 
     * <p>查询指定用户的待办审批任务列表。</p>
     * 
     * @param request 审批任务查询请求
     * @return 审批流程响应列表
     */
    List<ApprovalProcessResponse> getUserPendingTasks(ApprovalTaskQueryRequest request);
    
    /**
     * 执行审批操作
     * 
     * <p>对审批节点执行审批操作。</p>
     * 
     * @param request 审批操作请求
     * @return 审批流程响应
     */
    ApprovalProcessResponse performApprovalAction(ApprovalActionRequest request);
    
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
    List<ApprovalProcessResponse> getHistoricalApprovals(String approverId, Integer pageNum, Integer pageSize);
    
    /**
     * 撤回审批流程
     * 
     * <p>撤回尚未完成的审批流程。</p>
     * 
     * @param processId 流程ID
     * @param reason 撤回原因
     * @return 审批流程响应
     */
    ApprovalProcessResponse withdrawApprovalProcess(Long processId, String reason);
    
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
    ApprovalProcessResponse delegateApprovalTask(Long processId, Long nodeId, String newApproverId, 
                                               String newApproverName, String operator, String reason);
    
    /**
     * 查询审批流程统计信息
     * 
     * <p>查询审批流程的统计信息。</p>
     * 
     * @param approverId 审批人ID
     * @return 统计信息
     */
    Object getApprovalStatistics(String approverId);
}