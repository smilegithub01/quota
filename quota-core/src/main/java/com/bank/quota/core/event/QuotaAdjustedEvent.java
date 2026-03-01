package com.bank.quota.core.event;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class QuotaAdjustedEvent extends BaseDomainEvent {
    
    private final Long quotaId;
    private final String quotaType;
    private final BigDecimal oldAmount;
    private final BigDecimal newAmount;
    private final BigDecimal adjustmentAmount;
    private final String reason;
    private final String operator;
    
    public QuotaAdjustedEvent(Object source, Long quotaId, String quotaType,
            BigDecimal oldAmount, BigDecimal newAmount, BigDecimal adjustmentAmount,
            String reason, String operator) {
        super(source, "QUOTA_ADJUSTED");
        this.quotaId = quotaId;
        this.quotaType = quotaType;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
        this.adjustmentAmount = adjustmentAmount;
        this.reason = reason;
        this.operator = operator;
    }
}
