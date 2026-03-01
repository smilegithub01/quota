package com.bank.whitelist.core;

import com.bank.whitelist.common.CheckItem;
import com.bank.whitelist.common.WhitelistRule;
import com.bank.whitelist.common.WhitelistRule.RuleType;
import com.bank.whitelist.common.CheckResult;
import com.bank.whitelist.common.WhitelistRule.MatchType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 白名单管理器
 * 
 * 核心功能：
 * 1. 统一管理所有白名单规则
 * 2. 高效查询和匹配
 * 3. 支持动态更新
 * 4. 提供统一的验证接口
 */
public class WhitelistManager {
    
    /**
     * 白名单规则缓存 key: ruleId -> WhitelistRule
     */
    private final Map<String, WhitelistRule> ruleCache = new ConcurrentHashMap<>();
    
    /**
     * 校验项缓存 key: checkItemId -> CheckItem
     */
    private final Map<String, CheckItem> checkItemCache = new ConcurrentHashMap<>();
    
    /**
     * 规则变更监听器
     */
    private final List<Consumer<RuleChangeEvent>> ruleChangeListeners = new ArrayList<>();
    
    /**
     * 校验项变更监听器
     */
    private final List<Consumer<CheckItemChangeEvent>> checkItemChangeListeners = new ArrayList<>();
    
    /**
     * 统计信息
     */
    private final Map<String, Long> checkCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> rejectCounters = new ConcurrentHashMap<>();
    
    public WhitelistManager() {}
    
    // ==================== 规则管理 ====================
    
    /**
     * 添加或更新白名单规则
     */
    public void addOrUpdateRule(WhitelistRule rule) {
        if (rule == null || rule.getRuleId() == null) {
            return;
        }
        
        String ruleId = rule.getRuleId();
        WhitelistRule oldRule = ruleCache.get(ruleId);
        ruleCache.put(ruleId, rule);
        
        // 触发变更监听
        if (oldRule == null) {
            notifyRuleAdded(rule);
        } else {
            notifyRuleUpdated(rule);
        }
        
        System.out.println("[WhitelistManager] 规则已更新: " + ruleId + ", 类型: " + rule.getRuleType() + 
                          ", 白名单数量: " + (rule.getWhitelist() != null ? rule.getWhitelist().size() : 0));
    }
    
    /**
     * 批量添加或更新规则
     */
    public void addOrUpdateRules(Collection<WhitelistRule> rules) {
        if (rules == null) return;
        rules.forEach(this::addOrUpdateRule);
    }
    
    /**
     * 删除白名单规则
     */
    public void removeRule(String ruleId) {
        WhitelistRule removed = ruleCache.remove(ruleId);
        if (removed != null) {
            notifyRuleRemoved(removed);
            System.out.println("[WhitelistManager] 规则已删除: " + ruleId);
        }
    }
    
    /**
     * 获取规则
     */
    public WhitelistRule getRule(String ruleId) {
        return ruleCache.get(ruleId);
    }
    
    /**
     * 获取所有规则
     */
    public Collection<WhitelistRule> getAllRules() {
        return Collections.unmodifiableCollection(ruleCache.values());
    }
    
    /**
     * 根据类型获取规则
     */
    public List<WhitelistRule> getRulesByType(RuleType ruleType) {
        List<WhitelistRule> result = new ArrayList<>();
        for (WhitelistRule rule : ruleCache.values()) {
            if (rule.getRuleType() == ruleType && rule.isEnabled()) {
                result.add(rule);
            }
        }
        return result;
    }
    
    /**
     * 清空所有规则
     */
    public void clearRules() {
        ruleCache.clear();
        System.out.println("[WhitelistManager] 已清空所有规则");
    }
    
    // ==================== 校验项管理 ====================
    
    /**
     * 注册校验项
     */
    public void registerCheckItem(CheckItem checkItem) {
        if (checkItem == null || checkItem.getCheckItemId() == null) {
            return;
        }
        
        String checkItemId = checkItem.getCheckItemId();
        CheckItem oldItem = checkItemCache.get(checkItemId);
        checkItemCache.put(checkItemId, checkItem);
        
        if (oldItem == null) {
            notifyCheckItemAdded(checkItem);
        } else {
            notifyCheckItemUpdated(checkItem);
        }
        
        System.out.println("[WhitelistManager] 校验项已注册: " + checkItemId + 
                          ", 关联规则: " + checkItem.getWhitelistRuleIds());
    }
    
    /**
     * 批量注册校验项
     */
    public void registerCheckItems(Collection<CheckItem> checkItems) {
        if (checkItems == null) return;
        checkItems.forEach(this::registerCheckItem);
    }
    
