package com.example.check.core;

import com.example.check.context.InvokeContext;

/**
 * 校验链核心接口 - 责任链模式中的处理器
 *
 * 该接口定义了校验器的统一入口，每个实现类代表校验链中的一个具体校验环节
 * 典型实现包括：参数校验、权限校验、状态校验、风控校验、金额校验等
 *
 * @see AbstractCheck 抽象基类，提供了白名单跳过机制的默认实现
 * @see com.example.check.checkimpl.ParamCheck 参数校验实现
 * @see com.example.check.checkimpl.AuthCheck 权限校验实现
 * @see com.example.check.checkimpl.StatusCheck 状态校验实现
 * @see com.example.check.checkimpl.RiskCheck 风控校验实现
 * @see com.example.check.checkimpl.AmountCheck 金额校验实现
 */
public interface Check {

    /**
     * 执行校验逻辑
     *
     * @param ctx 调用上下文，包含请求数据、校验结果、白名单配置等信息
     *            校验成功时：ctx.setPass(true)
     *            校验失败时：ctx.setPass(false) 且 ctx.setErrMsg("错误信息")
     */
    void check(InvokeContext ctx);
}
