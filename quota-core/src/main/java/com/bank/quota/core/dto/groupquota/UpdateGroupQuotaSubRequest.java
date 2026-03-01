package com.bank.quota.core.dto.groupquota;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateGroupQuotaSubRequest {
    @Positive(message = "子限额必须大于0")
    private BigDecimal subQuota;
    
    private String subTypeName;
    
    private String updateBy;
}
