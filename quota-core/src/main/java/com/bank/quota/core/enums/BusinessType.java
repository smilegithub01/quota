package com.bank.quota.core.enums;

/**
 * 业务类型枚举
 * 
 * <p>定义系统中支持的不同业务类型。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public enum BusinessType {
    CREDIT_APPLICATION("授信申请"),
    USAGE_APPLICATION("使用申请"),
    QUOTA_ADJUSTMENT("额度调整");

    private final String description;

    BusinessType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}