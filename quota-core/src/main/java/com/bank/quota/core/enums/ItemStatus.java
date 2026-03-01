package com.bank.quota.core.enums;

public enum ItemStatus {
    ACTIVE("有效"),
    FROZEN("冻结"),
    TERMINATED("终止");

    private final String description;

    ItemStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
