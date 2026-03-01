package com.bank.quota.core.enums;

public enum QuotaType {
    GROUP("集团"),
    CUSTOMER("客户"),
    APPROVAL("批复");

    private final String description;

    QuotaType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
