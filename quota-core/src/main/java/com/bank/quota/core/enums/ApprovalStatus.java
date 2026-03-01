package com.bank.quota.core.enums;

/**
 * 审批状态枚举
 * 
 * <p>定义审批流程中的各种状态。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public enum ApprovalStatus {
    DRAFT("草稿"),
    PENDING("待审批"),
    SUBMITTED("已提交"),
    UNDER_REVIEW("审核中"),
    APPROVED("已批准"),
    REJECTED("已拒绝"),
    CANCELLED("已取消"),
    TIMEOUT("已超时"),
    EXECUTED("已执行"),
    WITHDRAWN("已撤回");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}