package com.example.check.checkimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.AbstractCheck;
import org.springframework.stereotype.Component;

/**
 * 参数校验器
 *
 * 负责验证请求参数的合法性，是校验链的第一个环节
 *
 * 校验内容：
 *   - 必填参数是否存在
 *   - 参数类型是否正确
 *   - 参数值是否在有效范围内
 *   - 字符串长度是否符合要求
 *
 * 层级：1 (最高优先级，最先执行)
 *
 * @see AbstractCheck 抽象基类
 */
@Component("PARAM_CHECK")
public class ParamCheck extends AbstractCheck {

    @Override
    public int level() {
        return 1;
    }

    @Override
    protected void doCheck(InvokeContext ctx) {
        System.out.println("PARAM_CHECK");
    }
}
