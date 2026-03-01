package com.bank.quota.core.service;

import com.bank.quota.core.domain.CustomerQuota;
import com.bank.quota.core.dto.customerquota.*;
import com.bank.quota.core.enums.CustomerType;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerQuotaService {
    
    CustomerQuotaResponse createCustomerQuota(CreateCustomerQuotaRequest request);
    
    CustomerQuotaResponse updateCustomerQuota(Long customerId, UpdateCustomerQuotaRequest request);
    
    CustomerQuotaResponse freezeCustomerQuota(Long customerId, String reason, String operator);
    
    CustomerQuotaResponse unfreezeCustomerQuota(Long customerId, String operator);
    
    CustomerQuotaResponse disableCustomerQuota(Long customerId, String reason, String operator);
    
    CustomerQuotaResponse getCustomerQuota(Long customerId);
    
    List<CustomerQuotaResponse> getCustomerQuotasByGroupId(Long groupId);
    
    List<CustomerQuotaResponse> getEnabledCustomerQuotas();
    
    List<CustomerQuotaResponse> getCustomerQuotasByType(CustomerType customerType);
    
    CustomerQuotaQueryResponse queryCustomerQuotas(CustomerQuotaQueryRequest request);
    
    CustomerQuotaUsageResponse getCustomerQuotaUsage(Long customerId);
    
    void adjustCustomerQuota(Long customerId, BigDecimal adjustmentAmount, String reason, String operator);
    
    void transferQuota(Long fromCustomerId, Long toCustomerId, BigDecimal amount, String reason, String operator);
}
