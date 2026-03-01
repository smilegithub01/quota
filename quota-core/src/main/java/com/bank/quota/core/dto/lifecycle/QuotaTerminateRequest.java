package com.bank.quota.core.dto.lifecycle;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class QuotaTerminateRequest {
    
    @NotNull(message = "额度ID不能为空")
    private Long quotaId;
    
    @NotBlank(message = "额度类型不能为空")
    private String quotaType;
    
    @NotBlank(message = "终止类型不能为空")
    private String terminateType;
    
    @NotBlank(message = "终止原因不能为空")
    @Size(max = 500, message = "终止原因长度不能超过500")
    private String reason;
    
    @NotBlank(message = "操作人不能为空")
    private String operator;
    
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