    /**
     * 删除校验项
     */
    public void removeCheckItem(String checkItemId) {
        CheckItem removed = checkItemCache.remove(checkItemId);
        if (removed != null) {
            notifyCheckItemRemoved(removed);
            System.out.println("[WhitelistManager] 校验项已删除: " + checkItemId);
        }
    }
    
    /**
     * 获取校验项
     */
    public CheckItem getCheckItem(String checkItemId) {
        return checkItemCache.get(checkItemId);
    }
    
    /**
     * 获取所有校验项
     */
    public Collection<CheckItem> getAllCheckItems() {
        return Collections.unmodifiableCollection(checkItemCache.values());
    }
    
    // ==================== 统一验证接口 ====================
    
    /**
     * 统一校验接口 - 核心方法
     * 
     * @param checkItemId 校验项ID
     * @param context 校验上下文（包含待校验的值）
     * @return CheckResult 校验结果
     */
    public CheckResult check(String checkItemId, CheckContext context) {
        long startTime = System.currentTimeMillis();
        
        // 获取校验项配置
        CheckItem checkItem = checkItemCache.get(checkItemId);
        if (checkItem == null) {
            return buildRejectResult(checkItemId, "CHECK_ITEM_NOT_FOUND", 
                    "校验项不存在: " + checkItemId, null, startTime);
        }
        
        // 检查校验项是否启用
        if (!checkItem.isEnabled()) {
            CheckResult result = CheckResult.pass(checkItemId);
            result.setDurationMs(System.currentTimeMillis() - startTime);
            return result;
        }
        
        // 获取关联的规则
        List<String> ruleIds = checkItem.getWhitelistRuleIds();
        if (ruleIds == null || ruleIds.isEmpty()) {
            // 没有配置白名单规则，默认通过
            CheckResult result = CheckResult.pass(checkItemId);
            result.setDurationMs(System.currentTimeMillis() - startTime);
            return result;
        }
        
        // 根据匹配模式进行校验
        CheckItem.MatchMode mode = checkItem.getMatchMode() != null ? checkItem.getMatchMode() : CheckItem.MatchMode.ANY;
        
        return doCheck(checkItem, ruleIds, context, mode, startTime);
    }
    
    /**
     * 执行校验逻辑
     */
    private CheckResult doCheck(CheckItem checkItem, List<String> ruleIds, 
                               CheckContext context, CheckItem.MatchMode mode, long startTime) {
        String checkItemId = checkItem.getCheckItemId();
        
        boolean anyPassed = false;
        boolean allPassed = true;
        String lastMatchedRule = null;
        
        for (String ruleId : ruleIds) {
            WhitelistRule rule = ruleCache.get(ruleId);
            if (rule == null || !rule.isEnabled()) {
                allPassed = false;
                continue;
            }
            
            // 获取待校验的值
            Object valueToCheck = context.getValue(rule.getRuleType());
            if (valueToCheck == null) {
                allPassed = false;
                continue;
            }
            
            String valueStr = valueToCheck.toString();
            if (rule.contains(valueStr)) {
                anyPassed = true;
                lastMatchedRule = ruleId;
            } else {
                allPassed = false;
            }
        }
        
        // 更新统计
        updateStats(checkItemId);
        
        boolean passed;
        if (mode == CheckItem.MatchMode.ALL) {
            passed = allPassed;
        } else {
            passed = anyPassed;
        }
        
        CheckResult result;
        if (passed) {
            result = CheckResult.pass(checkItemId, lastMatchedRule);
            result.setCheckItemName(checkItem.getCheckItemName());
        } else {
            String errorCode = checkItem.getErrorCode() != null ? 
                    checkItem.getErrorCode() : "WHITELIST_REJECTED";
            String errorMsg = checkItem.getErrorMessage() != null ? 
                    checkItem.getErrorMessage() : "校验未通过";
            result = CheckResult.reject(checkItemId, errorCode, errorMsg, lastMatchedRule);
            result.setCheckItemName(checkItem.getCheckItemName());
            updateRejectStats(checkItemId);
        }
        
        result.setDurationMs(System.currentTimeMillis() - startTime);
        return result;
    }
    
    /**
     * 批量校验
     */
    public List<CheckResult> checkBatch(List<String> checkItemIds, CheckContext context) {
        List<CheckResult> results = new ArrayList<>();
        for (String checkItemId : checkItemIds) {
            results.add(check(checkItemId, context));
        }
        return results;
    }
    
    /**
     * 校验所有已注册的校验项
     */
    public List<CheckResult> checkAll(CheckContext context) {
        List<CheckResult> results = new ArrayList<>();
        for (CheckItem item : checkItemCache.values()) {
            if (item.isEnabled()) {
                results.add(check(item.getCheckItemId(), context));
            }
        }
        return results;
    }
    
