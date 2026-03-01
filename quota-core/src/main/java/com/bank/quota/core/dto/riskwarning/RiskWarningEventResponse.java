package com.bank.quota.core.dto.riskwarning;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RiskWarningEventResponse {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String warningLevel;
    private String objectType;
    private Long objectId;
    private String warningMessage;
    private BigDecimal currentValue;
    private BigDecimal thresholdValue;
    private String status;
    private String action;
    private String actionRemark;
    private String processedBy;
    private LocalDateTime processedTime;
    private LocalDateTime triggerTime;
    private LocalDateTime createTime;
}
