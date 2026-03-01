package com.bank.quota.core.dto.lifecycle;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuotaAdjustRequest {
    
    @NotNull(message = "额度ID不能为空")
    private Long quotaId;
    
    @NotBlank(message = "额度类型不能为空")
    private String quotaType;
    
    @NotBlank(message = "调整类型不能为空")
    private String adjustmentType;
    
    @NotNull(message = "调整金额不能为空")
    private BigDecimal adjustmentAmount;
    
    @NotBlank(message = "调整原因不能为空")
    @Size(max = 500, message = "调整原因长度不能超过500")
    private String reason;
    
    private LocalDateTime effectiveTime;
    
    private LocalDateTime expiryTime;
    
    @NotBlank(message = "申请人不能为空")
    private String applicant;
}
