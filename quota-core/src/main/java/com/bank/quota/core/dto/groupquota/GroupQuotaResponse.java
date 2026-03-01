package com.bank.quota.core.dto.groupquota;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupQuotaResponse {
    private Long id;
    private Long groupId;
    private String groupName;
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
