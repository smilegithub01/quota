package com.example.check.common;

import lombok.Data;

/**
 * 白名单配置 - 控制白名单跳过规则
 *
 * 白名单机制用于让特定用户（如VIP客户、内部员工）跳过部分或全部校验
 *
 * 字段说明：
 *   - breakLevel: 允许跳过的最高校验层级
 *     例如：breakLevel=2 表示可以跳过 level<=2 的所有校验
 *   - maxCount: 剩余可用次数
 *     每次跳过校验后减1，为0时不再跳过
 *
 * 使用示例：
 *   // 创建白名单配置，允许跳过2层及以下的校验，共10次机会
 *   WhiteListConfig whiteList = new WhiteListConfig();
 *   whiteList.setBreakLevel(2);
 *   whiteList.setMaxCount(10);
 *
 * 工作原理：
 *   当 AbstractCheck.check() 执行时：
 *   1. 获取当前Check的level()
 *   2. 判断 whiteList.breakLevel >= this.level()
 *   3. 判断 whiteList.maxCount > 0
 *   4. 如果同时满足，则跳过该校验，maxCount--
 *
 * @see com.example.check.core.AbstractCheck 白名单跳过逻辑实现
 * @see com.example.check.context.InvokeContext 调用上下文
 */
@Data
public class WhiteListConfig {

    /**
     * 允许跳过的最高校验层级
     *
     * 含义：可以跳过所有 level <= breakLevel 的校验
     * 例如：
     *   - breakLevel = 0: 不跳过任何校验
     *   - breakLevel = 1: 跳过 level=1 的校验
     *   - breakLevel = 2: 跳过 level=1,2 的校验
     *   - breakLevel = 99: 跳过所有校验
     */
    private int breakLevel;

    /**
     * 剩余可用次数
     *
     * 含义：白名单剩余的跳过次数
     * 每次跳过校验时减1，为0时不再跳过任何校验
     */
    private int maxCount;
}
