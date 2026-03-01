package com.bank.quota.common.chain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 参数校验处理器
 * 
 * 校验处理器的一种，用于验证请求参数是否符合业务规则
 * 这是金融系统中必不可少的安全控制措施，确保传入的数据有效且安全
 * 
 * 设计说明：
 * - 参数校验是防止无效数据进入系统的第一道防线
 * - 支持多种校验规则：非空校验、数值范围校验、字符串长度校验、正则表达式校验等
 * - 可以配置不同请求类型的不同校验规则
 * - 校验失败时返回详细的错误信息，便于前端提示用户
 */
public class ParameterValidationHandler extends AbstractHandler {
    
    /**
     * 参数校验规则映射
     * key: 参数名
     * value: 校验规则
     */
    private final Map<String, ValidationRule> validationRules;
    
    /**
     * 必需的参数集合
     */
    private final Map<String, Boolean> requiredParams;
    
    /**
     * 校验规则枚举
     */
    public enum ValidationType {
        NOT_NULL,           // 非空
        NOT_EMPTY,          // 非空字符串
        NUMERIC,            // 数字
        POSITIVE,           // 正数
        NON_NEGATIVE,       // 非负数
        EMAIL,              // 邮箱格式
        PHONE,              // 手机号格式
        ID_CARD,            // 身份证号
        REGEX               // 自定义正则
    }
    
    /**
     * 校验规则定义
     */
    public static class ValidationRule {
        private ValidationType type;
        private String regexPattern;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private Integer minLength;
        private Integer maxLength;
        private String errorMessage;
        
        public ValidationRule(ValidationType type) {
            this.type = type;
            this.errorMessage = "参数校验失败";
        }
        
        public ValidationRule(ValidationType type, String errorMessage) {
            this.type = type;
            this.errorMessage = errorMessage;
        }
        
        // Getters and Setters
        public ValidationType getType() { return type; }
        public void setType(ValidationType type) { this.type = type; }
        public String getRegexPattern() { return regexPattern; }
        public void setRegexPattern(String regexPattern) { this.regexPattern = regexPattern; }
        public BigDecimal getMinValue() { return minValue; }
        public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
        public BigDecimal getMaxValue() { return maxValue; }
        public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
        public Integer getMinLength() { return minLength; }
        public void setMinLength(Integer minLength) { this.minLength = minLength; }
        public Integer getMaxLength() { return maxLength; }
        public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public static ValidationRule notNull(String errorMessage) {
            return new ValidationRule(ValidationType.NOT_NULL, errorMessage);
        }
        
        public static ValidationRule numeric() {
            return new ValidationRule(ValidationType.NUMERIC, "参数必须为数字");
        }
        
        public static ValidationRule positive(String errorMessage) {
            return new ValidationRule(ValidationType.POSITIVE, errorMessage);
        }
        
        public static ValidationRule range(BigDecimal min, BigDecimal max, String errorMessage) {
            ValidationRule rule = new ValidationRule(ValidationType.NUMERIC, errorMessage);
            rule.minValue = min;
            rule.maxValue = max;
            return rule;
        }
        
        public static ValidationRule email() {
            return new ValidationRule(ValidationType.EMAIL, "参数必须为有效的邮箱格式");
        }
        
        public static ValidationRule phone() {
            return new ValidationRule(ValidationType.PHONE, "参数必须为有效的手机号格式");
        }
        
        public static ValidationRule regex(String pattern, String errorMessage) {
            ValidationRule rule = new ValidationRule(ValidationType.REGEX, errorMessage);
            rule.regexPattern = pattern;
            return rule;
        }
        
        public static ValidationRule length(int min, int max, String errorMessage) {
            ValidationRule rule = new ValidationRule(ValidationType.NOT_EMPTY, errorMessage);
            rule.minLength = min;
            rule.maxLength = max;
            return rule;
        }
    }
    
    /**
     * 构造函数
     */
    public ParameterValidationHandler() {
        super("参数校验处理器");
        this.validationRules = new HashMap<>();
        this.requiredParams = new HashMap<>();
    }
    
    /**
     * 添加必需参数
     */
    public void addRequiredParam(String paramName) {
        addRequiredParam(paramName, true);
    }
    
    /**
     * 添加必需参数
     */
    public void addRequiredParam(String paramName, boolean required) {
        requiredParams.put(paramName, required);
    }
    
