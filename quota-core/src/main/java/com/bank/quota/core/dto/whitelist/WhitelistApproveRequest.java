package com.bank.quota.core.dto.whitelist;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WhitelistApproveRequest {
    @NotBlank(message = "白名单编号不能为空")
    private String whitelistNo;
    
    private boolean approved;
    
    private String approveRemark;
    
    @NotBlank(message = "审批人不能为空")
    private String approver;
}
