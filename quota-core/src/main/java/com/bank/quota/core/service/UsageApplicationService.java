package com.bank.quota.core.service;

import com.bank.quota.core.dto.usage.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用信申请服务
 * 
 * <p>提供银行级信贷额度管控平台的用信申请功能。
 * 包括用信申请的创建、查询、审批等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public interface UsageApplicationService {
    
    /**
     * 创建用信申请
     * 
     * <p>创建新的用信申请记录。</p>
     * 
     * @param request 用信申请请求
     * @return 用信申请响应
     */
    UsageApplicationResponse createUsageApplication(UsageApplicationRequest request);
    
    /**
     * 查询用信申请
     * 
     * <p>根据申请ID查询用信申请详情。</p>
     * 
     * @param applicationId 申请ID
     * @return 用信申请响应
     */
    UsageApplicationResponse getUsageApplication(Long applicationId);
    
    /**
     * 查询客户用信申请列表
     * 
     * <p>查询指定客户的用信申请列表。</p>
     * 
     * @param customerId 客户ID
     * @return 用信申请响应列表
     */
    List<UsageApplicationResponse> getApplicationsByCustomer(Long customerId);
    
    /**
     * 查询用信申请列表
     * 
     * <p>根据状态查询用信申请列表。</p>
     * 
     * @param status 状态
     * @return 用信申请响应列表
     */
    List<UsageApplicationResponse> getApplicationsByStatus(String status);
    
    /**
     * 审批用信申请
     * 
     * <p>审批用信申请，更新申请状态。</p>
     * 
     * @param request 用信申请审批请求
     * @return 用信申请响应
     */
    UsageApplicationResponse approveUsageApplication(UsageApplicationApprovalRequest request);
    
    /**
     * 更新用信申请
     * 
     * <p>更新用信申请信息。</p>
     * 
     * @param applicationId 申请ID
     * @param request 用信申请请求
     * @return 用信申请响应
     */
    UsageApplicationResponse updateUsageApplication(Long applicationId, UsageApplicationRequest request);
    
    /**
     * 删除用信申请
     * 
     * <p>删除用信申请记录。</p>
     * 
     * @param applicationId 申请ID
     */
    void deleteUsageApplication(Long applicationId);
    
    /**
     * 提交用信申请
     * 
     * <p>提交待审批的用信申请。</p>
     * 
     * @param applicationId 申请ID
     * @param submitter 提交人
     * @return 用信申请响应
     */
    UsageApplicationResponse submitApplication(Long applicationId, String submitter);
    
    /**
     * 查询待审批的用信申请列表
     * 
     * <p>查询所有待审批的用信申请列表。</p>
     * 
     * @return 用信申请响应列表
     */
    List<UsageApplicationResponse> getPendingApplications();
    
    /**
     * 撤销用信申请
     * 
     * <p>撤销已提交但未审批的用信申请。</p>
     * 
     * @param applicationId 申请ID
     * @param reason 撤销原因
     * @return 用信申请响应
     */
    UsageApplicationResponse cancelApplication(Long applicationId, String reason);
    
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
    UsageApplicationResponse writeOffApplication(Long applicationId, BigDecimal usageAmount, String operator);
}