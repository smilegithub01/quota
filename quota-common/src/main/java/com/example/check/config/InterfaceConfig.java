package com.example.check.config;

import com.example.check.define.InterfaceDefine;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * 接口配置管理类
 *
 * 负责加载和管理接口流程配置
 *
 * 功能：
 *   - 启动时从XML文件加载接口配置
 *   - 根据接口名称获取对应的配置定义
 *
 * 配置来源：interface-flow.xml
 *
 * @see InterfaceDefine 接口定义
 * @see XmlParser XML解析器
 */
@Component
public class InterfaceConfig {

    /**
     * 接口流程配置映射
     * key: 接口名称
     * value: 接口定义
     */
    private Map<String, InterfaceDefine> flowMap;

    /**
     * 初始化方法
     *
     * Spring容器启动时执行，加载XML配置文件
     */
    @PostConstruct
    public void init() {
        flowMap = XmlParser.parse("interface-flow.xml");
    }

    /**
     * 获取接口定义
     *
     * @param name 接口名称
     * @return 接口定义，如果不存在返回null
     */
    public InterfaceDefine get(String name) {
        return flowMap.get(name);
    }
}
