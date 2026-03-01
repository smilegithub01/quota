package com.bank.quota.core.repository;

import com.bank.quota.core.domain.CreditApplication;
import com.bank.quota.core.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 授信申请仓储接口
 * 
 * <p>提供对授信申请数据访问的操作接口。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Repository
public interface CreditApplicationRepository extends JpaRepository<CreditApplication, Long> {
    
    /**
     * 根据客户ID查询授信申请列表
     * 
     * @param customerId 客户ID
     * @return 授信申请列表
     */
    List<CreditApplication> findByCustomerId(Long customerId);
    
    /**
     * 根据状态查询授信申请列表
     * 
     * @param status 状态
     * @return 授信申请列表
     */
    List<CreditApplication> findByStatus(ApprovalStatus status);
    
    /**
     * 根据状态列表查询授信申请列表
     * 
     * @param statuses 状态列表
     * @return 授信申请列表
     */
    List<CreditApplication> findByStatusIn(List<ApprovalStatus> statuses);
    
    /**
     * 根据客户ID和状态查询授信申请列表
     * 
     * @param customerId 客户ID
     * @param status 状态
     * @return 授信申请列表
     */
    List<CreditApplication> findByCustomerIdAndStatus(Long customerId, ApprovalStatus status);
    
    /**
     * 根据申请编号查询授信申请
     * 
     * @param applicationNo 申请编号
     * @return 授信申请
     */
    CreditApplication findByApplicationNo(String applicationNo);
    
    /**
     * 统计客户在指定状态下的申请数量
     * 
     * @param customerId 客户ID
     * @param status 状态
     * @return 申请数量
     */
    long countByCustomerIdAndStatus(Long customerId, ApprovalStatus status);
    
    /**
     * 查询指定客户的特定状态申请
     * 
     * @param customerId 客户ID
     * @param status 状态
     * @return 授信申请列表
     */
    @Query("SELECT ca FROM CreditApplication ca WHERE ca.customerId = :customerId AND ca.status = :status ORDER BY ca.createTime DESC")
    List<CreditApplication> findApplicationsByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                                                 @Param("status") ApprovalStatus status);
    
    /**
     * 查找超时的申请
     * 
     * @param cutoffTime 截止时间
     * @return 超时的申请列表
     */
    @Query("SELECT ca FROM CreditApplication ca WHERE (ca.status = com.bank.quota.core.enums.ApprovalStatus.SUBMITTED OR ca.status = com.bank.quota.core.enums.ApprovalStatus.UNDER_REVIEW) " +
           "AND ca.createTime < :cutoffTime")
    List<CreditApplication> findTimeoutApplications(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);
}