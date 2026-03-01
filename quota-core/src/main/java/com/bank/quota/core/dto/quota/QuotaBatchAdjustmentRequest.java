package com.bank.quota.core.dto.quota;

import com.bank.quota.core.enums.AdjustmentType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * 额度批量调整请求
 * 
 * <p>用于发起额度批量调整的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaBatchAdjustmentRequest {
    /**
     * 批量调整名称
     */
    private String batchName;
    
    /**
     * 调整类型
     */
    private AdjustmentType adjustmentType;
    
    /**
     * 调整明细列表
     */
    private List<QuotaBatchAdjustmentItem> adjustmentItems;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 执行者ID
     */
    private String executorId;
    
    /**
     * 执行者姓名
     */
    private String executorName;
    
    /**
     * 单个调整项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotaBatchAdjustmentItem {
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
        private String quotaType; // CUSTOMER, GROUP, ITEM等
        
        /**
         * 调整金额
         */
        private BigDecimal adjustmentAmount;
        
        /**
         * 货币类型
         */
        private String currency;
    }
}