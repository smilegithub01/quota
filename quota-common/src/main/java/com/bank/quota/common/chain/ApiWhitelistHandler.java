package com.bank.quota.common.chain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * API白名单处理器
 * 
 * 白名单处理器的一种，用于验证请求的API是否在允许的API列表中
 * 这是金融系统中常见的API访问控制措施，只有特定的API才能被调用
 * 
 * 设计说明：
 * - API白名单用于限制接口的访问权限，防止未授权的接口调用
 * - 可以按请求类型（requestType）配置不同的白名单
 * - 支持精确匹配和通配符匹配
 * - 可以设置特定用户只能访问特定API
 */
public class ApiWhitelistHandler extends AbstractHandler {
    
    /**
     * 全局API白名单，所有请求都需要通过
     */
    private final Set<String> globalWhitelist;
    
    /**
     * 按用户ID划分的API白名单
     */
    private final Map<Long, Set<String>> userApiWhitelist;
    
    /**
     * 按角色划分的API白名单
     */
    private final Map<String, Set<String>> roleApiWhitelist;
    
    /**
     * 是否启用严格模式
     */
    private final boolean strictMode;
    
    /**
     * 构造函数，默认严格模式
     */
    public ApiWhitelistHandler() {
        this(true);
    }
    
    /**
     * 构造函数
     * 
     * @param strictMode 是否严格模式
     */
    public ApiWhitelistHandler(boolean strictMode) {
        super("API白名单处理器");
        this.globalWhitelist = new HashSet<>();
        this.userApiWhitelist = new HashMap<>();
        this.roleApiWhitelist = new HashMap<>();
        this.strictMode = strictMode;
    }
    
    /**
     * 实际的处理逻辑
     * 
     * 检查请求的API是否在白名单中：
     * 1. 首先检查全局白名单
     * 2. 然后检查用户特定的白名单
     * 3. 最后检查角色特定的白名单
     * 任一条件满足即可通过
     */
    @Override
    protected HandleResult doHandle(RequestContext context) {
        String requestType = context.getRequestType();
        
        // 检查请求类型
        if (requestType == null || requestType.isEmpty()) {
            context.setMessage("请求类型为空，拒绝访问");
            return HandleResult.REJECTED;
        }
        
        // 严格模式下，白名单为空则拒绝所有请求
        if (strictMode && globalWhitelist.isEmpty() && userApiWhitelist.isEmpty() && roleApiWhitelist.isEmpty()) {
            context.setMessage("API白名单未配置，拒绝所有请求");
            return HandleResult.REJECTED;
        }
        
        // 非严格模式下，白名单为空则允许所有请求
        if (!strictMode && globalWhitelist.isEmpty() && userApiWhitelist.isEmpty() && roleApiWhitelist.isEmpty()) {
            context.setMessage("API白名单为空，非严格模式，允许所有请求");
            return HandleResult.PASSED;
        }
        
        // 1. 检查全局白名单
        if (globalWhitelist.contains(requestType)) {
            context.setMessage("API [" + requestType + "] 在全局白名单中，允许通过");
            return HandleResult.PASSED;
        }
        
        // 2. 检查用户特定白名单
        Long userId = context.getUserId();
        if (userId != null && userApiWhitelist.containsKey(userId)) {
            Set<String> userWhitelist = userApiWhitelist.get(userId);
            if (userWhitelist.contains(requestType)) {
                context.setMessage("API [" + requestType + "] 在用户 [" + userId + "] 的白名单中，允许通过");
                return HandleResult.PASSED;
            }
        }
        
        // 3. 检查角色特定白名单
        for (String role : context.getRoles()) {
            if (roleApiWhitelist.containsKey(role)) {
                Set<String> roleWhitelist = roleApiWhitelist.get(role);
                if (roleWhitelist.contains(requestType)) {
                    context.setMessage("API [" + requestType + "] 在角色 [" + role + "] 的白名单中，允许通过");
                    return HandleResult.PASSED;
                }
            }
        }
        
        // 所有检查都未通过
        context.setMessage("API [" + requestType + "] 不在任何白名单中，拒绝访问");
        return HandleResult.REJECTED;
    }
    
    /**
     * 添加API到全局白名单
     */
    public void addGlobalApi(String api) {
        if (api != null && !api.isEmpty()) {
            globalWhitelist.add(api);
        }
    }
    
    /**
     * 添加API到用户白名单
     */
    public void addUserApi(Long userId, String api) {
        if (userId != null && api != null && !api.isEmpty()) {
            userApiWhitelist.computeIfAbsent(userId, k -> new HashSet<>()).add(api);
        }
    }
    
    /**
     * 添加API到角色白名单
     */
    public void addRoleApi(String role, String api) {
        if (role != null && !role.isEmpty() && api != null && !api.isEmpty()) {
            roleApiWhitelist.computeIfAbsent(role, k -> new HashSet<>()).add(api);
        }
    }
    
    /**
     * 批量添加API到全局白名单
     */
    public void addGlobalApis(Set<String> apis) {
        if (apis != null) {
            globalWhitelist.addAll(apis);
        }
    }
}
