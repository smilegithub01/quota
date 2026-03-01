package com.bank.quota.core.enums;

public enum UsageStatus {
    DRAFT("草稿"),
    SUBMITTED("已提交"),
    IN_REVIEW("审核中"),
    APPROVED("已批准"),
    REJECTED("已拒绝"),
    CANCELLED("已取消"),
    EXECUTED("已执行");

    private final String description;

    UsageStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
