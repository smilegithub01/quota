package com.bank.quota.core.dto.usage;

import com.bank.quota.core.enums.BusinessType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用信申请请求
 * 
 * <p>用于创建用信申请的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageApplicationRequest {
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 客户姓名
     */
    private String customerName;
    
    /**
     * 业务类型
     */
    private BusinessType businessType;
    
    /**
     * 用信额度
     */
    private BigDecimal usageQuota;
    
    /**
     * 货币类型
     */
    private String currency;
    
    /**
     * 担保类型
     */
    private String guaranteeType;
    
    /**
     * 抵押品价值
     */
    private BigDecimal collateralValue;
    
    /**
     * 期限（月）
     */
    private Integer termMonths;
    
    /**
     * 利率
     */
    private BigDecimal interestRate;
    
    /**
     * 资金用途
     */
    private String purpose;
    
    /**
     * 风险等级
     */
    private String riskLevel;
    
    /**
     * 关联合同ID
     */
    private String contractId;
    
    /**
     * 关联审批ID
     */
    private Long relatedApprovalId;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 申请人
     */
    private String applicant;
}