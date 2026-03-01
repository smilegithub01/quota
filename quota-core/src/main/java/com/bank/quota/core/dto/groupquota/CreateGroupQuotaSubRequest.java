package com.bank.quota.core.dto.groupquota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateGroupQuotaSubRequest {
    @NotBlank(message = "子类型不能为空")
    private String subType;
    
    @NotBlank(message = "子类型名称不能为空")
    private String subTypeName;
    
    @NotNull(message = "子限额不能为空")
    @Positive(message = "子限额必须大于0")
    private BigDecimal subQuota;
    
    @NotBlank(message = "创建人不能为空")
    private String createBy;
}
