package com.bank.quota.core.dto.quota;

import com.bank.quota.core.enums.BusinessType;
import com.bank.quota.core.enums.QuotaUsageType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 额度使用明细查询请求
 * 
 * <p>用于查询额度使用明细的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaUsageDetailQueryRequest {
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 客户姓名
     */
    private String customerName;
    
    /**
     * 集团ID
     */
    private Long groupId;
    
    /**
     * 集团名称
     */
    private String groupName;
    
    /**
     * 业务类型
     */
    private BusinessType businessType;
    
    /**
     * 使用类型
     */
    private QuotaUsageType usageType;
    
    /**
     * 关联ID
     */
    private String relatedId;
    
    /**
     * 关联类型
     */
    private String relatedType;
    
    /**
     * 操作员ID
     */
    private String operatorId;
    
    /**
     * 操作员姓名
     */
    private String operatorName;
    
    /**
     * 开始日期
     */
    private LocalDateTime startDate;
    
    /**
     * 结束日期
     */
    private LocalDateTime endDate;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页面大小
     */
    private Integer pageSize = 10;
}