package com.example.check.core;

import com.example.check.common.WhiteListConfig;
import com.example.check.context.InvokeContext;

/**
 * 校验抽象基类 - 实现责任链模式与白名单跳过机制
 *
 * 本类是整个校验链的核心基类，提供了：
 * 1. 责任链模式的模板方法 - check() 方法定义了校验的执行流程
 * 2. 白名单跳过机制 - 根据WhiteListConfig决定是否跳过当前校验
 *
 * 工作原理：
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │                    AbstractCheck.check()                    │
 *   ├─────────────────────────────────────────────────────────────┤
 *   │  1. 检查上下文是否已失败 (ctx.pass == false)                │
 *   │     → 已失败则直接返回，不重复校验                          │
 *   │                                                              │
 *   │  2. 检查白名单配置 (WhiteListConfig)                         │
 *   │     → 如果 breakLevel >= 当前level 且 maxCount > 0          │
 *   │     → 消耗一次次数，跳过校验 (白名单生效)                   │
 *   │                                                              │
 *   │  3. 执行实际校验逻辑 doCheck()                              │
 *   │     → 子类实现具体的校验规则                                │
 *   └─────────────────────────────────────────────────────────────┘
 *
 * 设计模式：
 *   - 模板方法模式: check() 定义校验流程骨架，子类实现doCheck()
 *   - 责任链模式: 多个Check实现类组成校验链，依次执行
 *
 * 使用示例：
 *   public class AuthCheck extends AbstractCheck {
 *       @Override public int level() { return 2; }  // 校验级别
 *
 *       @Override protected void doCheck(InvokeContext ctx) {
 *           // 实际权限校验逻辑
 *           if (!hasPermission(ctx)) {
 *               ctx.setPass(false);
 *               ctx.setErrMsg("权限不足");
 *           }
 *       }
 *   }
 *
 * @see Check 校验接口
 * @see WhiteListConfig 白名单配置
 * @see InvokeContext 调用上下文
 */
public abstract class AbstractCheck implements Check {

    /**
     * 获取校验层级
     *
     * 层级用于白名单跳过判断：
     *   - 白名单 breakLevel >= 当前level 时，跳过该校验
     *   - 层级越低越先执行
     *
     * @return 校验层级，数值越小优先级越高
     */
    public abstract int level();

    /**
     * 执行实际校验逻辑
     *
     * 子类需要实现具体的校验规则
     * 校验成功：保持 ctx.pass=true
     * 校验失败：ctx.setPass(false) 且 ctx.setErrMsg("错误信息")
     *
     * @param ctx 调用上下文，包含请求数据等信息
     */
    protected abstract void doCheck(InvokeContext ctx);

    /**
     * 模板方法：执行校验（含白名单跳过逻辑）
     *
     * 该方法是校验链的核心，流程如下：
     *
     * 1. 快速失败检查
     *    如果上下文已经标记为失败(ctx.pass=false)，直接返回，避免重复校验
     *
     * 2. 白名单检查
     *    如果配置了白名单且满足以下条件，跳过当前校验：
     *    - whiteList.breakLevel >= this.level()  (白名单允许跳过当前级别)
     *    - whiteList.maxCount > 0                (白名单次数未用完)
     *    跳过时：将当前level加入已消耗集合，次数减1
     *
     * 3. 执行校验
     *    调用子类实现的doCheck()方法执行实际校验逻辑
     *
     * @param ctx 调用上下文
     */
    @Override
    public final void check(InvokeContext ctx) {
        if (!ctx.isPass()) {
            return;
        }

        WhiteListConfig wl = ctx.getWhiteList();
        if (wl != null && wl.getBreakLevel() >= level() && wl.getMaxCount() > 0) {
            if (!ctx.getConsumedLevels().contains(level())) {
                ctx.getConsumedLevels().add(level());
                wl.setMaxCount(wl.getMaxCount() - 1);
            }
            return;
        }

        doCheck(ctx);
    }
}
