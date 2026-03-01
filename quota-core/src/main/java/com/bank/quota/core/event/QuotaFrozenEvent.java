package com.bank.quota.core.event;

import lombok.Getter;

@Getter
public class QuotaFrozenEvent extends BaseDomainEvent {
    
    private final Long quotaId;
    private final String quotaType;
    private final String reason;
    private final String operator;
    
    public QuotaFrozenEvent(Object source, Long quotaId, String quotaType,
            String reason, String operator) {
        super(source, "QUOTA_FROZEN");
        this.quotaId = quotaId;
        this.quotaType = quotaType;
        this.reason = reason;
        this.operator = operator;
    }
}
