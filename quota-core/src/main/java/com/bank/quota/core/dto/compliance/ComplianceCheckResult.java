package com.bank.quota.core.dto.compliance;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 合规检查结果
 * 
 * <p>用于监管指标校验的响应数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceCheckResult {
    /**
     * 检查是否通过
     */
    private boolean passed;
    
    /**
     * 检查结果代码
     */
    private String resultCode;
    
    /**
     * 检查结果消息
     */
    private String message;
    
    /**
     * 检查值
     */
    private BigDecimal checkValue;
    
    /**
     * 阈值
     */
    private BigDecimal threshold;
    
    /**
     * 基准值
     */
    private BigDecimal baselineValue;
    
    /**
     * 实际计算值
     */
    private BigDecimal actualValue;
    
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 检查类型
     */
    private String checkType;
    
    /**
     * 检查详情
     */
    private String details;
    
    /**
     * 违规详情列表
     */
    private List<ViolationDetail> violations;
    
    /**
     * 附加数据
     */
    private Map<String, Object> additionalData;
    
    /**
     * 违规详情内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViolationDetail {
        private String violationCode;
        private String violationDescription;
        private String severity; // INFO, WARNING, ERROR
        private BigDecimal currentValue;
        private BigDecimal thresholdValue;
        private String suggestion;
    }
}