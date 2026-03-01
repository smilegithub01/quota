package com.bank.quota.core.dto.approval;

import com.bank.quota.core.enums.ApprovalStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 审批操作请求
 * 
 * <p>用于执行审批操作的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalActionRequest {
    /**
     * 流程ID
     */
    private Long processId;
    
    /**
     * 节点ID
     */
    private Long nodeId;
    
    /**
     * 审批人ID
     */
    private String approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 审批状态
     */
    private ApprovalStatus status;
    
    /**
     * 审批结果
     */
    private String result; // APPROVE, REJECT, DELEGATE, RETURN
    
    /**
     * 评论
     */
    private String comments;
    
    /**
     * 下一节点审批人ID
     */
    private String nextApproverId;
    
    /**
     * 下一节点审批人姓名
     */
    private String nextApproverName;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 操作人
     */
    private String operator;
}