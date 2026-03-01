package com.bank.quota.core.enums;

public enum RiskLevel {
    R1("低风险"),
    R2("较低风险"),
    R3("中等风险"),
    R4("较高风险"),
    R5("高风险");

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
