package com.bank.quota.common.chain;

import java.util.HashSet;
import java.util.Set;

/**
 * IP白名单处理器
 * 
 * 白名单处理器的一种，用于验证请求的客户端IP是否在允许的IP列表中
 * 这是金融系统中常见的安全控制措施，只有来自受信任IP地址的请求才能继续处理
 * 
 * 设计说明：
 * - 白名单处理器负责判断请求是否来自可信来源
 * - IP白名单通常用于限制只有内网或特定安全网络的请求才能访问敏感接口
 * - 当IP不在白名单中时，直接拒绝请求，确保系统安全
 */
public class IpWhitelistHandler extends AbstractHandler {
    
    /**
     * IP白名单集合
     */
    private final Set<String> whitelist;
    
    /**
     * 是否启用严格模式
     * 严格模式下，白名单为空时拒绝所有请求
     * 非严格模式下，白名单为空时允许所有请求通过
     */
    private final boolean strictMode;
    
    /**
     * 构造函数，使用默认白名单（严格模式）
     * 
     * @param whitelist 白名单IP集合
     */
    public IpWhitelistHandler(Set<String> whitelist) {
        this(whitelist, true);
    }
    
    /**
     * 构造函数
     * 
     * @param whitelist 白名单IP集合
     * @param strictMode 是否严格模式
     */
    public IpWhitelistHandler(Set<String> whitelist, boolean strictMode) {
        super("IP白名单处理器");
        this.whitelist = whitelist != null ? new HashSet<>(whitelist) : new HashSet<>();
        this.strictMode = strictMode;
    }
    
    /**
     * 实际的处理逻辑
     * 
     * 检查请求的客户端IP是否在白名单中
     * - 如果白名单包含该IP，允许请求通过（PASSED）
     * - 如果白名单不包含该IP，拒绝请求（REJECTED）
     * - 如果白名单为空且非严格模式，允许请求通过（PASSED）
     * - 如果白名单为空且严格模式，拒绝请求（REJECTED）
     */
    @Override
    protected HandleResult doHandle(RequestContext context) {
        String clientIp = context.getClientIp();
        
        // 检查客户端IP
        if (clientIp == null || clientIp.isEmpty()) {
            context.setMessage("客户端IP为空，拒绝访问");
            return HandleResult.REJECTED;
        }
        
        // 严格模式下，白名单为空则拒绝所有请求
        if (strictMode && whitelist.isEmpty()) {
            context.setMessage("IP白名单未配置，拒绝所有请求");
            return HandleResult.REJECTED;
        }
        
        // 非严格模式下，白名单为空则允许所有请求
        if (!strictMode && whitelist.isEmpty()) {
            context.setMessage("IP白名单为空，非严格模式，允许所有请求");
            return HandleResult.PASSED;
        }
        
        // 检查IP是否在白名单中
        if (whitelist.contains(clientIp)) {
            context.setMessage("IP [" + clientIp + "] 在白名单中，允许通过");
            return HandleResult.PASSED;
        } else {
            context.setMessage("IP [" + clientIp + "] 不在白名单中，拒绝访问");
            return HandleResult.REJECTED;
        }
    }
    
    /**
     * 添加IP到白名单
     */
    public void addIp(String ip) {
        if (ip != null && !ip.isEmpty()) {
            whitelist.add(ip);
        }
    }
    
    /**
     * 从白名单移除IP
     */
    public void removeIp(String ip) {
        if (ip != null) {
            whitelist.remove(ip);
        }
    }
    
    /**
     * 检查IP是否在白名单中
     */
    public boolean isWhitelisted(String ip) {
        return whitelist.contains(ip);
    }
    
    /**
     * 获取白名单大小
     */
    public int getWhitelistSize() {
        return whitelist.size();
    }
}
