package com.bank.quota.core.dto.usage;

import com.bank.quota.core.enums.UsageStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用信申请审批请求
 * 
 * <p>用于审批用信申请的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageApplicationApprovalRequest {
    /**
     * 申请ID
     */
    private Long applicationId;
    
    /**
     * 审核意见
     */
    private String reviewComments;
    
    /**
     * 审批状态
     */
    private UsageStatus status;
    
    /**
     * 生效日期
     */
    private LocalDateTime effectiveDate;
    
    /**
     * 到期日期
     */
    private LocalDateTime expiryDate;
    
    /**
     * 审批人ID
     */
    private String approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 操作人
     */
    private String operator;
}