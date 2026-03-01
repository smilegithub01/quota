package com.bank.quota.core.dto.customerquota;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerQuotaQueryRequest {
    private Long customerId;
    private String customerName;
    private String customerType;
    private Long groupId;
    private String status;
    private Integer pageNum;
    private Integer pageSize;
}
