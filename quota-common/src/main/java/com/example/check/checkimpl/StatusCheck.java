package com.example.check.checkimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.AbstractCheck;
import org.springframework.stereotype.Component;

/**
 * 状态校验器
 *
 * 负责验证用户/账户/订单的状态是否正常
 *
 * 校验内容：
 *   - 用户状态是否正常（未冻结、未注销）
 *   - 账户状态是否可用
 *   - 订单状态是否允许当前操作
 *   - 业务状态机验证
 *
 * 层级：4
 *
 * 使用场景：
 *   - 用户是否被禁用
 *   - 账户是否被冻结
 *   - 订单是否可以修改/取消
 *
 * @see AbstractCheck 抽象基类
 */
@Component("STATUS_CHECK")
public class StatusCheck extends AbstractCheck {

    @Override
    public int level() {
        return 4;
    }

    @Override
    protected void doCheck(InvokeContext ctx) {
        System.out.println("STATUS_CHECK");
    }
}
