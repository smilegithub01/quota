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
 * 额度使用明细响应
 * 
 * <p>用于返回额度使用明细信息的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaUsageDetailResponse {
    /**
     * ID
     */
    private Long id;
    
    /**
     * 使用明细编号
     */
    private String usageNo;
    
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
     * 使用金额
     */
    private BigDecimal usageAmount;
    
    /**
     * 货币类型
     */
    private String currency;
    
    /**
     * 原始余额
     */
    private BigDecimal originalBalance;
    
    /**
     * 当前余额
     */
    private BigDecimal currentBalance;
    
    /**
     * 变更后余额
     */
    private BigDecimal balanceAfter;
    
    /**
     * 关联ID
     */
    private String relatedId;
    
    /**
     * 关联类型
     */
    private String relatedType;
    
    /**
     * 使用日期
     */
    private LocalDateTime usageDate;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 操作员ID
     */
    private String operatorId;
    
    /**
     * 操作员姓名
     */
    private String operatorName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}