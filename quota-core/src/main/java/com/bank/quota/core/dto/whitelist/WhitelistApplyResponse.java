package com.bank.quota.core.dto.whitelist;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WhitelistApplyResponse {
    private String whitelistNo;
    private String whitelistType;
    private Long customerId;
    private String customerName;
    private String businessType;
    private String exemptRule;
    private BigDecimal exemptAmount;
    private LocalDateTime effectiveTime;
    private LocalDateTime expiryTime;
    private String status;
    private String message;
    private LocalDateTime applyTime;
}
