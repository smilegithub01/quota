package com.bank.quota.core.service;

import com.bank.quota.core.dto.lifecycle.*;
import com.bank.quota.core.domain.*;

import java.math.BigDecimal;
import java.util.List;

public interface QuotaLifecycleService {
    
    QuotaLifecycleResponse createQuota(QuotaCreateRequest request);
    
    QuotaLifecycleResponse adjustQuota(QuotaAdjustRequest request);
    
    QuotaLifecycleResponse approveAdjustment(String adjustmentNo, boolean approved, String approveRemark, String approver);
    
    QuotaLifecycleResponse freezeQuota(QuotaFreezeRequest request);
    
    QuotaLifecycleResponse unfreezeQuota(QuotaUnfreezeRequest request);
    
    QuotaLifecycleResponse terminateQuota(QuotaTerminateRequest request);
    
    QuotaLifecycleResponse getQuotaLifecycle(Long quotaId, String quotaType);
    
    List<QuotaLifecycleEvent> getQuotaLifecycleEvents(Long quotaId);
    
    List<QuotaAdjustment> getQuotaAdjustmentHistory(Long quotaId);
    
    List<QuotaFreeze> getQuotaFreezeHistory(Long quotaId);
    
    List<QuotaFreeze> getActiveFreezes(Long quotaId);
    
    BigDecimal getTotalFrozenAmount(Long quotaId);
}
