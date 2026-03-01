package com.bank.quota.core.dto.groupquota;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupQuotaQueryRequest {
    private Long groupId;
    private String groupName;
    private String status;
    private Integer pageNum;
    private Integer pageSize;
}
