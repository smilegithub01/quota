package com.bank.quota.core.repository;

import com.bank.quota.core.domain.UsageApplication;
import com.bank.quota.core.enums.UsageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用信申请仓储接口
 * 
 * <p>提供对用信申请数据访问的操作接口。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Repository
public interface UsageApplicationRepository extends JpaRepository<UsageApplication, Long> {
    
    /**
     * 根据客户ID查询用信申请列表
     * 
     * @param customerId 客户ID
     * @return 用信申请列表
     */
    List<UsageApplication> findByCustomerId(Long customerId);
    
    /**
     * 根据状态查询用信申请列表
     * 
     * @param status 状态
     * @return 用信申请列表
     */
    List<UsageApplication> findByStatus(UsageStatus status);
    
    /**
     * 根据状态列表查询用信申请列表
     * 
     * @param statuses 状态列表
     * @return 用信申请列表
     */
    List<UsageApplication> findByStatusIn(List<UsageStatus> statuses);
    
    /**
     * 根据客户ID和状态查询用信申请列表
     * 
     * @param customerId 客户ID
     * @param status 状态
     * @return 用信申请列表
     */
    List<UsageApplication> findByCustomerIdAndStatus(Long customerId, UsageStatus status);
    
    /**
     * 根据申请编号查询用信申请
     * 
     * @param applicationNo 申请编号
     * @return 用信申请
     */
    UsageApplication findByApplicationNo(String applicationNo);
    
    /**
     * 统计客户在指定状态下的申请数量
     * 
     * @param customerId 客户ID
     * @param status 状态
     * @return 申请数量
     */
    long countByCustomerIdAndStatus(Long customerId, UsageStatus status);
    
    /**
     * 查询指定客户的特定状态申请
     * 
     * @param customerId 客户ID
     * @param status 状态
     * @return 用信申请列表
     */
    @Query("SELECT ua FROM UsageApplication ua WHERE ua.customerId = :customerId AND ua.status = :status ORDER BY ua.createTime DESC")
    List<UsageApplication> findApplicationsByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                                               @Param("status") UsageStatus status);
    
    /**
     * 查找超时的申请
     * 
     * @param cutoffTime 截止时间
     * @return 超时的申请列表
     */
    @Query("SELECT ua FROM UsageApplication ua WHERE (ua.status = com.bank.quota.core.enums.UsageStatus.SUBMITTED OR ua.status = com.bank.quota.core.enums.UsageStatus.IN_REVIEW) " +
           "AND ua.createTime < :cutoffTime")
    List<UsageApplication> findTimeoutApplications(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);
}