package com.bank.quota.core.dto.riskwarning;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateWarningRuleRequest {
    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;
    
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;
    
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;
    
    @NotBlank(message = "预警级别不能为空")
    private String warningLevel;
    
    @NotBlank(message = "阈值类型不能为空")
    private String thresholdType;
    
    @NotNull(message = "阈值不能为空")
    @Positive(message = "阈值必须大于0")
    private BigDecimal thresholdValue;
    
    @NotBlank(message = "对象类型不能为空")
    private String objectType;
    
    private String notifyChannels;
    
    private String notifyRecipients;
    
    private String description;
    
    @NotBlank(message = "创建人不能为空")
    private String createBy;
}
