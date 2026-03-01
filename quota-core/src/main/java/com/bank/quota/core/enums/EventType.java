package com.bank.quota.core.enums;

public enum EventType {
    CREATE("创建"),
    ACTIVATE("启用"),
    ADJUST("调整"),
    OCCUPY("占用"),
    RELEASE("释放"),
    FREEZE("冻结"),
    UNFREEZE("解冻"),
    TERMINATE("终止");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
