package com.bank.quota.core.enums;

public enum QuotaStatus {
    ENABLED("有效"),
    FROZEN("冻结"),
    DISABLED("停用");

    private final String description;

    QuotaStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
