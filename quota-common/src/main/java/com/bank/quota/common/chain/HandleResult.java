package com.bank.quota.common.chain;

/**
 * 请求结果枚举
 * 用于表示处理器处理请求的结果状态
 */
public enum HandleResult {
    /**
     * 处理通过，请求可以继续传递到下一个处理器
     */
    PASSED,
    
    /**
     * 处理拒绝，请求被当前处理器拒绝，不再继续传递
     */
    REJECTED,
    
    /**
     * 处理终止，请求已成功处理完毕，不再需要后续处理器处理
     */
    TERMINATED
}
