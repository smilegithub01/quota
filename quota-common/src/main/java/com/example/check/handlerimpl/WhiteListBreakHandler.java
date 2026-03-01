package com.example.check.handlerimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.Handler;
import org.springframework.stereotype.Component;

/**
 * 白名单处理处理器
 *
 * 当请求命中白名单时执行的后续处理
 *
 * 功能：
 *   - 记录白名单使用情况
 *   - 更新白名单剩余次数
 *   - 记录白名单校验日志
 *
 * 工作时机：
 *   在校验链之后、bizAction之前执行（specialProcess阶段）
 *
 * 注意：
 *   白名单的实际跳过逻辑在 AbstractCheck 中实现
 *   此Handler仅用于记录和后续处理
 *
 * 所属流程：specialProcess (特殊处理链)
 *
 * @see Handler 处理器接口
 * @see com.example.check.core.AbstractCheck 白名单跳过逻辑
 * @see com.example.check.common.WhiteListConfig 白名单配置
 */
@Component("whiteListBreak")
public class WhiteListBreakHandler implements Handler {

    @Override
    public void handle(InvokeContext ctx) {
        System.out.println("白名单校验通过，跳后续校验");
    }
}
