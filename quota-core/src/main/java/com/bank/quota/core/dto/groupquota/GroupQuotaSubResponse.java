package com.bank.quota.core.dto.groupquota;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupQuotaSubResponse {
    private Long id;
    private Long groupQuotaId;
    private String subType;
    private String subTypeName;
    private BigDecimal subQuota;
    private BigDecimal usedQuota;
    private BigDecimal availableQuota;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
    private BigDecimal usageRate;
}
