package com.bank.quota.core.service;

import com.bank.quota.core.dto.quotacontrol.*;

import java.math.BigDecimal;
import java.util.List;

public interface QuotaControlService {
    
    QuotaLockResult lockQuota(QuotaLockRequest request);
    
    void unlockQuota(String lockId);
    
    QuotaOccupyResult occupyQuota(QuotaOccupyRequest request);
    
    /**
     * 多占额度 - 支持多占规则的额度占用
     */
    QuotaOccupyResult multiOccupyQuota(QuotaOccupyRequest request);
    
    void releaseQuota(String occupyId, BigDecimal amount);
    
    QuotaValidateResult validateQuota(QuotaValidateRequest request);
    
    /**
     * 多占校验 - 根据多占规则校验额度
     */
    QuotaValidateResult validateMultiOccupancy(QuotaValidateRequest request);
    
    QuotaQueryResult queryQuota(QuotaQueryRequest request);
    
    List<QuotaLockResult> getActiveLocks(String objectType, Long objectId);
    
    void expireLocks();
}
