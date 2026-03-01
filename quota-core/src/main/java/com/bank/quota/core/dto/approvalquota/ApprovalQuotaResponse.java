package com.bank.quota.core.dto.approvalquota;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ApprovalQuotaResponse {
    private Long id;
    private Long customerQuotaSubId;
    private String approvalNo;
    private String approvalType;
    private BigDecimal approvalQuota;
    private BigDecimal usedQuota;
    private BigDecimal availableQuota;
    private String status;
    private String description;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private BigDecimal usageRate;
}
