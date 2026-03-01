package com.bank.quota.core.dto.quotacontrol;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaOccupyRequest {
    private String objectType;
    private Long objectId;
    private Long customerId;
    private Long groupId;
    private Long approvalId;
    private String businessType;
    private BigDecimal amount;
    private String contractNo;
    private Long approvalQuotaSubId;
    private String operator;
    private String currency;
    private boolean needExtraApproval;
    
    // 多占规则相关字段
    private boolean multiOccupancy;
    private String multiOccupancyType;
    private String parentOccupyId;
    private Long timeSlotId;
    private String occupancyPurpose;
}
