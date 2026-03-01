package com.bank.quota.core.dto.customerquota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateCustomerQuotaRequest {
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    
    @NotBlank(message = "客户名称不能为空")
    private String customerName;
    
    @NotBlank(message = "客户类型不能为空")
    private String customerType;
    
    @NotNull(message = "集团ID不能为空")
    private Long groupId;
    
    @NotNull(message = "总限额不能为空")
    @Positive(message = "总限额必须大于0")
    private BigDecimal totalQuota;
    
    private String description;
    
    @NotBlank(message = "创建人不能为空")
    private String createBy;
}
