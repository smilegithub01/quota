package com.bank.quota.core.dto.groupquota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateGroupQuotaRequest {
    @NotNull(message = "集团ID不能为空")
    private Long groupId;
    
    @NotBlank(message = "集团名称不能为空")
    private String groupName;
    
    @NotNull(message = "总限额不能为空")
    @Positive(message = "总限额必须大于0")
    private BigDecimal totalQuota;
    
    private String description;
    
    @NotBlank(message = "创建人不能为空")
    private String createBy;
}
