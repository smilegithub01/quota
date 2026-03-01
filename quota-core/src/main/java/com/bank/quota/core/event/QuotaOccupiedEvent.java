package com.bank.quota.core.event;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class QuotaOccupiedEvent extends BaseDomainEvent {
    
    private final Long quotaId;
    private final String quotaType;
    private final String businessRefNo;
    private final BigDecimal occupyAmount;
    private final String operator;
    
    public QuotaOccupiedEvent(Object source, Long quotaId, String quotaType,
            String businessRefNo, BigDecimal occupyAmount, String operator) {
        super(source, "QUOTA_OCCUPIED");
        this.quotaId = quotaId;
        this.quotaType = quotaType;
        this.businessRefNo = businessRefNo;
        this.occupyAmount = occupyAmount;
        this.operator = operator;
    }
}
