package com.example.check.handlerimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.Handler;
import org.springframework.stereotype.Component;

/**
 * 日志结束处理器
 *
 * 在接口执行流程完成时记录日志
 *
 * 功能：
 *   - 记录请求结束时间
 *   - 打印请求完成日志
 *   - 记录执行结果
 *
 * 所属流程：bizAction (业务动作链)
 *
 * @see Handler 处理器接口
 */
@Component("logFinish")
public class LogFinishHandler implements Handler {

    @Override
    public void handle(InvokeContext ctx) {
        System.out.println("===== 执行完成 =====");
    }
}
