package com.bank.quota.core.dto.quotacontrol;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuotaQueryResult {
    private String objectType;
    private Long objectId;
    private BigDecimal totalQuota;
    private BigDecimal usedQuota;
    private BigDecimal availableQuota;
    private String status;
    private BigDecimal usageRate;
}
