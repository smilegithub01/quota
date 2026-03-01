package com.bank.quota.core.dto.approval;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 审批任务查询请求
 * 
 * <p>用于查询审批任务的数据传输对象。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalTaskQueryRequest {
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
    private String status;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页面大小
     */
    private Integer pageSize = 10;
}