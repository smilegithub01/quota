package com.bank.whitelist.core;

import com.bank.whitelist.common.WhitelistRule.RuleType;

import java.util.HashMap;
import java.util.Map;

/**
 * 校验上下文
 * 
 * 提供待校验的值，根据规则类型自动匹配
 */
public class CheckContext {
    
    /**
     * 原始数据
     */
    private final Map<String, Object> rawData;
    
    /**
     * 校验值映射 key: RuleType -> value
     */
    private final Map<RuleType, Object> valueMap;
    
    public CheckContext() {
        this.rawData = new HashMap<>();
        this.valueMap = new HashMap<>();
    }
    
    public CheckContext(Map<String, Object> data) {
        this.rawData = new HashMap<>(data);
        this.valueMap = new HashMap<>();
    }
    
    /**
     * 设置校验值
     */
    public CheckContext setValue(RuleType ruleType, Object value) {
        valueMap.put(ruleType, value);
        return this;
    }
    
    /**
     * 设置多个校验值
     */
    public CheckContext setValues(Map<RuleType, Object> values) {
        if (values != null) {
            valueMap.putAll(values);
        }
        return this;
    }
    
    /**
     * 获取校验值
     */
    public Object getValue(RuleType ruleType) {
        return valueMap.get(ruleType);
    }
    
    /**
     * 获取校验值（字符串形式）
     */
    public String getStringValue(RuleType ruleType) {
        Object value = valueMap.get(ruleType);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 便捷方法：设置IP
     */
    public CheckContext setIp(String ip) {
        return setValue(RuleType.IP, ip);
    }
    
    /**
     * 便捷方法：设置用户ID
     */
    public CheckContext setUserId(String userId) {
        return setValue(RuleType.USER_ID, userId);
    }
    
    /**
     * 便捷方法：设置用户名
     */
    public CheckContext setUsername(String username) {
        return setValue(RuleType.USERNAME, username);
    }
    
    /**
     * 便捷方法：设置设备ID
     */
    public CheckContext setDeviceId(String deviceId) {
        return setValue(RuleType.DEVICE_ID, deviceId);
    }
    
    /**
     * 便捷方法：设置手机号
     */
    public CheckContext setPhone(String phone) {
        return setValue(RuleType.PHONE, phone);
    }
    
    /**
     * 便捷方法：设置邮箱
     */
    public CheckContext setEmail(String email) {
        return setValue(RuleType.EMAIL, email);
    }
    
    /**
     * 便捷方法：设置API编码
     */
    public CheckContext setApiCode(String apiCode) {
        return setValue(RuleType.API_CODE, apiCode);
    }
    
    /**
     * 便捷方法：设置业务编码
     */
    public CheckContext setBizCode(String bizCode) {
        return setValue(RuleType.BIZ_CODE, bizCode);
    }
    
    /**
     * 添加原始数据
     */
    public CheckContext addRawData(String key, Object value) {
        rawData.put(key, value);
        return this;
    }
    
    /**
     * 获取原始数据
     */
    public Object getRawData(String key) {
        return rawData.get(key);
    }
    
    /**
     * 获取所有原始数据
     */
    public Map<String, Object> getAllRawData() {
        return new HashMap<>(rawData);
    }
    
    /**
     * 获取所有校验值
     */
    public Map<RuleType, Object> getAllValues() {
        return new HashMap<>(valueMap);
    }
    
    @Override
    public String toString() {
        return "CheckContext{" +
                "valueMap=" + valueMap +
                ", rawData=" + rawData +
                '}';
    }
}
