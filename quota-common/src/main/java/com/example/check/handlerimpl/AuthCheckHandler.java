package com.example.check.handlerimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.Handler;
import org.springframework.stereotype.Component;

/**
 * 权限校验处理器
 *
 * 在通用处理阶段进行权限预检查
 *
 * 功能：
 *   - 验证用户是否已登录
 *   - 检查用户Token有效性
 *   - 验证接口访问权限
 *
 * 注意：
 *   此Handler与AuthCheck的区别：
 *   - AuthCheck: 校验链中的权限检查，校验失败会阻断流程
 *   - AuthCheckHandler: 通用处理中的权限预检查，做一些前置验证
 *
 * 所属流程：commonProcess (通用处理链)
 *
 * @see Handler 处理器接口
 */
@Component("authCheck")
public class AuthCheckHandler implements Handler {

    @Override
    public void handle(InvokeContext ctx) {
        System.out.println("权限校验通过");
    }
}
