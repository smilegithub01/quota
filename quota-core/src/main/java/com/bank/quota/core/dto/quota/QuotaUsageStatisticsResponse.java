package com.bank.quota.core.dto.quota;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 额度使用统计响应
 * 
 * <p>用于返回额度使用统计数据的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaUsageStatisticsResponse {
    /**
     * 统计期间开始时间
     */
    private LocalDateTime startDate;
    
    /**
     * 统计期间结束时间
     */
    private LocalDateTime endDate;
    
    /**
     * 总使用笔数
     */
    private Long totalUsageCount;
    
    /**
     * 总使用金额
     */
    private BigDecimal totalUsageAmount;
    
    /**
     * 总占用金额
     */
    private BigDecimal totalOccupyAmount;
    
    /**
     * 总释放金额
     */
    private BigDecimal totalReleaseAmount;
    
    /**
     * 总调整金额
     */
    private BigDecimal totalAdjustAmount;
    
    /**
     * 按使用类型统计的详情
     */
    private List<UsageTypeSummary> usageTypeSummaries;
    
    /**
     * 按客户统计的详情
     */
    private List<CustomerUsageSummary> customerSummaries;
    
    /**
     * 使用类型摘要
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageTypeSummary {
        private String usageType;
        private Long count;
        private BigDecimal amount;
    }
    
    /**
     * 客户使用摘要
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerUsageSummary {
        private Long customerId;
        private String customerName;
        private Long count;
        private BigDecimal amount;
    }
}