package com.example.check.core;

import com.example.check.context.InvokeContext;

/**
 * 处理器接口 - 责任链模式中的处理节点
 *
 * 该接口用于定义通用处理器，不参与校验链但执行必要的辅助操作
 * 与Check接口的区别：
 *   - Check: 参与校验链，负责业务规则校验，校验失败会阻断流程
 *   - Handler: 执行通用/特殊处理，不直接参与校验判断，用于日志、上下文填充、业务操作等
 *
 * 典型应用场景：
 *   - 日志记录 (LogStartHandler, LogFinishHandler)
 *   - 上下文填充 (ContextFillHandler)
 *   - 认证处理 (AuthCheckHandler)
 *   - 白名单处理 (WhiteListBreakHandler)
 *   - 业务操作 (InsertOrderHandler)
 *
 * @see com.example.check.handlerimpl.LogStartHandler 日志开始处理器
 * @see com.example.check.handlerimpl.LogFinishHandler 日志结束处理器
 * @see com.example.check.handlerimpl.ContextFillHandler 上下文填充处理器
 * @see com.example.check.handlerimpl.InsertOrderHandler 订单创建处理器
 * @see com.example.check.handlerimpl.WhiteListBreakHandler 白名单处理器
 */
public interface Handler {

    /**
     * 处理请求
     *
     * @param ctx 调用上下文，包含请求数据、处理结果等信息
     *            处理成功时：通常保持 ctx.pass=true
     *            处理失败时：ctx.setPass(false) 且 ctx.setErrMsg("错误信息")
     */
    void handle(InvokeContext ctx);
}
