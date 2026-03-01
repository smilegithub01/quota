package com.bank.quota.core.event;

import lombok.Getter;

@Getter
public class QuotaUnfrozenEvent extends BaseDomainEvent {
    
    private final Long quotaId;
    private final String quotaType;
    private final String operator;
    
    public QuotaUnfrozenEvent(Object source, Long quotaId, String quotaType, String operator) {
        super(source, "QUOTA_UNFROZEN");
        this.quotaId = quotaId;
        this.quotaType = quotaType;
        this.operator = operator;
    }
}
