package com.bank.whitelist.service;

import com.bank.whitelist.common.CheckItem;
import com.bank.whitelist.common.CheckResult;
import com.bank.whitelist.common.WhitelistRule;
import com.bank.whitelist.common.WhitelistRule.RuleType;
import com.bank.whitelist.common.WhitelistRule.MatchType;
import com.bank.whitelist.core.CheckContext;
import com.bank.whitelist.core.WhitelistManager;

import java.util.*;
import java.util.function.Consumer;

/**
 * 白名单服务门面
 * 
 * 提供简化的API，统一入口
 * 支持链式调用
 */
public class WhitelistService {
    
    private final WhitelistManager manager;
    
    public WhitelistService() {
        this.manager = new WhitelistManager();
    }
    
    public WhitelistService(WhitelistManager manager) {
        this.manager = manager;
    }
    
    // ==================== 规则管理API ====================
    
    /**
     * 添加IP白名单规则
     */
    public WhitelistService addIpRule(String ruleId, String ruleName, String... ips) {
        Set<String> whitelist = new HashSet<>(Arrays.asList(ips));
        WhitelistRule rule = new WhitelistRule(ruleId, ruleName, RuleType.IP, whitelist);
        rule.setMatchType(MatchType.EXACT);
        manager.addOrUpdateRule(rule);
        return this;
    }
    
    /**
     * 添加用户ID白名单规则
     */
    public WhitelistService addUserIdRule(String ruleId, String ruleName, String... userIds) {
        Set<String> whitelist = new HashSet<>(Arrays.asList(userIds));
        WhitelistRule rule = new WhitelistRule(ruleId, ruleName, RuleType.USER_ID, whitelist);
        manager.addOrUpdateRule(rule);
        return this;
    }
    
    /**
     * 添加用户名白名单规则
     */
    public WhitelistService addUsernameRule(String ruleId, String ruleName, String... usernames) {
        Set<String> whitelist = new HashSet<>(Arrays.asList(usernames));
        WhitelistRule rule = new WhitelistRule(ruleId, ruleName, RuleType.USERNAME, whitelist);
        manager.addOrUpdateRule(rule);
        return this;
    }
    
    /**
     * 添加设备ID白名单规则
     */
    public WhitelistService addDeviceIdRule(String ruleId, String ruleName, String... deviceIds) {
        Set<String> whitelist = new HashSet<>(Arrays.asList(deviceIds));
        WhitelistRule rule = new WhitelistRule(ruleId, ruleName, RuleType.DEVICE_ID, whitelist);
        manager.addOrUpdateRule(rule);
        return this;
    }
    
    /**
     * 添加手机号白名单规则
     */
    public WhitelistService addPhoneRule(String ruleId, String ruleName, String... phones) {
        Set<String> whitelist = new HashSet<>(Arrays.asList(phones));
        WhitelistRule rule = new WhitelistRule(ruleId, ruleName, RuleType.PHONE, whitelist);
        manager.addOrUpdateRule(rule);
        return this;
    }
    
    /**
     * 添加API编码白名单规则
     */
    public WhitelistService addApiCodeRule(String ruleId, String ruleName, String... apiCodes) {
        Set<String> whitelist = new HashSet<>(Arrays.asList(apiCodes));
        WhitelistRule rule = new WhitelistRule(ruleId, ruleName, RuleType.API_CODE, whitelist);
        manager.addOrUpdateRule(rule);
        return this;
    }
    
    /**
     * 添加业务编码白名单规则
     */
    public WhitelistService addBizCodeRule(String ruleId, String ruleName, String... bizCodes) {
        Set<String> whitelist = new HashSet<>(Arrays.asList(bizCodes));
        WhitelistRule rule = new WhitelistRule(ruleId, ruleName, RuleType.BIZ_CODE, whitelist);
        manager.addOrUpdateRule(rule);
        return this;
    }
    
    /**
     * 添加带前缀匹配的白名单规则
     */
    public WhitelistService addPrefixRule(String ruleId, String ruleName, RuleType type, String... prefixes) {
        Set<String> whitelist = new HashSet<>(Arrays.asList(prefixes));
        WhitelistRule rule = new WhitelistRule(ruleId, ruleName, type, whitelist);
        rule.setMatchType(MatchType.PREFIX);
        manager.addOrUpdateRule(rule);
        return this;
    }
    
    /**
     * 添加带正则匹配的白名单规则
     */
    public WhitelistService addRegexRule(String ruleId, String ruleName, RuleType type, String... patterns) {
        Set<String> whitelist = new HashSet<>(Arrays.asList(patterns));
        WhitelistRule rule = new WhitelistRule(ruleId, ruleName, type, whitelist);
        rule.setMatchType(MatchType.REGEX);
        manager.addOrUpdateRule(rule);
        return this;
    }
    
    // ==================== 校验项管理API ====================
    
