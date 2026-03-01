package com.bank.quota.common.chain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 额度校验处理器
 * 
 * 校验处理器的一种，用于验证请求的额度是否在允许的范围内
 * 这是金融系统中特有的校验逻辑，确保用户的额度使用符合业务规则和风险控制要求
 * 
 * 设计说明：
 * - 额度校验是金融系统的核心校验逻辑
 * - 支持单笔额度限制、累计额度限制、日额度限制等
 * - 可以针对不同用户设置不同的额度限制
 * - 支持黑名单机制，超额度直接拒绝
 */
public class QuotaValidationHandler extends AbstractHandler {
    
    /**
     * 用户的当前使用额度（实际应用中应该从数据库查询）
     * key: 用户ID
     * value: 当前已使用额度
     */
    private final Map<Long, BigDecimal> userUsedQuota;
    
    /**
     * 用户额度限制配置
     * key: 用户ID
     * value: 额度限制配置
     */
    private final Map<Long, QuotaLimit> userQuotaLimits;
    
    /**
     * 默认单笔额度限制
     */
    private BigDecimal defaultSingleLimit;
    
    /**
     * 默认每日额度限制
     */
    private BigDecimal defaultDailyLimit;
    
    /**
     * 默认每月额度限制
     */
    private BigDecimal defaultMonthlyLimit;
    
    /**
     * 额度限制配置
     */
    public static class QuotaLimit {
        private BigDecimal singleLimit;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        
        public QuotaLimit(BigDecimal singleLimit, BigDecimal dailyLimit, BigDecimal monthlyLimit) {
            this.singleLimit = singleLimit;
            this.dailyLimit = dailyLimit;
            this.monthlyLimit = monthlyLimit;
        }
        
        public BigDecimal getSingleLimit() { return singleLimit; }
        public void setSingleLimit(BigDecimal singleLimit) { this.singleLimit = singleLimit; }
        public BigDecimal getDailyLimit() { return dailyLimit; }
        public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
        public BigDecimal getMonthlyLimit() { return monthlyLimit; }
        public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    }
    
    /**
     * 构造函数
     */
    public QuotaValidationHandler() {
        super("额度校验处理器");
        this.userUsedQuota = new HashMap<>();
        this.userQuotaLimits = new HashMap<>();
        this.defaultSingleLimit = new BigDecimal("100000.00");
        this.defaultDailyLimit = new BigDecimal("500000.00");
        this.defaultMonthlyLimit = new BigDecimal("2000000.00");
    }
    
    /**
     * 设置默认额度限制
     */
    public void setDefaultLimits(BigDecimal single, BigDecimal daily, BigDecimal monthly) {
        this.defaultSingleLimit = single;
        this.defaultDailyLimit = daily;
        this.defaultMonthlyLimit = monthly;
    }
    
    /**
     * 设置用户额度限制
     */
    public void setUserQuotaLimit(Long userId, QuotaLimit limit) {
        if (userId != null && limit != null) {
            userQuotaLimits.put(userId, limit);
        }
    }
    
    /**
     * 设置用户已使用额度（模拟数据库数据）
     */
    public void setUserUsedQuota(Long userId, BigDecimal usedQuota) {
        if (userId != null && usedQuota != null) {
            userUsedQuota.put(userId, usedQuota);
        }
    }
    
    /**
     * 增加用户已使用额度
     */
    public void addUserUsedQuota(Long userId, BigDecimal amount) {
        if (userId != null && amount != null) {
            userUsedQuota.merge(userId, amount, BigDecimal::add);
        }
    }
    
    /**
     * 实际的处理逻辑
     * 
     * 校验请求的额度是否在允许范围内：
     * 1. 获取用户当前已使用额度
     * 2. 获取用户或默认的额度限制
     * 3. 检查单笔额度是否超限
     * 4. 检查累计额度是否超限
     * 5. 校验通过返回PASSED，超限返回REJECTED
     */
    @Override
    protected HandleResult doHandle(RequestContext context) {
        Long userId = context.getUserId();
        
        // 获取请求金额
        Object amountObj = context.getParam("amount");
        if (amountObj == null) {
            context.setMessage("缺少金额参数");
            return HandleResult.REJECTED;
        }
        
        BigDecimal requestAmount;
        try {
            requestAmount = new BigDecimal(amountObj.toString());
        } catch (NumberFormatException e) {
            context.setMessage("金额格式不正确");
            return HandleResult.REJECTED;
        }
        
        // 检查金额是否为正数
        if (requestAmount.compareTo(BigDecimal.ZERO) <= 0) {
            context.setMessage("金额必须为正数");
            return HandleResult.REJECTED;
        }
        
        // 获取用户的额度限制配置
        QuotaLimit limit = userQuotaLimits.get(userId);
        if (limit == null) {
            limit = new QuotaLimit(defaultSingleLimit, defaultDailyLimit, defaultMonthlyLimit);
        }
        
        // 获取用户当前已使用额度
        BigDecimal usedQuota = userUsedQuota.getOrDefault(userId, BigDecimal.ZERO);
        
        // 1. 检查单笔额度限制
        if (requestAmount.compareTo(limit.getSingleLimit()) > 0) {
            context.setMessage("单笔金额 [" + requestAmount + "] 超过限制 [" + limit.getSingleLimit() + "]");
            return HandleResult.REJECTED;
        }
        
        // 2. 检查累计额度（这里简化为检查已使用额度+请求金额是否超过日限额）
        BigDecimal totalAfterRequest = usedQuota.add(requestAmount);
        
        // 检查是否超过日限额
        if (limit.getDailyLimit() != null && totalAfterRequest.compareTo(limit.getDailyLimit()) > 0) {
            BigDecimal remaining = limit.getDailyLimit().subtract(usedQuota);
            context.setMessage("超过日额度限制，剩余可用额度: " + remaining);
            return HandleResult.REJECTED;
        }
        
        // 3. 检查是否超过月限额（简化处理，假设月限额 = 日限额 * 30）
        BigDecimal monthlyLimit = limit.getMonthlyLimit();
        if (monthlyLimit == null) {
            monthlyLimit = limit.getDailyLimit().multiply(new BigDecimal("30"));
        }
        
        // 模拟月累计（这里简化为直接使用usedQuota，实际应该从数据库查询月累计）
        if (totalAfterRequest.compareTo(monthlyLimit) > 0) {
            context.setMessage("超过月额度限制");
            return HandleResult.REJECTED;
        }
        
        // 校验通过，记录本次使用的额度（实际应用中应该通过事务更新数据库）
        addUserUsedQuota(userId, requestAmount);
        
        context.setMessage("额度校验通过，单笔限额: " + limit.getSingleLimit() + 
                          "，日限额: " + limit.getDailyLimit() + 
                          "，已使用: " + usedQuota.add(requestAmount));
        return HandleResult.PASSED;
    }
    
    /**
     * 获取用户当前已使用额度
     */
    public BigDecimal getUserUsedQuota(Long userId) {
        return userUsedQuota.getOrDefault(userId, BigDecimal.ZERO);
    }
}