    /**
     * 添加校验规则
     */
    public void addValidationRule(String paramName, ValidationRule rule) {
        validationRules.put(paramName, rule);
    }
    
    /**
     * 实际的处理逻辑
     * 
     * 对请求参数进行校验：
     * 1. 首先检查必需参数是否存在
     * 2. 然后对每个有校验规则的参数进行规则校验
     * 3. 校验失败返回REJECTED，全部通过返回PASSED
     */
    @Override
    protected HandleResult doHandle(RequestContext context) {
        Map<String, Object> params = context.getParams();
        
        // 1. 检查必需参数
        for (Map.Entry<String, Boolean> entry : requiredParams.entrySet()) {
            String paramName = entry.getKey();
            Boolean required = entry.getValue();
            
            if (required && (!params.containsKey(paramName) || params.get(paramName) == null)) {
                context.setMessage("必需参数 [" + paramName + "] 缺失");
                return HandleResult.REJECTED;
            }
        }
        
        // 2. 执行参数校验
        for (Map.Entry<String, ValidationRule> entry : validationRules.entrySet()) {
            String paramName = entry.getKey();
            ValidationRule rule = entry.getValue();
            Object value = params.get(paramName);
            
            // 如果参数不存在且不是必需的，跳过校验
            if (value == null) {
                continue;
            }
            
            // 执行校验
            String errorMsg = validateValue(paramName, value, rule);
            if (errorMsg != null) {
                context.setMessage(errorMsg);
                return HandleResult.REJECTED;
            }
        }
        
        context.setMessage("参数校验通过");
        return HandleResult.PASSED;
    }
    
    /**
     * 校验单个值
     */
    private String validateValue(String paramName, Object value, ValidationRule rule) {
        switch (rule.getType()) {
            case NOT_NULL:
                if (value == null) {
                    return rule.getErrorMessage();
                }
                break;
                
            case NOT_EMPTY:
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.trim().isEmpty()) {
                        return rule.getErrorMessage();
                    }
                }
                break;
                
            case NUMERIC:
                try {
                    new BigDecimal(value.toString());
                } catch (NumberFormatException e) {
                    return rule.getErrorMessage();
                }
                // 范围校验
                BigDecimal numValue = new BigDecimal(value.toString());
                if (rule.getMinValue() != null && numValue.compareTo(rule.getMinValue()) < 0) {
                    return rule.getErrorMessage();
                }
                if (rule.getMaxValue() != null && numValue.compareTo(rule.getMaxValue()) > 0) {
                    return rule.getErrorMessage();
                }
                break;
                
            case POSITIVE:
                try {
                    BigDecimal positiveValue = new BigDecimal(value.toString());
                    if (positiveValue.compareTo(BigDecimal.ZERO) <= 0) {
                        return rule.getErrorMessage();
                    }
                } catch (NumberFormatException e) {
                    return rule.getErrorMessage();
                }
                break;
                
            case NON_NEGATIVE:
                try {
                    BigDecimal nonNegValue = new BigDecimal(value.toString());
                    if (nonNegValue.compareTo(BigDecimal.ZERO) < 0) {
                        return rule.getErrorMessage();
                    }
                } catch (NumberFormatException e) {
                    return rule.getErrorMessage();
                }
                break;
                
            case EMAIL:
                if (value instanceof String) {
                    String email = (String) value;
                    if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email)) {
                        return rule.getErrorMessage();
                    }
                }
                break;
                
            case PHONE:
                if (value instanceof String) {
                    String phone = (String) value;
                    if (!Pattern.matches("^1[3-9]\\d{9}$", phone)) {
                        return rule.getErrorMessage();
                    }
                }
                break;
                
            case ID_CARD:
                if (value instanceof String) {
                    String idCard = (String) value;
                    if (!Pattern.matches("^\\d{15}|\\d{18}$", idCard)) {
                        return rule.getErrorMessage();
                    }
                }
                break;
                
            case REGEX:
                if (value instanceof String && rule.getRegexPattern() != null) {
                    String strValue = (String) value;
                    if (!Pattern.matches(rule.getRegexPattern(), strValue)) {
                        return rule.getErrorMessage();
                    }
                }
                break;
        }
        
        return null;
    }
}
