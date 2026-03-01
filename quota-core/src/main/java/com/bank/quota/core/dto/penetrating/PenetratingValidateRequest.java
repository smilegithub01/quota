package com.bank.quota.core.dto.penetrating;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class PenetratingValidateRequest {
    
    @NotBlank(message = "客户ID不能为空")
    private String customerId;
    
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    
    @NotBlank(message = "产品类型不能为空")
    private String productType;
    
    @NotNull(message = "校验金额不能为空")
    private BigDecimal amount;
    
    private String approvalId;
    
    private String itemId;
    
    private String contractId;
}
