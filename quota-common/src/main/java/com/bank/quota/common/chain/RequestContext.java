package com.bank.quota.common.chain;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求上下文
 * 封装请求的所有信息，包括请求参数、用户信息、IP等
 * 在责任链中传递，供各个处理器使用
 */
public class RequestContext {
    
    /**
     * 请求ID，用于追踪整个处理流程
     */
    private String requestId;
    
    /**
     * 请求类型
     */
    private String requestType;
    
    /**
     * 客户端IP地址
     */
    private String clientIp;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户角色列表
     */
    private java.util.List<String> roles;
    
    /**
     * 请求参数
     */
    private Map<String, Object> params;
    
    /**
     * 处理结果描述
     */
    private String message;
    
    /**
     * 当前处理状态
     */
    private HandleResult result;
    
    public RequestContext() {
        this.params = new HashMap<>();
        this.roles = new java.util.ArrayList<>();
        this.result = HandleResult.PASSED;
    }
    
    public RequestContext(String requestId, String requestType) {
        this();
        this.requestId = requestId;
        this.requestType = requestType;
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getRequestType() {
        return requestType;
    }
    
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public java.util.List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(java.util.List<String> roles) {
        this.roles = roles;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    public void addParam(String key, Object value) {
        this.params.put(key, value);
    }
    
    public Object getParam(String key) {
        return this.params.get(key);
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public HandleResult getResult() {
        return result;
    }
    
    public void setResult(HandleResult result) {
        this.result = result;
    }
    
    /**
     * 判断是否包含指定角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    @Override
    public String toString() {
        return "RequestContext{" +
                "requestId='" + requestId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                ", params=" + params +
                ", result=" + result +
                ", message='" + message + '\'' +
                '}';
    }
}
