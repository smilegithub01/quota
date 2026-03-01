package com.bank.quota.core.enums;

public enum IndexStatus {
    ACTIVE("有效"),
    INACTIVE("无效");

    private final String description;

    IndexStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
