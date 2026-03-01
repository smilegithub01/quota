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
 * 额度批量调整明细响应
 * 
 * <p>用于返回额度批量调整明细结果的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaBatchAdjustmentDetailResponse {
    /**
     * ID
     */
    private Long id;
    
    /**
     * 批量ID
     */
    private Long batchId;
    
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 客户姓名
     */
    private String customerName;
    
    /**
     * 额度类型
     */
    private String quotaType;
    
    /**
     * 调整类型
     */
    private AdjustmentType adjustmentType;
    
    /**
     * 调整金额
     */
    private BigDecimal adjustmentAmount;
    
    /**
     * 调整前金额
     */
    private BigDecimal beforeAmount;
    
    /**
     * 调整后金额
     */
    private BigDecimal afterAmount;
    
    /**
     * 货币类型
     */
    private String currency;
    
    /**
     * 状态
     */
    private AdjustmentStatus status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 处理时间
     */
    private LocalDateTime processTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}