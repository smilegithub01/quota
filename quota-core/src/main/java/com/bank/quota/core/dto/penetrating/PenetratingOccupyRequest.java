package com.bank.quota.core.dto.penetrating;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PenetratingOccupyRequest {
    
    @NotBlank(message = "客户ID不能为空")
    private String customerId;
    
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    
    @NotBlank(message = "产品类型不能为空")
    private String productType;
    
    @NotNull(message = "占用金额不能为空")
    private BigDecimal amount;
    
    private String contractId;
    
    private String itemId;
    
    private String approvalId;
    
    @NotBlank(message = "操作人不能为空")
    private String operator;
    
    private String businessRefNo;
    
    private List<String> bypassLevels;
}
