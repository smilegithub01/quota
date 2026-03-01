package com.example.check.checkimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.AbstractCheck;
import org.springframework.stereotype.Component;

/**
 * 权限校验器
 *
 * 负责验证用户是否具有执行当前接口的权限
 *
 * 校验内容：
 *   - 用户是否登录
 *   - 用户角色是否满足要求
 *   - IP地址是否在允许范围内
 *   - 接口访问权限是否匹配
 *
 * 层级：2
 *
 * 使用场景：
 *   - 管理员接口需要ADMIN角色
 *   - 敏感操作需要特定权限
 *   - IP白名单验证
 *
 * @see AbstractCheck 抽象基类
 */
@Component("AUTH_CHECK")
public class AuthCheck extends AbstractCheck {

    @Override
    public int level() {
        return 2;
    }

    @Override
    protected void doCheck(InvokeContext ctx) {
        System.out.println("AUTH_CHECK");
    }
}
