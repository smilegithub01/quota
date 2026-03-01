package com.bank.quota.core.dto.customerquota;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomerQuotaUsageResponse {
    private Long customerId;
    private String customerName;
    private String customerType;
    private Long groupId;
    private BigDecimal totalQuota;
    private BigDecimal usedQuota;
    private BigDecimal availableQuota;
    private BigDecimal usageRate;
    private List<SubQuotaUsage> subQuotaUsages;
    
    @Data
    public static class SubQuotaUsage {
        private String subType;
        private String subTypeName;
        private BigDecimal subQuota;
        private BigDecimal usedQuota;
        private BigDecimal availableQuota;
        private BigDecimal usageRate;
    }
}
