package com.bank.quota.core.dto.approval;

import com.bank.quota.core.enums.ApprovalStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 审批节点响应
 * 
 * <p>用于返回审批节点信息的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalNodeResponse {
    /**
     * 节点ID
     */
    private Long id;
    
    /**
     * 流程ID
     */
    private Long processId;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 节点序号
     */
    private Integer nodeSequence;
    
    /**
     * 审批人ID
     */
    private String approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 审批人角色
     */
    private String approverRole;
    
    /**
     * 审批人部门
     */
    private String approverDept;
    
    /**
     * 状态
     */
    private ApprovalStatus status;
    
    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;
    
    /**
     * 评论
     */
    private String comments;
    
    /**
     * 结果
     */
    private String result; // APPROVE, REJECT, DELEGATE, RETURN
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}