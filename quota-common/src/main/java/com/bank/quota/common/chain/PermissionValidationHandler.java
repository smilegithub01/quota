package com.bank.quota.common.chain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 权限校验处理器
 * 
 * 校验处理器的一种，用于验证请求用户是否具有访问特定资源的权限
 * 这是金融系统中核心的访问控制机制，确保用户只能访问其被授权的资源
 * 
 * 设计说明：
 * - 权限校验是继白名单校验后的重要安全控制措施
 * - 支持基于角色的访问控制（RBAC）
 * - 支持基于资源的权限校验
 * - 支持权限继承和组合
 * - 可以配置不同角色的不同权限集合
 */
public class PermissionValidationHandler extends AbstractHandler {
    
    /**
     * 角色权限映射
     * key: 角色名
     * value: 权限集合
     */
    private final Map<String, Set<String>> rolePermissions;
    
    /**
     * 用户直接权限映射
     * key: 用户ID
     * value: 权限集合
     */
    private final Map<Long, Set<String>> userPermissions;
    
    /**
     * 所有用户都需要的公共权限
     */
    private final Set<String> commonPermissions;
    
    /**
     * 是否启用权限校验
     */
    private boolean enabled;
    
    /**
     * 构造函数
     */
    public PermissionValidationHandler() {
        super("权限校验处理器");
        this.rolePermissions = new HashMap<>();
        this.userPermissions = new HashMap<>();
        this.commonPermissions = new HashSet<>();
        this.enabled = true;
    }
    
    /**
     * 添加角色权限
     */
    public void addRolePermission(String role, String permission) {
        rolePermissions.computeIfAbsent(role, k -> new HashSet<>()).add(permission);
    }
    
    /**
     * 批量添加角色权限
     */
    public void addRolePermissions(String role, Set<String> permissions) {
        if (role != null && permissions != null) {
            rolePermissions.computeIfAbsent(role, k -> new HashSet<>()).addAll(permissions);
        }
    }
    
    /**
     * 添加用户直接权限
     */
    public void addUserPermission(Long userId, String permission) {
        userPermissions.computeIfAbsent(userId, k -> new HashSet<>()).add(permission);
    }
    
    /**
     * 批量添加用户直接权限
     */
    public void addUserPermissions(Long userId, Set<String> permissions) {
        if (userId != null && permissions != null) {
            userPermissions.computeIfAbsent(userId, k -> new HashSet<>()).addAll(permissions);
        }
    }
    
    /**
     * 添加公共权限（所有用户都必须拥有）
     */
    public void addCommonPermission(String permission) {
        if (permission != null && !permission.isEmpty()) {
            commonPermissions.add(permission);
        }
    }
    
    /**
     * 设置是否启用权限校验
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 实际的处理逻辑
     * 
     * 校验用户是否具有访问权限：
     * 1. 如果未启用权限校验，直接通过
     * 2. 检查用户是否满足公共权限要求
     * 3. 检查用户角色是否具有所需权限
     * 4. 检查用户是否有直接权限
     * 5. 如果都不满足，返回拒绝
     */
    @Override
    protected HandleResult doHandle(RequestContext context) {
        // 如果未启用权限校验，直接通过
        if (!enabled) {
            context.setMessage("权限校验已禁用，允许通过");
            return HandleResult.PASSED;
        }
        
        Long userId = context.getUserId();
        java.util.List<String> roles = context.getRoles();
        
        // 如果没有用户ID且没有角色，且没有公共权限要求，则通过
        if (userId == null && (roles == null || roles.isEmpty()) && commonPermissions.isEmpty()) {
            context.setMessage("用户无角色且无公共权限要求，允许通过");
            return HandleResult.PASSED;
        }
        
        // 1. 检查公共权限
        Set<String> userAllPermissions = new HashSet<>();
        
        // 获取用户直接权限
        if (userId != null && userPermissions.containsKey(userId)) {
            userAllPermissions.addAll(userPermissions.get(userId));
        }
        
        // 获取角色权限
        if (roles != null) {
            for (String role : roles) {
                if (rolePermissions.containsKey(role)) {
                    userAllPermissions.addAll(rolePermissions.get(role));
                }
            }
        }
        
        // 检查公共权限是否满足
        for (String commonPerm : commonPermissions) {
            if (!userAllPermissions.contains(commonPerm)) {
                context.setMessage("缺少公共权限: [" + commonPerm + "]");
                return HandleResult.REJECTED;
            }
        }
        
        // 2. 检查请求类型对应的权限
        String requestType = context.getRequestType();
        if (requestType != null && !requestType.isEmpty()) {
            String requiredPermission = "api:" + requestType;
            
            if (!userAllPermissions.contains(requiredPermission)) {
                context.setMessage("缺少权限: [" + requiredPermission + "]");
                return HandleResult.REJECTED;
            }
        }
        
        context.setMessage("权限校验通过");
        return HandleResult.PASSED;
    }
    
    /**
     * 检查角色是否有特定权限
     */
    public boolean hasRolePermission(String role, String permission) {
        Set<String> perms = rolePermissions.get(role);
        return perms != null && perms.contains(permission);
    }
    
    /**
     * 检查用户是否有特定权限
     */
    public boolean hasUserPermission(Long userId, String permission) {
        Set<String> perms = userPermissions.get(userId);
        return perms != null && perms.contains(permission);
    }
}
