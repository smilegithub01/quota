package com.bank.quota.core.enums;

public enum MappingStatus {
    ACTIVE("有效"),
    INACTIVE("无效");

    private final String description;

    MappingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
