package com.bank.whitelist.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 白名单规则定义
 * 
 * 支持灵活配置不同类型的白名单
 */
public class WhitelistRule {
    
    /**
     * 规则ID
     */
    private String ruleId;
    
    /**
     * 规则名称
     */
    private String ruleName;
    
    /**
     * 规则类型
     */
    private RuleType ruleType;
    
    /**
     * 白名单值集合
     */
    private Set<String> whitelist;
    
    /**
     * 规则描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private boolean enabled;
    
    /**
     * 优先级（越小越优先）
     */
    private int priority;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> extProperties;
    
    public WhitelistRule() {
        this.extProperties = new HashMap<>();
        this.enabled = true;
    }
    
    public WhitelistRule(String ruleId, String ruleName, RuleType ruleType, Set<String> whitelist) {
        this();
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.whitelist = whitelist;
    }
    
    // 规则类型枚举
    public enum RuleType {
        IP,           // IP地址
        USER_ID,      // 用户ID
        USERNAME,     // 用户名
        DEVICE_ID,    // 设备ID
        PHONE,        // 手机号
        EMAIL,        // 邮箱
        API_CODE,     // API编码
        BIZ_CODE,     // 业务编码
        IP_RANGE,     // IP段
        CUSTOM        // 自定义
    }
    
    // 验证类型枚举
    public enum MatchType {
        EXACT,    // 精确匹配
        PREFIX,   // 前缀匹配
        REGEX,    // 正则匹配
        RANGE     // 范围匹配
    }
    
    private MatchType matchType = MatchType.EXACT;
    
    // Getters and Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    
    public RuleType getRuleType() { return ruleType; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }
    
    public Set<String> getWhitelist() { return whitelist; }
    public void setWhitelist(Set<String> whitelist) { this.whitelist = whitelist; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public MatchType getMatchType() { return matchType; }
    public void setMatchType(MatchType matchType) { this.matchType = matchType; }
    
    public Map<String, Object> getExtProperties() { return extProperties; }
    public void setExtProperties(Map<String, Object> extProperties) { this.extProperties = extProperties; }
    
    public void addExtProperty(String key, Object value) {
        this.extProperties.put(key, value);
    }
    
    /**
     * 检查值是否在白名单中
     */
    public boolean contains(String value) {
        if (!enabled || whitelist == null || whitelist.isEmpty() || value == null) {
            return false;
        }
        
        switch (matchType) {
            case EXACT:
                return whitelist.contains(value);
                
            case PREFIX:
                for (String item : whitelist) {
                    if (value.startsWith(item)) {
                        return true;
                    }
                }
                return false;
                
            case REGEX:
                for (String item : whitelist) {
                    if (value.matches(item)) {
                        return true;
                    }
                }
                return false;
                
            default:
                return whitelist.contains(value);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhitelistRule that = (WhitelistRule) o;
        return Objects.equals(ruleId, that.ruleId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(ruleId);
    }
    
    @Override
    public String toString() {
        return "WhitelistRule{" +
                "ruleId='" + ruleId + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", ruleType=" + ruleType +
                ", whitelist=" + whitelist +
                ", enabled=" + enabled +
                ", matchType=" + matchType +
                '}';
    }
}
