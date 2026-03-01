package com.bank.quota.core.service;

import com.bank.quota.core.dto.penetrating.*;

import java.math.BigDecimal;

public interface PenetratingQuotaService {
    
    PenetratingValidateResult validateQuota(PenetratingValidateRequest request);
    
    PenetratingOccupyResult occupyQuota(PenetratingOccupyRequest request);
    
    PenetratingOccupyResult releaseQuota(String occupyNo, BigDecimal amount, String operator);
    
    PenetratingOccupyResult getOccupyDetail(String occupyNo);
    
    BigDecimal getAvailableQuota(String customerId, String businessType, String productType);
}
