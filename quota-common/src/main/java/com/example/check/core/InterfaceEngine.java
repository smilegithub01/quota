package com.example.check.core;

import com.example.check.config.InterfaceConfig;
import com.example.check.context.InvokeContext;
import com.example.check.common.Result;
import com.example.check.common.WhiteListConfig;
import com.example.check.define.InterfaceDefine;
import com.example.check.validator.ParamValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 接口执行引擎 - 校验链调度中心
 *
 * 本类是整个校验系统的核心调度器，负责：
 * 1. 根据接口名称获取对应的流程配置
 * 2. 依次执行：参数校验 → 通用处理 → 校验链 → 特殊处理 → 业务操作
 * 3. 管理白名单配置的传递
 *
 * 执行流程：
 *   ┌───────────────────────────────────────────────────────────────────┐
 *   │                     InterfaceEngine.execute()                     │
 *   ├───────────────────────────────────────────────────────────────────┤
 *   │                                                                   │
 *   │  1. 获取接口定义                                                   │
 *   │     └─> InterfaceConfig.get(interfaceName)                       │
 *   │                                                                   │
 *   │  2. 参数校验 (ParamValidator)                                     │
 *   │     └─> 验证请求参数是否符合定义                                   │
 *   │                                                                   │
 *   │  3. 通用处理链 (commonProcess)                                    │
 *   │     └─> logStart → contextFill → authCheck                       │
 *   │                                                                   │
 *   │  4. 校验链 (checkChain) 【核心】                                 │
 *   │     └─> PARAM_CHECK → AUTH_CHECK → RISK_CHECK → ...             │
 *   │         ↑ 每个Check都支持白名单跳过                               │
 *   │                                                                   │
 *   │  5. 特殊处理链 (specialProcess)                                  │
 *   │     └─> whiteListBreak (白名单处理)                               │
 *   │                                                                   │
 *   │  6. 业务动作链 (bizAction)                                       │
 *   │     └─> insertOrder → logFinish                                  │
 *   │                                                                   │
 *   └───────────────────────────────────────────────────────────────────┘
 *
 * 使用示例：
 *   // 1. 普通请求（完整校验）
 *   WhiteListConfig whiteList = null;
 *   Result<?> result = engine.execute("submit", request, whiteList);
 *
 *   // 2. 白名单请求（跳过部分校验）
 *   WhiteListConfig whiteList = new WhiteListConfig();
 *   whiteList.setBreakLevel(2);   // 跳过 level <= 2 的校验
 *   whiteList.setMaxCount(10);   // 允许跳过10次
 *   Result<?> result = engine.execute("submit", request, whiteList);
 *
 * @see InterfaceConfig 接口配置管理
 * @see Check 校验接口
 * @see Handler 处理器接口
 * @see WhiteListConfig 白名单配置
 * @see InvokeContext 调用上下文
 */
@Component
@RequiredArgsConstructor
public class InterfaceEngine {

    private final InterfaceConfig config;

    private final Map<String, Check> checkMap;

    private final Map<String, Handler> handlerMap;

    /**
     * 执行接口校验流程
     *
     * 该方法是整个系统的入口，根据接口名称执行完整的校验流程
     *
     * @param interfaceName 接口名称，对应XML配置中的interface节点
     * @param request       请求对象
     * @param whiteList     白名单配置，可为null（null表示不使用白名单）
     * @return 校验结果，成功返回Result.ok()，失败返回Result.fail(错误信息)
     */
    public Result<?> execute(String interfaceName, Object request, WhiteListConfig whiteList) {
        InterfaceDefine def = config.get(interfaceName);
        if (def == null) {
            return Result.fail("接口不存在");
        }

        InvokeContext ctx = new InvokeContext();
        ctx.setRequest(request);
        ctx.setWhiteList(whiteList);

        if (!ParamValidator.validate(def.getParamValidate(), request)) {
            return Result.fail("参数校验失败");
        }

        for (String h : def.getCommonProcess()) {
            handlerMap.get(h).handle(ctx);
            if (!ctx.isPass()) {
                return Result.fail(ctx.getErrMsg());
            }
        }

        for (String c : def.getCheckChain()) {
            checkMap.get(c).check(ctx);
            if (!ctx.isPass()) {
                return Result.fail(ctx.getErrMsg());
            }
        }

        for (String h : def.getSpecialProcess()) {
            handlerMap.get(h).handle(ctx);
            if (!ctx.isPass()) {
                return Result.fail(ctx.getErrMsg());
            }
        }

        for (String h : def.getBizAction()) {
            handlerMap.get(h).handle(ctx);
            if (!ctx.isPass()) {
                return Result.fail(ctx.getErrMsg());
            }
        }

        return Result.ok("执行成功");
    }
}
