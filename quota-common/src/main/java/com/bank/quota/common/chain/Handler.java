package com.bank.quota.common.chain;

/**
 * 处理器接口
 * 
 * 责任链模式（Chain of Responsibility Pattern）的核心接口
 * 
 * 原理说明：
 * 责任链模式是一种行为型设计模式，它允许多个处理器对象依次处理请求。
 * 每个处理器都有一个指向下一个处理器的引用，形成一条链式结构。
 * 请求从链首开始，依次传递到链上的每个处理器，直到被某个处理器处理或到达链尾。
 * 
 * 本接口定义了所有处理器的通用行为：
 * 1. handleRequest: 处理请求的核心方法，每个具体处理器实现具体的处理逻辑
 * 2. setNextHandler: 设置链上的下一个处理器，建立处理器之间的关联
 * 3. getHandlerName: 获取处理器名称，用于日志和调试
 * 
 * 设计优点：
 * - 解耦：发送者和接收者之间没有直接耦合，发送者只需要知道链的开始
 * - 单一职责：每个处理器只负责自己职责范围内的判断和处理
 * - 灵活性：可以动态地改变链中的处理器顺序或增删处理器
 * - 责任分担：每个处理器可以处理一部分职责，将请求传递给下一个处理器
 */
public interface Handler {
    
    /**
     * 处理请求
     * 
     * @param context 请求上下文，包含请求的所有信息
     * @return HandleResult 处理结果
     *         - PASSED: 处理通过，请求可以继续传递到下一个处理器
     *         - REJECTED: 处理拒绝，请求被当前处理器拒绝，不再继续传递
     *         - TERMINATED: 处理终止，请求已成功处理完毕，不再需要后续处理器处理
     */
    HandleResult handleRequest(RequestContext context);
    
    /**
     * 设置下一个处理器
     * 
     * 通过此方法可以将多个处理器串联成一条责任链
     * 每个处理器处理完请求后，根据返回结果决定是否传递给下一个处理器
     * 
     * @param nextHandler 下一个处理器
     */
    void setNextHandler(Handler nextHandler);
    
    /**
     * 获取处理器名称
     * 
     * 用于日志记录和调试，方便追踪请求在责任链中的处理过程
     * 
     * @return 处理器名称
     */
    String getHandlerName();
    
    /**
     * 获取链上的下一个处理器
     * 
     * @return 下一个处理器，如果没有则返回null
     */
    default Handler getNextHandler() {
        return null;
    }
}
