package com.bank.quota.common.chain;

/**
 * 抽象处理器基类
 * 
 * 提供处理器的通用实现，简化具体处理器的开发
 * 实现了链式调用的核心逻辑：处理完成后根据结果决定是否传递给下一个处理器
 */
public abstract class AbstractHandler implements Handler {
    
    /**
     * 处理器名称，由子类提供
     */
    protected String handlerName;
    
    /**
     * 下一个处理器
     */
    protected Handler nextHandler;
    
    /**
     * 构造函数
     * 
     * @param handlerName 处理器名称
     */
    protected AbstractHandler(String handlerName) {
        this.handlerName = handlerName;
    }
    
    /**
     * 处理请求的模板方法
     * 
     * 该方法实现了责任链的核心逻辑：
     * 1. 调用子类的doHandle方法进行实际处理
     * 2. 根据处理结果决定是否传递给下一个处理器
     * 3. 如果没有下一个处理器但请求需要继续传递，记录警告日志
     * 
     * @param context 请求上下文
     * @return HandleResult 处理结果
     */
    @Override
    public HandleResult handleRequest(RequestContext context) {
        System.out.println("  [" + handlerName + "] 开始处理请求: " + context.getRequestId());
        
        // 调用子类的实际处理逻辑
        HandleResult result = doHandle(context);
        
        System.out.println("  [" + handlerName + "] 处理结果: " + result + ", 消息: " + context.getMessage());
        
        // 根据处理结果决定是否继续传递
        switch (result) {
            case PASSED:
                // 处理通过，如果存在下一个处理器则传递
                if (nextHandler != null) {
                    System.out.println("  [" + handlerName + "] 请求通过，传递给下一个处理器: " + nextHandler.getHandlerName());
                    return nextHandler.handleRequest(context);
                } else {
                    System.out.println("  [" + handlerName + "] 请求通过，已到达链尾");
                    return result;
                }
                
            case REJECTED:
                // 处理拒绝，不再继续传递
                System.out.println("  [" + handlerName + "] 请求被拒绝，终止处理");
                return result;
                
            case TERMINATED:
                // 处理终止（成功处理完毕），不再继续传递
                System.out.println("  [" + handlerName + "] 请求已成功处理，终止传递");
                return result;
                
            default:
                return result;
        }
    }
    
    /**
     * 子类实现的实际处理逻辑
     * 
     * @param context 请求上下文
     * @return HandleResult 处理结果
     */
    protected abstract HandleResult doHandle(RequestContext context);
    
    /**
     * 设置下一个处理器
     */
    @Override
    public void setNextHandler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }
    
    /**
     * 获取处理器名称
     */
    @Override
    public String getHandlerName() {
        return handlerName;
    }
    
    /**
     * 获取下一个处理器
     */
    @Override
    public Handler getNextHandler() {
        return nextHandler;
    }
}
