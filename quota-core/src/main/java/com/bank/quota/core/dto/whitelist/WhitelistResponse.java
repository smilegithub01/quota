package com.bank.quota.core.dto.whitelist;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WhitelistResponse {
    private Long id;
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
    private String applyReason;
    private String approveRemark;
    private String applicant;
    private String approver;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
