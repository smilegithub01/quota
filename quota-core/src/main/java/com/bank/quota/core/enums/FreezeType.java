package com.bank.quota.core.enums;

public enum FreezeType {
    FULL("全额冻结"),
    PARTIAL("部分冻结"),
    CONDITIONAL("条件冻结");

    private final String description;

    FreezeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
