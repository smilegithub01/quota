package com.bank.quota.core.enums;

public enum ContractStatus {
    ACTIVE("有效"),
    FROZEN("冻结"),
    TERMINATED("终止");

    private final String description;

    ContractStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
