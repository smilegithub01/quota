package com.bank.quota.core.enums;

public enum CustomerCategory {
    GROUP_CUSTOMER("集团客户"),
    SINGLE_CUSTOMER("单一客户");

    private final String description;

    CustomerCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
