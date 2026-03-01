package com.bank.quota.core.dto.quotacontrol;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuotaLockResult {
    private String lockId;
    private String objectType;
    private Long objectId;
    private BigDecimal lockedAmount;
    private String status;
    private LocalDateTime lockTime;
    private LocalDateTime expiryTime;
}
