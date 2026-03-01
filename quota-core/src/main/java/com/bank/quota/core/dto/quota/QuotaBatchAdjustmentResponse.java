package com.bank.quota.core.dto.quota;

import com.bank.quota.core.enums.AdjustmentStatus;
import com.bank.quota.core.enums.AdjustmentType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 额度批量调整响应
 * 
 * <p>用于返回额度批量调整结果的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaBatchAdjustmentResponse {
    /**
     * ID
     */
    private Long id;
    
    /**
     * 批量号
     */
    private String batchNo;
    
    /**
     * 批量调整名称
     */
    private String batchName;
    
    /**
     * 调整类型
     */
    private AdjustmentType adjustmentType;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 项目数量
     */
    private Integer itemCount;
    
    /**
     * 已处理数量
     */
    private Integer processedCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failedCount;
    
    /**
     * 状态
     */
    private AdjustmentStatus status;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 执行者ID
     */
    private String executorId;
    
    /**
     * 执行者姓名
     */
    private String executorName;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 错误日志
     */
    private String errorLog;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}