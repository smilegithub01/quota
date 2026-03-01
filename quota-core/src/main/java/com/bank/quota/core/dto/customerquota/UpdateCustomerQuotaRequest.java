package com.bank.quota.core.dto.customerquota;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateCustomerQuotaRequest {
    @Positive(message = "总限额必须大于0")
    private BigDecimal totalQuota;
    
    private String description;
    
    private String updateBy;
}
