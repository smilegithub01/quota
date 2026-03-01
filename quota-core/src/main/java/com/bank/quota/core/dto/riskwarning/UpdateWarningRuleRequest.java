package com.bank.quota.core.dto.riskwarning;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateWarningRuleRequest {
    private String ruleName;
    
    private String warningLevel;
    
    @Positive(message = "阈值必须大于0")
    private BigDecimal thresholdValue;
    
    private String notifyChannels;
    
    private String notifyRecipients;
    
    private String description;
    
    private String updateBy;
}
