package com.bank.quota.core.enums;

public enum AdjustmentStatus {
    PENDING("待审批"),
    UNDER_REVIEW("审核中"),
    APPROVED("已审批"),
    COMPLETED("已完成"),
    REJECTED("已拒绝"),
    FAILED("失败"),
    PARTIAL_SUCCESS("部分成功"),
    CANCELLED("已取消");

    private final String description;

    AdjustmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
