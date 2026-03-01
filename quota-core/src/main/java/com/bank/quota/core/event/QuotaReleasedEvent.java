package com.bank.quota.core.event;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class QuotaReleasedEvent extends BaseDomainEvent {
    
    private final Long quotaId;
    private final String quotaType;
    private final String businessRefNo;
    private final BigDecimal releaseAmount;
    private final String operator;
    
    public QuotaReleasedEvent(Object source, Long quotaId, String quotaType,
            String businessRefNo, BigDecimal releaseAmount, String operator) {
        super(source, "QUOTA_RELEASED");
        this.quotaId = quotaId;
        this.quotaType = quotaType;
        this.businessRefNo = businessRefNo;
        this.releaseAmount = releaseAmount;
        this.operator = operator;
    }
}
