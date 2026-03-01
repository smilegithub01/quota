package com.bank.quota.core.dto.quotacontrol;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuotaLockRequest {
    private String objectType;
    private Long objectId;
    private BigDecimal amount;
    private String lockReason;
    private Integer lockTimeoutMinutes;
    private String operator;
}
