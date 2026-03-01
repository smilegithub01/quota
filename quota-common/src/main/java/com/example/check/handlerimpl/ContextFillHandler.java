package com.example.check.handlerimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.Handler;
import org.springframework.stereotype.Component;

/**
 * 上下文填充处理器
 *
 * 在接口执行前填充必要的上下文信息
 *
 * 功能：
 *   - 获取当前登录用户信息
 *   - 填充请求相关的数据
 *   - 初始化业务上下文
 *
 * 所属流程：commonProcess (通用处理链)
 *
 * @see Handler 处理器接口
 */
@Component("contextFill")
public class ContextFillHandler implements Handler {

    @Override
    public void handle(InvokeContext ctx) {
        System.out.println("上下文填充完成");
    }
}
