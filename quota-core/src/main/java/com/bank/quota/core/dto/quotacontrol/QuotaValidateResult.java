package com.bank.quota.core.dto.quotacontrol;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaValidateResult {
    private boolean passed;
    private boolean valid;
    private BigDecimal availableQuota;
    private boolean whitelisted;
    private String whitelistNo;
    private String message;
    private String ruleCode;
    private String ruleName;
    private Integer validationLevel;
    private String errorCode;
    private LocalDateTime validationTime;
    
    // 多占规则相关字段
    private boolean multiOccupancyAllowed;
    private String multiOccupancyType;
    private BigDecimal maxMultiOccupancyAmount;
    private Integer currentOccupants;
}
