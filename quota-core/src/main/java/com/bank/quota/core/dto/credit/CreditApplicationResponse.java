package com.bank.quota.core.dto.credit;

import com.bank.quota.core.enums.ApprovalStatus;
import com.bank.quota.core.enums.BusinessType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 授信申请响应
 * 
 * <p>用于返回授信申请信息的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplicationResponse {
    /**
     * 申请ID
     */
    private Long id;
    
    /**
     * 申请编号
     */
    private String applicationNo;
    
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
     * 申请额度
     */
    private BigDecimal appliedQuota;
    
    /**
     * 批准额度
     */
    private BigDecimal approvedQuota;
    
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
     * 状态
     */
    private ApprovalStatus status;
    
    /**
     * 审核意见
     */
    private String reviewComments;
    
    /**
     * 审批人ID
     */
    private String approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    
    /**
     * 生效日期
     */
    private LocalDateTime effectiveDate;
    
    /**
     * 到期日期
     */
    private LocalDateTime expiryDate;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}