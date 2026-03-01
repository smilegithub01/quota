package com.bank.quota.core.enums;

public enum FreezeStatus {
    FROZEN("已冻结"),
    UNFROZEN("已解冻");

    private final String description;

    FreezeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