    /**
     * 注册校验项
     */
    public WhitelistService registerCheckItem(String checkItemId, String checkItemName, 
                                               CheckItem.MatchMode mode, String... ruleIds) {
        CheckItem item = new CheckItem(checkItemId, checkItemName);
        item.setMatchMode(mode);
        item.setErrorCode("WHITELIST_REJECTED");
        item.setErrorMessage("白名单校验未通过: " + checkItemName);
        item.setWhitelistRuleIds(Arrays.asList(ruleIds));
        manager.registerCheckItem(item);
        return this;
    }
    
    /**
     * 快速注册校验项（任一匹配模式）
     */
    public WhitelistService registerCheckItem(String checkItemId, String checkItemName, String... ruleIds) {
        return registerCheckItem(checkItemId, checkItemName, CheckItem.MatchMode.ANY, ruleIds);
    }
    
    // ==================== 校验API ====================
    
    /**
     * 统一校验接口
     */
    public CheckResult check(String checkItemId, CheckContext context) {
        return manager.check(checkItemId, context);
    }
    
    /**
     * 快速校验
     */
    public CheckResult check(String checkItemId, String ip, String userId) {
        CheckContext context = new CheckContext()
                .setIp(ip)
                .setUserId(userId);
        return manager.check(checkItemId, context);
    }
    
    /**
     * 快速校验（带用户名）
     */
    public CheckResult check(String checkItemId, String ip, String userId, String username) {
        CheckContext context = new CheckContext()
                .setIp(ip)
                .setUserId(userId)
                .setUsername(username);
        return manager.check(checkItemId, context);
    }
    
    /**
     * 批量校验
     */
    public List<CheckResult> checkBatch(List<String> checkItemIds, CheckContext context) {
        return manager.checkBatch(checkItemIds, context);
    }
    
    /**
     * 校验所有已注册的校验项
     */
    public List<CheckResult> checkAll(CheckContext context) {
        return manager.checkAll(context);
    }
    
    // ==================== 动态更新API ====================
    
    /**
     * 更新白名单（动态添加/修改）
     */
    public WhitelistService updateWhitelist(String ruleId, String... values) {
        WhitelistRule rule = manager.getRule(ruleId);
        if (rule != null) {
            Set<String> whitelist = new HashSet<>(rule.getWhitelist());
            whitelist.addAll(Arrays.asList(values));
            rule.setWhitelist(whitelist);
            manager.addOrUpdateRule(rule);
        }
        return this;
    }
    
    /**
     * 移除白名单
     */
    public WhitelistService removeFromWhitelist(String ruleId, String... values) {
        WhitelistRule rule = manager.getRule(ruleId);
        if (rule != null) {
            Set<String> whitelist = new HashSet<>(rule.getWhitelist());
            whitelist.removeAll(Arrays.asList(values));
            rule.setWhitelist(whitelist);
            manager.addOrUpdateRule(rule);
        }
        return this;
    }
    
    /**
     * 启用/禁用规则
     */
    public WhitelistService setRuleEnabled(String ruleId, boolean enabled) {
        WhitelistRule rule = manager.getRule(ruleId);
        if (rule != null) {
            rule.setEnabled(enabled);
            manager.addOrUpdateRule(rule);
        }
        return this;
    }
    
    /**
     * 启用/禁用校验项
     */
    public WhitelistService setCheckItemEnabled(String checkItemId, boolean enabled) {
        CheckItem item = manager.getCheckItem(checkItemId);
        if (item != null) {
            item.setEnabled(enabled);
            manager.registerCheckItem(item);
        }
        return this;
    }
    
    // ==================== 监听器API ====================
    
    /**
     * 注册规则变更监听器
     */
    public WhitelistService addRuleChangeListener(Consumer<WhitelistManager.RuleChangeEvent> listener) {
        manager.addRuleChangeListener(listener);
        return this;
    }
    
    /**
     * 注册校验项变更监听器
     */
    public WhitelistService addCheckItemChangeListener(Consumer<WhitelistManager.CheckItemChangeEvent> listener) {
        manager.addCheckItemChangeListener(listener);
        return this;
    }
    
    // ==================== 查询API ====================
    
    /**
     * 获取规则
     */
    public WhitelistRule getRule(String ruleId) {
        return manager.getRule(ruleId);
    }
    
    /**
     * 获取校验项
     */
    public CheckItem getCheckItem(String checkItemId) {
        return manager.getCheckItem(checkItemId);
    }
    
    /**
     * 获取统计信息
     */
    public long getCheckCount(String checkItemId) {
        return manager.getCheckCount(checkItemId);
    }
    
    public long getRejectCount(String checkItemId) {
        return manager.getRejectCount(checkItemId);
    }
    
    /**
     * 快速判断是否在白名单中
     */
    public boolean isWhitelisted(String ruleId, String value) {
        return manager.isWhitelisted(ruleId, value);
    }
    
    /**
     * 获取系统状态
     */
    public String getStatus() {
        return manager.getStatus();
    }
    
    /**
     * 获取内部管理器（高级用法）
     */
    public WhitelistManager getManager() {
        return manager;
    }
}
