package com.bank.quota.core.repository;

import com.bank.quota.core.domain.ApprovalProcess;
import com.bank.quota.core.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 审批流程仓储接口
 * 
 * <p>提供对审批流程数据访问的操作接口。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Repository
public interface ApprovalProcessRepository extends JpaRepository<ApprovalProcess, Long> {
    
    /**
     * 根据业务ID查询审批流程
     * 
     * @param businessId 业务ID
     * @return 审批流程列表
     */
    List<ApprovalProcess> findByBusinessId(Long businessId);
    
    /**
     * 根据业务类型查询审批流程
     * 
     * @param businessType 业务类型
     * @return 审批流程列表
     */
    List<ApprovalProcess> findByBusinessType(com.bank.quota.core.enums.BusinessType businessType);
    
    /**
     * 根据状态查询审批流程
     * 
     * @param status 状态
     * @return 审批流程列表
     */
    List<ApprovalProcess> findByStatus(ApprovalStatus status);
    
    /**
     * 根据申请人ID查询审批流程
     * 
     * @param applicantId 申请人ID
     * @return 审批流程列表
     */
    List<ApprovalProcess> findByApplicantId(String applicantId);
    
    /**
     * 根据当前审批人ID查询待处理流程
     * 
     * @param currentApproverId 当前审批人ID
     * @return 审批流程列表
     */
    List<ApprovalProcess> findByCurrentApproverIdAndStatusIn(String currentApproverId, List<ApprovalStatus> statuses);
    
    /**
     * 查询指定审批人的待处理任务
     * 
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @return 审批流程列表
     */
    @Query("SELECT ap FROM ApprovalProcess ap JOIN ApprovalNode an ON ap.currentNodeId = an.id WHERE " +
           "(an.approverId = :approverId OR an.approverName = :approverName) AND ap.status IN ('SUBMITTED', 'UNDER_REVIEW')")
    List<ApprovalProcess> findPendingProcessesByApprover(@Param("approverId") String approverId, 
                                                        @Param("approverName") String approverName);
    
    /**
     * 查询指定审批人的历史审批记录
     * 
     * @param approverId 审批人ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 审批流程列表
     */
    @Query("SELECT DISTINCT ap FROM ApprovalProcess ap WHERE ap.status IN ('APPROVED', 'REJECTED') " +
           "AND EXISTS (SELECT 1 FROM ApprovalNode an WHERE an.processId = ap.id AND an.approverId = :approverId)")
    List<ApprovalProcess> findHistoricalApprovalsByApprover(@Param("approverId") String approverId, 
                                                           @Param("offset") int offset, 
                                                           @Param("limit") int limit);
    
    /**
     * 统计审批人的各种状态流程数量
     * 
     * @param approverId 审批人ID
     * @return 状态数量映射
     */
    @Query("SELECT ap.status AS status, COUNT(*) AS count FROM ApprovalProcess ap WHERE " +
           "EXISTS (SELECT 1 FROM ApprovalNode an WHERE an.processId = ap.id AND an.approverId = :approverId) " +
           "GROUP BY ap.status")
    List<Object[]> countProcessesByApproverAndStatus(@Param("approverId") String approverId);
    
    /**
     * 获取审批人的平均处理时间
     * 
     * @param approverId 审批人ID
     * @return 处理时间统计
     */
    @Query("SELECT AVG(ap.endTime - ap.startTime) AS avgProcessingTime, COUNT(*) AS totalProcessed FROM ApprovalProcess ap WHERE " +
           "ap.status IN ('APPROVED', 'REJECTED') AND EXISTS (SELECT 1 FROM ApprovalNode an WHERE an.processId = ap.id AND an.approverId = :approverId)")
    Object[] getAverageProcessingTimeByApprover(@Param("approverId") String approverId);
}