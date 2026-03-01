package com.bank.quota.core.dto.approvalquota;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateApprovalQuotaRequest {
    @Positive(message = "批复额度必须大于0")
    private BigDecimal approvalQuota;
    
    private String description;
    
    private String updateBy;
}
