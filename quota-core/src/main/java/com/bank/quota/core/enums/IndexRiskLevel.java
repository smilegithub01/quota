package com.bank.quota.core.enums;

public enum IndexRiskLevel {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高"),
    CRITICAL("严重");

    private final String description;

    IndexRiskLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
