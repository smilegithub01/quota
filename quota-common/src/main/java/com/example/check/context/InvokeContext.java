package com.example.check.context;

import com.example.check.common.WhiteListConfig;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 调用上下文 - 贯穿整个校验流程的数据载体
 *
 * 本类是整个校验系统的数据传递载体，在校验链的各个阶段之间传递数据
 *
 * 字段说明：
 *   - request: 请求对象，承载业务数据
 *   - pass: 校验是否通过，false表示校验失败
 *   - errMsg: 错误信息，当pass=false时有效
 *   - whiteList: 白名单配置，用于跳过部分校验
 *   - consumedLevels: 已消耗的白名单层级集合，用于记录哪些层级已被跳过
 *
 * 使用场景：
 *   1. 校验链各环节之间传递数据
 *   2. 记录校验过程中的状态
 *   3. 存储白名单使用情况
 *
 * @see WhiteListConfig 白名单配置
 */
@Data
public class InvokeContext {

    /**
     * 请求对象
     * 承载业务请求的数据，在整个校验流程中传递
     */
    private Object request;

    /**
     * 校验是否通过
     * 初始值为true，任何一个校验环节失败则置为false
     */
    private boolean pass = true;

    /**
     * 错误信息
     * 当pass=false时，存储具体的错误描述
     */
    private String errMsg;

    /**
     * 白名单配置
     * 可为null，null表示不使用白名单机制
     */
    private WhiteListConfig whiteList;

    /**
     * 已消耗的白名单层级集合
     * 用于记录哪些校验层级已被白名单跳过
     * 避免同一层级被重复跳过
     */
    private Set<Integer> consumedLevels = new HashSet<>();
}
