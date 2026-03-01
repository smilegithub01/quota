package com.bank.quota.core.service;

import com.bank.quota.core.dto.approvalquota.*;

import java.math.BigDecimal;
import java.util.List;

public interface ApprovalQuotaService {
    
    ApprovalQuotaResponse createApprovalQuota(CreateApprovalQuotaRequest request);
    
    ApprovalQuotaResponse updateApprovalQuota(Long approvalId, UpdateApprovalQuotaRequest request);
    
    ApprovalQuotaResponse freezeApprovalQuota(Long approvalId, String reason, String operator);
    
    ApprovalQuotaResponse unfreezeApprovalQuota(Long approvalId, String operator);
    
    ApprovalQuotaResponse disableApprovalQuota(Long approvalId, String reason, String operator);
    
    ApprovalQuotaResponse getApprovalQuota(Long approvalId);
    
    ApprovalQuotaResponse getApprovalQuotaByNo(String approvalNo);
    
    List<ApprovalQuotaResponse> getApprovalQuotasByCustomerQuotaSubId(Long customerQuotaSubId);
    
    List<ApprovalQuotaResponse> getEnabledApprovalQuotas();
    
    List<ApprovalQuotaResponse> getApprovalQuotasByType(String approvalType);
    
    ApprovalQuotaUsageResponse getApprovalQuotaUsage(Long approvalId);
    
    void adjustApprovalQuota(Long approvalId, BigDecimal adjustmentAmount, String reason, String operator);
    
    void withdrawApprovalQuota(Long approvalId, BigDecimal amount, String reason, String operator);
}
