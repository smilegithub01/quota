package com.bank.quota.core.enums;

/**
 * 多占类型枚举
 * 
 * <p>定义额度多占的类型，支持多种占用模式。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public enum MultiOccupancyType {
    /**
     * 不可多占 - 额度一旦被占用就不能再次占用
     */
    EXCLUSIVE("EXCLUSIVE", "不可多占"),
    
    /**
     * 可重复占用 - 同一笔额度可以被多次占用（需在总额度内）
     */
    REPEATABLE("REPEATABLE", "可重复占用"),
    
    /**
     * 按比例多占 - 按照一定比例允许多个业务占用
     */
    PROPORTIONAL("PROPORTIONAL", "按比例多占"),
    
    /**
     * 按时间多占 - 不同时间段允许多个业务占用
     */
    TIME_SHARED("TIME_SHARED", "按时间多占"),
    
    /**
     * 按业务类型多占 - 不同业务类型允许多个占用
     */
    BY_BUSINESS_TYPE("BY_BUSINESS_TYPE", "按业务类型多占");

    private final String code;
    private final String description;

    MultiOccupancyType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}