package com.bank.quota.core.dto.lifecycle;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class QuotaFreezeRequest {
    
    @NotNull(message = "额度ID不能为空")
    private Long quotaId;
    
    @NotBlank(message = "额度类型不能为空")
    private String quotaType;
    
    @NotBlank(message = "冻结类型不能为空")
    private String freezeType;
    
    private BigDecimal freezeAmount;
    
    @NotBlank(message = "冻结原因不能为空")
    @Size(max = 500, message = "冻结原因长度不能超过500")
    private String reason;
    
    @Size(max = 500, message = "冻结条件长度不能超过500")
    private String condition;
    
    @NotBlank(message = "操作人不能为空")
    private String operator;
}
