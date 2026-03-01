package com.bank.quota.core.dto.approval;

import com.bank.quota.core.enums.ApprovalStatus;
import com.bank.quota.core.enums.BusinessType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批流程响应
 * 
 * <p>用于返回审批流程信息的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalProcessResponse {
    /**
     * 流程ID
     */
    private Long id;
    
    /**
     * 流程编号
     */
    private String processNo;
    
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
     * 当前节点ID
     */
    private Long currentNodeId;
    
    /**
     * 当前审批人ID
     */
    private String currentApproverId;
    
    /**
     * 当前审批人姓名
     */
    private String currentApproverName;
    
    /**
     * 状态
     */
    private ApprovalStatus status;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 预计审批天数
     */
    private Integer estimatedDurationDays;
    
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
    
    /**
     * 审批节点列表
     */
    private List<ApprovalNodeResponse> approvalNodes;
}