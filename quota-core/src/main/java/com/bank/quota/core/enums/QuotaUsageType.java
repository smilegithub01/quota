package com.bank.quota.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 额度使用类型枚举
 * 
 * <p>定义额度使用的不同类型。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Getter
@RequiredArgsConstructor
public enum QuotaUsageType {
    
    /**
     * 占用
     */
    OCCUPY("OCCUPY", "占用"),
    
    /**
     * 释放
     */
    RELEASE("RELEASE", "释放"),
    
    /**
     * 调整
     */
    ADJUST("ADJUST", "调整"),
    
    /**
     * 冻结
     */
    FREEZE("FREEZE", "冻结"),
    
    /**
     * 解冻
     */
    THAW("THAW", "解冻"),
    
    /**
     * 核销
     */
    WRITE_OFF("WRITE_OFF", "核销"),
    
    /**
     * 还款
     */
    REPAYMENT("REPAYMENT", "还款"),
    
    /**
     * 到期
     */
    EXPIRY("EXPIRY", "到期");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 获取JSON序列化的值
     */
    @JsonValue
    public String getCode() {
        return code;
    }
}