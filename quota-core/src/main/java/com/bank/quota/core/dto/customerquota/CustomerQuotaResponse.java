package com.bank.quota.core.dto.customerquota;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerQuotaResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerType;
    private Long groupId;
    private BigDecimal totalQuota;
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
