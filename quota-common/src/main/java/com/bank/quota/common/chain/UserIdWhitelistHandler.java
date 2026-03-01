package com.bank.quota.common.chain;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户ID白名单处理器
 * 
 * 白名单处理器的一种，用于验证请求的用户ID是否在允许的用户列表中
 * 这是金融系统中常见的访问控制措施，只有特定用户或内部人员才能访问某些敏感接口
 * 
 * 设计说明：
 * - 用户ID白名单通常用于限制只有特定用户或内部系统账号才能访问某些功能
 * - 常见场景：管理员操作、内部系统调用、特殊业务的白名单用户等
 * - 与角色权限校验不同，白名单是更严格的访问控制机制
 */
public class UserIdWhitelistHandler extends AbstractHandler {
    
    /**
     * 用户ID白名单集合
     */
    private final Set<Long> whitelist;
    
    /**
     * 允许的用户名白名单（作为用户ID白名单的补充）
     */
    private final Set<String> usernameWhitelist;
    
    /**
     * 构造函数
     */
    public UserIdWhitelistHandler() {
        super("用户ID白名单处理器");
        this.whitelist = new HashSet<>();
        this.usernameWhitelist = new HashSet<>();
    }
    
    /**
     * 构造函数，初始化用户ID白名单
     * 
     * @param userIds 用户ID集合
     */
    public UserIdWhitelistHandler(Set<Long> userIds) {
        super("用户ID白名单处理器");
        this.whitelist = userIds != null ? new HashSet<>(userIds) : new HashSet<>();
        this.usernameWhitelist = new HashSet<>();
    }
    
    /**
     * 构造函数，初始化用户ID和用户名白名单
     * 
     * @param userIds 用户ID集合
     * @param usernames 用户名集合
     */
    public UserIdWhitelistHandler(Set<Long> userIds, Set<String> usernames) {
        super("用户ID白名单处理器");
        this.whitelist = userIds != null ? new HashSet<>(userIds) : new HashSet<>();
        this.usernameWhitelist = usernames != null ? new HashSet<>(usernames) : new HashSet<>();
    }
    
    /**
     * 实际的处理逻辑
     * 
     * 检查请求的用户ID或用户名是否在白名单中
     * - 用户ID在白名单中，允许请求通过（PASSED）
     * - 用户名在白名单中，允许请求通过（PASSED）
     * - 两者都不在白名单中，拒绝请求（REJECTED）
     */
    @Override
    protected HandleResult doHandle(RequestContext context) {
        Long userId = context.getUserId();
        String username = context.getUsername();
        
        // 检查用户ID和用户名
        if (userId == null && (username == null || username.isEmpty())) {
            context.setMessage("用户ID和用户名都为空，拒绝访问");
            return HandleResult.REJECTED;
        }
        
        // 白名单为空时拒绝所有请求
        if (whitelist.isEmpty() && usernameWhitelist.isEmpty()) {
            context.setMessage("用户白名单未配置，拒绝所有请求");
            return HandleResult.REJECTED;
        }
        
        // 检查用户ID是否在白名单
        boolean userIdAllowed = userId != null && whitelist.contains(userId);
        
        // 检查用户名是否在白名单
        boolean usernameAllowed = username != null && !username.isEmpty() && usernameWhitelist.contains(username);
        
        // 任一条件满足即可通过
        if (userIdAllowed || usernameAllowed) {
            if (userIdAllowed) {
                context.setMessage("用户ID [" + userId + "] 在白名单中，允许通过");
            } else {
                context.setMessage("用户名 [" + username + "] 在白名单中，允许通过");
            }
            return HandleResult.PASSED;
        } else {
            if (userId != null) {
                context.setMessage("用户ID [" + userId + "] 不在白名单中，拒绝访问");
            } else {
                context.setMessage("用户名 [" + username + "] 不在白名单中，拒绝访问");
            }
            return HandleResult.REJECTED;
        }
    }
    
    /**
     * 添加用户ID到白名单
     */
    public void addUserId(Long userId) {
        if (userId != null) {
            whitelist.add(userId);
        }
    }
    
    /**
     * 添加用户名到白名单
     */
    public void addUsername(String username) {
        if (username != null && !username.isEmpty()) {
            usernameWhitelist.add(username);
        }
    }
    
    /**
     * 从白名单移除用户ID
     */
    public void removeUserId(Long userId) {
        if (userId != null) {
            whitelist.remove(userId);
        }
    }
    
    /**
     * 从白名单移除用户名
     */
    public void removeUsername(String username) {
        if (username != null) {
            usernameWhitelist.remove(username);
        }
    }
    
    /**
     * 检查用户ID是否在白名单中
     */
    public boolean isWhitelisted(Long userId) {
        return whitelist.contains(userId);
    }
    
    /**
     * 检查用户名是否在白名单中
     */
    public boolean isUsernameWhitelisted(String username) {
        return usernameWhitelist.contains(username);
    }
}
