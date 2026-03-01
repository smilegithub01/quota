package com.bank.whitelist.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 校验项配置
 * 
 * 定义每个校验项及其关联的白名单规则
 */
public class CheckItem {
    
    /**
     * 校验项ID
     */
    private String checkItemId;
    
    /**
     * 校验项名称
     */
    private String checkItemName;
    
    /**
     * 校验项描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private boolean enabled;
    
    /**
     * 关联的白名单规则ID列表
     */
    private List<String> whitelistRuleIds;
    
    /**
     * 校验失败时的错误码
     */
    private String errorCode;
    
    /**
     * 校验失败时的错误消息
     */
    private String errorMessage;
    
    /**
     * 校验顺序（越小越先执行）
     */
    private int order;
    
    /**
     * 匹配模式：ALL(全部匹配) / ANY(任一匹配)
     */
    private MatchMode matchMode;
    
    /**
     * 扩展配置
     */
    private List<CheckItemConfig> configs;
    
    public CheckItem() {
        this.whitelistRuleIds = new ArrayList<>();
        this.enabled = true;
        this.matchMode = MatchMode.ANY;
        this.configs = new ArrayList<>();
    }
    
    public CheckItem(String checkItemId, String checkItemName) {
        this();
        this.checkItemId = checkItemId;
        this.checkItemName = checkItemName;
    }
    
    /**
     * 匹配模式
     */
    public enum MatchMode {
        ALL,   // 全部规则都需要匹配成功
        ANY    // 任一规则匹配成功即可
    }
    
    /**
     * 校验项配置项
     */
    public static class CheckItemConfig {
        private String key;
        private String value;
        
        public CheckItemConfig() {}
        
        public CheckItemConfig(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
    
    // Getters and Setters
    public String getCheckItemId() { return checkItemId; }
    public void setCheckItemId(String checkItemId) { this.checkItemId = checkItemId; }
    
    public String getCheckItemName() { return checkItemName; }
    public void setCheckItemName(String checkItemName) { this.checkItemName = checkItemName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public List<String> getWhitelistRuleIds() { return whitelistRuleIds; }
    public void setWhitelistRuleIds(List<String> whitelistRuleIds) { this.whitelistRuleIds = whitelistRuleIds; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    
    public MatchMode getMatchMode() { return matchMode; }
    public void setMatchMode(MatchMode matchMode) { this.matchMode = matchMode; }
    
    public List<CheckItemConfig> getConfigs() { return configs; }
    public void setConfigs(List<CheckItemConfig> configs) { this.configs = configs; }
    
    public CheckItem addWhitelistRuleId(String ruleId) {
        this.whitelistRuleIds.add(ruleId);
        return this;
    }
    
    public CheckItem addConfig(String key, String value) {
        this.configs.add(new CheckItemConfig(key, value));
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckItem checkItem = (CheckItem) o;
        return Objects.equals(checkItemId, checkItem.checkItemId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(checkItemId);
    }
    
    @Override
    public String toString() {
        return "CheckItem{" +
                "checkItemId='" + checkItemId + '\'' +
                ", checkItemName='" + checkItemName + '\'' +
                ", whitelistRuleIds=" + whitelistRuleIds +
                ", matchMode=" + matchMode +
                ", enabled=" + enabled +
                '}';
    }
}
