package com.bank.quota.core.dto.lifecycle;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class QuotaUnfreezeRequest {
    
    @NotNull(message = "冻结记录ID不能为空")
    private Long freezeId;
    
    private BigDecimal unfreezeAmount;
    
    @NotBlank(message = "解冻原因不能为空")
    @Size(max = 500, message = "解冻原因长度不能超过500")
    private String reason;
    
    @NotBlank(message = "操作人不能为空")
    private String operator;
}