    /**
     * 快速判断是否通过（用于性能敏感场景）
     */
    public boolean isWhitelisted(String ruleId, String value) {
        WhitelistRule rule = ruleCache.get(ruleId);
        if (rule == null || !rule.isEnabled()) {
            return false;
        }
        return rule.contains(value);
    }
    
    // ==================== 动态更新支持 ====================
    
    /**
     * 注册规则变更监听器
     */
    public void addRuleChangeListener(Consumer<RuleChangeEvent> listener) {
        ruleChangeListeners.add(listener);
    }
    
    /**
     * 注册校验项变更监听器
     */
    public void addCheckItemChangeListener(Consumer<CheckItemChangeEvent> listener) {
        checkItemChangeListeners.add(listener);
    }
    
    private void notifyRuleAdded(WhitelistRule rule) {
        RuleChangeEvent event = new RuleChangeEvent(RuleChangeEvent.EventType.ADDED, rule);
        ruleChangeListeners.forEach(l -> l.accept(event));
    }
    
    private void notifyRuleUpdated(WhitelistRule rule) {
        RuleChangeEvent event = new RuleChangeEvent(RuleChangeEvent.EventType.UPDATED, rule);
        ruleChangeListeners.forEach(l -> l.accept(event));
    }
    
    private void notifyRuleRemoved(WhitelistRule rule) {
        RuleChangeEvent event = new RuleChangeEvent(RuleChangeEvent.EventType.REMOVED, rule);
        ruleChangeListeners.forEach(l -> l.accept(event));
    }
    
    private void notifyCheckItemAdded(CheckItem item) {
        CheckItemChangeEvent event = new CheckItemChangeEvent(CheckItemChangeEvent.EventType.ADDED, item);
        checkItemChangeListeners.forEach(l -> l.accept(event));
    }
    
    private void notifyCheckItemUpdated(CheckItem item) {
        CheckItemChangeEvent event = new CheckItemChangeEvent(CheckItemChangeEvent.EventType.UPDATED, item);
        checkItemChangeListeners.forEach(l -> l.accept(event));
    }
    
    private void notifyCheckItemRemoved(CheckItem item) {
        CheckItemChangeEvent event = new CheckItemChangeEvent(CheckItemChangeEvent.EventType.REMOVED, item);
        checkItemChangeListeners.forEach(l -> l.accept(event));
    }
    
    // ==================== 统计功能 ====================
    
    private void updateStats(String checkItemId) {
        checkCounters.merge(checkItemId, 1L, Long::sum);
    }
    
    private void updateRejectStats(String checkItemId) {
        rejectCounters.merge(checkItemId, 1L, Long::sum);
    }
    
    public long getCheckCount(String checkItemId) {
        return checkCounters.getOrDefault(checkItemId, 0L);
    }
    
    public long getRejectCount(String checkItemId) {
        return rejectCounters.getOrDefault(checkItemId, 0L);
    }
    
    public Map<String, Long> getAllCheckCounts() {
        return Collections.unmodifiableMap(checkCounters);
    }
    
    public Map<String, Long> getAllRejectCounts() {
        return Collections.unmodifiableMap(rejectCounters);
    }
    
    private CheckResult buildRejectResult(String checkItemId, String errorCode, 
            String errorMessage, String matchedRule, long startTime) {
        CheckResult result = CheckResult.reject(checkItemId, errorCode, errorMessage, matchedRule);
        result.setDurationMs(System.currentTimeMillis() - startTime);
        return result;
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 规则变更事件
     */
    public static class RuleChangeEvent {
        public enum EventType { ADDED, UPDATED, REMOVED }
        
        private final EventType eventType;
        private final WhitelistRule rule;
        
        public RuleChangeEvent(EventType eventType, WhitelistRule rule) {
            this.eventType = eventType;
            this.rule = rule;
        }
        
        public EventType getEventType() { return eventType; }
        public WhitelistRule getRule() { return rule; }
    }
    
    /**
     * 校验项变更事件
     */
    public static class CheckItemChangeEvent {
        public enum EventType { ADDED, UPDATED, REMOVED }
        
        private final EventType eventType;
        private final CheckItem checkItem;
        
        public CheckItemChangeEvent(EventType eventType, CheckItem checkItem) {
            this.eventType = eventType;
            this.checkItem = checkItem;
        }
        
        public EventType getEventType() { return eventType; }
        public CheckItem getCheckItem() { return checkItem; }
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取系统状态信息
     */
    public String getStatus() {
        return String.format("WhitelistManager Status: Rules=%d, CheckItems=%d", 
                ruleCache.size(), checkItemCache.size());
    }
}
