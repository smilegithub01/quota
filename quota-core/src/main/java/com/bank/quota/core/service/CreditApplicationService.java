package com.bank.quota.core.service;

import com.bank.quota.core.dto.credit.*;

import java.util.List;

/**
 * 授信申请服务
 * 
 * <p>提供银行级信贷额度管控平台的授信申请功能。
 * 包括授信申请的创建、查询、审批等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public interface CreditApplicationService {
    
    /**
     * 创建授信申请
     * 
     * <p>创建新的授信申请记录。</p>
     * 
     * @param request 授信申请请求
     * @return 授信申请响应
     */
    CreditApplicationResponse createCreditApplication(CreditApplicationRequest request);
    
    /**
     * 查询授信申请
     * 
     * <p>根据申请ID查询授信申请详情。</p>
     * 
     * @param applicationId 申请ID
     * @return 授信申请响应
     */
    CreditApplicationResponse getCreditApplication(Long applicationId);
    
    /**
     * 查询客户授信申请列表
     * 
     * <p>查询指定客户的授信申请列表。</p>
     * 
     * @param customerId 客户ID
     * @return 授信申请响应列表
     */
    List<CreditApplicationResponse> getApplicationsByCustomer(Long customerId);
    
    /**
     * 查询授信申请列表
     * 
     * <p>根据状态查询授信申请列表。</p>
     * 
     * @param status 状态
     * @return 授信申请响应列表
     */
    List<CreditApplicationResponse> getApplicationsByStatus(String status);
    
    /**
     * 审批授信申请
     * 
     * <p>审批授信申请，更新申请状态和批准额度。</p>
     * 
     * @param request 授信申请审批请求
     * @return 授信申请响应
     */
    CreditApplicationResponse approveCreditApplication(CreditApplicationApprovalRequest request);
    
    /**
     * 更新授信申请
     * 
     * <p>更新授信申请信息。</p>
     * 
     * @param applicationId 申请ID
     * @param request 授信申请请求
     * @return 授信申请响应
     */
    CreditApplicationResponse updateCreditApplication(Long applicationId, CreditApplicationRequest request);
    
    /**
     * 删除授信申请
     * 
     * <p>删除授信申请记录。</p>
     * 
     * @param applicationId 申请ID
     */
    void deleteCreditApplication(Long applicationId);
    
    /**
     * 提交授信申请
     * 
     * <p>提交待审批的授信申请。</p>
     * 
     * @param applicationId 申请ID
     * @param submitter 提交人
     * @return 授信申请响应
     */
    CreditApplicationResponse submitApplication(Long applicationId, String submitter);
    
    /**
     * 查询待审批的授信申请列表
     * 
     * <p>查询所有待审批的授信申请列表。</p>
     * 
     * @return 授信申请响应列表
     */
    List<CreditApplicationResponse> getPendingApplications();
    
    /**
     * 撤销授信申请
     * 
     * <p>撤销已提交但未审批的授信申请。</p>
     * 
     * @param applicationId 申请ID
     * @param reason 撤销原因
     * @return 授信申请响应
     */
    CreditApplicationResponse cancelApplication(Long applicationId, String reason);
}