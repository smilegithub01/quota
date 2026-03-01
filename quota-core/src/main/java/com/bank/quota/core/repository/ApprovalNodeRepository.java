package com.bank.quota.core.repository;

import com.bank.quota.core.domain.ApprovalNode;
import com.bank.quota.core.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批节点仓储接口
 * 
 * <p>提供对审批节点数据访问的操作接口。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Repository
public interface ApprovalNodeRepository extends JpaRepository<ApprovalNode, Long> {
    
    /**
     * 根据流程ID查询审批节点列表
     * 
     * @param processId 流程ID
     * @return 审批节点列表
     */
    List<ApprovalNode> findByProcessId(Long processId);
    
    /**
     * 根据流程ID查询审批节点列表，按序号排序
     * 
     * @param processId 流程ID
     * @return 审批节点列表
     */
    List<ApprovalNode> findByProcessIdOrderByNodeSequenceAsc(Long processId);
    
    /**
     * 根据审批人ID查询审批节点列表
     * 
     * @param approverId 审批人ID
     * @return 审批节点列表
     */
    List<ApprovalNode> findByApproverId(String approverId);
    
    /**
     * 根据状态查询审批节点列表
     * 
     * @param status 状态
     * @return 审批节点列表
     */
    List<ApprovalNode> findByStatus(ApprovalStatus status);
    
    /**
     * 根据流程ID和状态查询审批节点
     * 
     * @param processId 流程ID
     * @param status 状态
     * @return 审批节点列表
     */
    List<ApprovalNode> findByProcessIdAndStatus(Long processId, ApprovalStatus status);
    
    /**
     * 查询指定审批人的待处理节点
     * 
     * @param approverId 审批人ID
     * @param statuses 状态列表
     * @return 审批节点列表
     */
    List<ApprovalNode> findByApproverIdAndStatusIn(String approverId, List<ApprovalStatus> statuses);
    
    /**
     * 根据流程ID和节点序号查询审批节点
     * 
     * @param processId 流程ID
     * @param nodeSequence 节点序号
     * @return 审批节点
     */
    ApprovalNode findByProcessIdAndNodeSequence(Long processId, Integer nodeSequence);
    
    /**
     * 查询指定审批人在指定流程中的节点
     * 
     * @param processId 流程ID
     * @param approverId 审批人ID
     * @return 审批节点列表
     */
    List<ApprovalNode> findByProcessIdAndApproverId(Long processId, String approverId);
}