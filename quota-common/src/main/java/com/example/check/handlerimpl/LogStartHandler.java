package com.example.check.handlerimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.Handler;
import org.springframework.stereotype.Component;

/**
 * 日志开始处理器
 *
 * 在接口执行流程开始时记录日志
 *
 * 功能：
 *   - 记录请求开始时间
 *   - 打印请求入口日志
 *   - 初始化日志上下文
 *
 * 所属流程：commonProcess (通用处理链)
 *
 * @see Handler 处理器接口
 */
@Component("logStart")
public class LogStartHandler implements Handler {

    @Override
    public void handle(InvokeContext ctx) {
        System.out.println("===== 执行开始 =====");
    }
}
