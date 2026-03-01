package com.bank.quota.core.enums;

public enum AdjustmentType {
    TEMPORARY("临时调整"),
    PERMANENT("永久调整"),
    BATCH("批量调整"),
    INCREASE("增加"),
    DECREASE("减少");

    private final String description;

    AdjustmentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
