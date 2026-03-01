package com.bank.quota.core.enums;

public enum CustomerType {
    CORPORATE("对公客户"),
    INDIVIDUAL("个人客户"),
    INTERBANK("同业客户");

    private final String description;

    CustomerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
