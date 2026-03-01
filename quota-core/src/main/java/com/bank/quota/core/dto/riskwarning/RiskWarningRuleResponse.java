package com.bank.quota.core.dto.riskwarning;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RiskWarningRuleResponse {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private String warningLevel;
    private String thresholdType;
    private BigDecimal thresholdValue;
    private String objectType;
    private String status;
    private String notifyChannels;
    private String notifyRecipients;
    private String description;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
