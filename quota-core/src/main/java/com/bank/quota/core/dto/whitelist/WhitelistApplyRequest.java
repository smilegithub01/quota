package com.bank.quota.core.dto.whitelist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WhitelistApplyRequest {
    @NotBlank(message = "白名单类型不能为空")
    private String whitelistType;
    
    private Long customerId;
    
    private String customerName;
    
    private String businessType;
    
    @NotBlank(message = "豁免规则不能为空")
    private String exemptRule;
    
    private BigDecimal exemptAmount;
    
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime effectiveTime;
    
    @NotNull(message = "失效时间不能为空")
    private LocalDateTime expiryTime;
    
    @NotBlank(message = "申请原因不能为空")
    private String applyReason;
    
    @NotBlank(message = "申请人不能为空")
    private String applicant;
}
