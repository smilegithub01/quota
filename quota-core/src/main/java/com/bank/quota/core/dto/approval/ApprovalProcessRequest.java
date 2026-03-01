package com.bank.quota.core.dto.approval;

import com.bank.quota.core.enums.ApprovalStatus;
import com.bank.quota.core.enums.BusinessType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审批流程请求
 * 
 * <p>用于发起审批流程的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalProcessRequest {
    /**
     * 业务ID
     */
    private Long businessId;
    
    /**
     * 业务类型
     */
    private BusinessType businessType;
    
    /**
     * 业务名称
     */
    private String businessName;
    
    /**
     * 申请人ID
     */
    private String applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 货币类型
     */
    private String currency;
    
    /**
     * 预计审批天数
     */
    private Integer estimatedDurationDays;
    
    /**
     * 描述
     */
    private String description;
}