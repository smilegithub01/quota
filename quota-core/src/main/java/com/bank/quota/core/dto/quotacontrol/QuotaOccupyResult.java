package com.bank.quota.core.dto.quotacontrol;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaOccupyResult {
    private boolean success;
    private String xid;
    private String occupyId;
    private String contractNo;
    private String objectType;
    private Long objectId;
    private BigDecimal occupiedAmount;
    private BigDecimal occupyAmount;
    private boolean whitelisted;
    private String status;
    private String message;
    private LocalDateTime occupyTime;
    
    // 客户和业务类型字段
    private Long customerId;
    private String businessType;
    
    // 多占规则相关字段
    private boolean multiOccupancy;
    private String multiOccupancyType;
    private String parentOccupyId;
    private Integer occupancySequence;
}
