package com.example.check.checkimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.AbstractCheck;
import org.springframework.stereotype.Component;

/**
 * 风控校验器
 *
 * 负责验证请求的风控指标，防止欺诈和风险交易
 *
 * 校验内容：
 *   - 用户风险等级评估
 *   - 交易风险评分
 *   - 实时风控规则匹配
 *   - 黑名单/灰名单检查
 *
 * 层级：3
 *
 * 使用场景：
 *   - 高风险交易拦截
 *   - 异常行为检测
 *   - 欺诈交易预防
 *
 * @see AbstractCheck 抽象基类
 */
@Component("RISK_CHECK")
public class RiskCheck extends AbstractCheck {

    @Override
    public int level() {
        return 3;
    }

    @Override
    protected void doCheck(InvokeContext ctx) {
        System.out.println("RISK_CHECK");
    }
}
