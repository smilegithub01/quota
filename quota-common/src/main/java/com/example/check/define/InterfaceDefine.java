package com.example.check.define;

import lombok.Data;

import java.util.List;

/**
 * 接口定义 - 描述一个接口的完整执行流程
 *
 * 本类对应XML配置文件中的一个interface节点，定义了接口的执行流程
 *
 * 字段说明：
 *   - name: 接口名称
 *   - paramValidate: 参数校验规则
 *   - commonProcess: 通用处理链（日志、上下文填充等）
 *   - checkChain: 校验链（参数、权限、风控等校验）
 *   - specialProcess: 特殊处理链（白名单处理等）
 *   - bizAction: 业务动作链（订单创建等）
 *
 * @see ParamValidate 参数校验规则
 */
@Data
public class InterfaceDefine {

    /**
     * 接口名称
     */
    private String name;

    /**
     * 参数校验规则
     */
    private ParamValidate paramValidate;

    /**
     * 通用处理链
     * 在校验链之前执行，如日志记录、上下文填充等
     */
    private List<String> commonProcess;

    /**
     * 校验链
     * 包含各类业务校验，按顺序执行
     */
    private List<String> checkChain;

    /**
     * 特殊处理链
     * 在校验链之后、业务动作之前执行，如白名单处理
     */
    private List<String> specialProcess;

    /**
     * 业务动作链
     * 校验通过后执行业务操作，如创建订单
     */
    private List<String> bizAction;
}
