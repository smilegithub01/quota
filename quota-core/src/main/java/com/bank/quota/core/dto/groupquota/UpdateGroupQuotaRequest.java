package com.bank.quota.core.dto.groupquota;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateGroupQuotaRequest {
    @Positive(message = "总限额必须大于0")
    private BigDecimal totalQuota;
    
    private String description;
    
    private String updateBy;
}
