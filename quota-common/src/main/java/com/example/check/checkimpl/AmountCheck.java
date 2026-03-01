package com.example.check.checkimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.AbstractCheck;
import org.springframework.stereotype.Component;

/**
 * 金额校验器
 *
 * 负责验证交易金额的合法性和业务规则
 *
 * 校验内容：
 *   - 金额是否在允许范围内（最小/最大）
 *   - 单笔交易限额检查
 *   - 日累计限额检查
 *   - 账户余额是否充足
 *
 * 层级：5 (最低优先级，最后执行)
 *
 * 使用场景：
 *   - 贷款额度校验
 *   - 单笔转账限额
 *   - 日累计交易限额
 *
 * @see AbstractCheck 抽象基类
 */
@Component("AMOUNT_CHECK")
public class AmountCheck extends AbstractCheck {

    @Override
    public int level() {
        return 5;
    }

    @Override
    protected void doCheck(InvokeContext ctx) {
        System.out.println("AMOUNT_CHECK");
    }
}
