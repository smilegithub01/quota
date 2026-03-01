package com.bank.quota.common.chain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 责任链组装器
 * 
 * 负责按顺序组合不同的处理器，构建完整的责任链
 * 提供链式API风格，便于配置和组装处理器
 * 
 * 设计说明：
 * - 责任链组装器将多个处理器串联成一条处理链
 * - 支持链式调用，配置直观方便
 * - 可以预设常用的处理器组合（模板链）
 * - 支持动态添加、移除处理器
 * 
 * 责任链模式的工作原理：
 * 1. 客户端创建请求上下文
 * 2. 客户端将请求交给责任链的首个处理器
 * 3. 每个处理器处理请求后，根据结果决定是否传递给下一个处理器
 * 4. 处理结果沿着责任链传播，直到被某个处理器终止或到达链尾
 * 
 * 典型的处理流程：
 * 请求入口 -> IP白名单 -> 用户ID白名单 -> API白名单 -> 参数校验 -> 权限校验 -> 额度校验 -> 处理完成
 * 
 * 各个处理器的作用：
 * - IP白名单：确保请求来自可信的网络环境
 * - 用户ID白名单：确保请求来自授权的用户
 * - API白名单：确保请求访问的是授权的接口
 * - 参数校验：确保请求参数符合业务规则
 * - 权限校验：确保用户具有访问资源的权限
 * - 额度校验：确保请求的额度在允许范围内
 */
public class ChainBuilder {
    
    /**
     * 处理器列表
     */
    private final List<Handler> handlers;
    
    /**
     * 责任链名称
     */
    private String chainName;
    
    /**
     * 构造函数
     */
    public ChainBuilder() {
        this.handlers = new ArrayList<>();
        this.chainName = "DefaultChain";
    }
    
    /**
     * 构造函数
     * 
     * @param chainName 责任链名称
     */
    public ChainBuilder(String chainName) {
        this.handlers = new ArrayList<>();
        this.chainName = chainName;
    }
    
    /**
     * 添加处理器到链尾
     * 
     * @param handler 处理器
     * @return ChainBuilder 自身，支持链式调用
     */
    public ChainBuilder addHandler(Handler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
        return this;
    }
    
    /**
     * 添加多个处理器到链尾
     * 
     * @param handlers 处理器数组
     * @return ChainBuilder 自身，支持链式调用
     */
    public ChainBuilder addHandlers(Handler... handlers) {
        if (handlers != null) {
            this.handlers.addAll(Arrays.asList(handlers));
        }
        return this;
    }
    
    /**
     * 添加处理器列表到链尾
     * 
     * @param handlers 处理器列表
     * @return ChainBuilder 自身，支持链式调用
     */
    public ChainBuilder addHandlers(List<Handler> handlers) {
        if (handlers != null) {
            this.handlers.addAll(handlers);
        }
        return this;
    }
    
    /**
     * 在指定位置插入处理器
     * 
     * @param index 位置索引
     * @param handler 处理器
     * @return ChainBuilder 自身，支持链式调用
     */
    public ChainBuilder insertHandler(int index, Handler handler) {
        if (handler != null && index >= 0 && index <= handlers.size()) {
            handlers.add(index, handler);
        }
        return this;
    }
    
    /**
     * 移除指定处理器
     * 
     * @param handler 处理器
     * @return ChainBuilder 自身，支持链式调用
     */
    public ChainBuilder removeHandler(Handler handler) {
        if (handler != null) {
            handlers.remove(handler);
        }
        return this;
    }
    
    /**
     * 移除指定名称的处理器
     * 
     * @param handlerName 处理器名称
     * @return ChainBuilder 自身，支持链式调用
     */
    public ChainBuilder removeHandlerByName(String handlerName) {
        handlers.removeIf(h -> h.getHandlerName().equals(handlerName));
        return this;
    }
    
    /**
     * 构建责任链
     * 
     * 将所有处理器串联成一条完整的责任链
     * 设置每个处理器的下一个处理器引用
     * 
     * @return Handler 返回责任链的首个处理器
     */
    public Handler build() {
        if (handlers.isEmpty()) {
            return null;
        }
        
        // 串联所有处理器
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNextHandler(handlers.get(i + 1));
        }
        
        System.out.println("========================================");
        System.out.println("责任链构建完成: " + chainName);
        System.out.println("处理器数量: " + handlers.size());
        System.out.println("处理器列表:");
        for (int i = 0; i < handlers.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + handlers.get(i).getHandlerName());
        }
        System.out.println("========================================");
        
        // 返回链首处理器
        return handlers.get(0);
    }
    
    /**
     * 执行请求处理
     * 
     * 快捷方法：构建链并执行处理
     * 
     * @param context 请求上下文
     * @return HandleResult 处理结果
     */
    public HandleResult execute(RequestContext context) {
        Handler chain = build();
        if (chain == null) {
            context.setMessage("责任链为空，无法处理请求");
            return HandleResult.REJECTED;
        }
        return chain.handleRequest(context);
    }
    
    /**
     * 获取处理器列表
     */
    public List<Handler> getHandlers() {
        return new ArrayList<>(handlers);
    }
    
    /**
     * 获取处理器数量
     */
    public int getHandlerCount() {
        return handlers.size();
    }
    
    /**
     * 设置责任链名称
     */
    public void setChainName(String chainName) {
        this.chainName = chainName;
    }
    
    /**
     * 获取责任链名称
     */
    public String getChainName() {
        return chainName;
    }
    
    /**
     * 创建标准的安全校验链
     * 
     * 标准链：IP白名单 -> 用户ID白名单 -> API白名单 -> 参数校验
     * 
     * @return ChainBuilder 组装器
     */
    public static ChainBuilder createStandardChain() {
        return new ChainBuilder("StandardChain")
                .addHandler(new IpWhitelistHandler(java.util.Collections.emptySet(), false))
                .addHandler(new UserIdWhitelistHandler())
                .addHandler(new ApiWhitelistHandler(false));
    }
    
    /**
     * 创建完整的安全校验链
     * 
     * 完整链：IP白名单 -> 用户ID白名单 -> API白名单 -> 参数校验 -> 权限校验 -> 额度校验
     * 
     * @return ChainBuilder 组装器
     */
    public static ChainBuilder createFullChain() {
        return new ChainBuilder("FullChain")
                .addHandler(new IpWhitelistHandler(java.util.Collections.emptySet(), false))
                .addHandler(new UserIdWhitelistHandler())
                .addHandler(new ApiWhitelistHandler(false))
                .addHandler(new ParameterValidationHandler())
                .addHandler(new PermissionValidationHandler())
                .addHandler(new QuotaValidationHandler());
    }
    
    /**
     * 创建管理后台校验链
     * 
     * 管理链：IP白名单 -> 参数校验 -> 权限校验 -> 额度校验
     * 
     * @return ChainBuilder 组装器
     */
    public static ChainBuilder createAdminChain() {
        return new ChainBuilder("AdminChain")
                .addHandler(new IpWhitelistHandler(java.util.Collections.emptySet(), true))
                .addHandler(new ParameterValidationHandler())
                .addHandler(new PermissionValidationHandler())
                .addHandler(new QuotaValidationHandler());
    }
}
