package com.bank.quota.core.dto.approvalquota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateApprovalQuotaRequest {
    @NotNull(message = "客户细项额度ID不能为空")
    private Long customerQuotaSubId;
    
    @NotBlank(message = "批复编号不能为空")
    private String approvalNo;
    
    @NotBlank(message = "批复类型不能为空")
    private String approvalType;
    
    @NotNull(message = "批复额度不能为空")
    @Positive(message = "批复额度必须大于0")
    private BigDecimal approvalQuota;
    
    private String description;
    
    @NotBlank(message = "创建人不能为空")
    private String createBy;
}
