package com.bank.quota.core.dto.compliance;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合规检查请求
 * 
 * <p>用于监管指标校验的请求数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceCheckRequest {
    /**
     * 检查类型
     */
    private String checkType;
    
    /**
     * 检查对象ID
     */
    private Long objectId;
    
    /**
     * 检查对象类型
     */
    private String objectType;
    
    /**
     * 检查值
     */
    private BigDecimal checkValue;
    
    /**
     * 基准值
     */
    private BigDecimal baselineValue;
    
    /**
     * 阈值
     */
    private BigDecimal threshold;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 集团ID
     */
    private Long groupId;
    
    /**
     * 检查时间范围开始
     */
    private LocalDateTime startTime;
    
    /**
     * 检查时间范围结束
     */
    private LocalDateTime endTime;
    
    /**
     * 其他参数
     */
    private String parameters;
}