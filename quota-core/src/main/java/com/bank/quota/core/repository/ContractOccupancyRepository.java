package com.bank.quota.core.repository;

import com.bank.quota.core.domain.ContractOccupancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractOccupancyRepository extends JpaRepository<ContractOccupancy, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContractOccupancy c WHERE c.id = :id")
    Optional<ContractOccupancy> findByIdWithLock(@Param("id") Long id);
    
    Optional<ContractOccupancy> findByContractNo(String contractNo);
    
    List<ContractOccupancy> findByStatus(ContractOccupancy.OccupancyStatus status);
    
    List<ContractOccupancy> findByApprovalQuotaSubId(Long approvalQuotaSubId);
    
    @Query("SELECT c FROM ContractOccupancy c WHERE c.approvalQuotaSubId = :approvalQuotaSubId " +
            "AND c.status = :status")
    List<ContractOccupancy> findOccupiedByApprovalQuotaSubId(
            @Param("approvalQuotaSubId") Long approvalQuotaSubId);
    
    @Query("SELECT COALESCE(SUM(c.occupancyAmount), 0) FROM ContractOccupancy c " +
            "WHERE c.approvalQuotaSubId = :approvalQuotaSubId AND c.status = :status")
    BigDecimal sumOccupancyAmountByApprovalQuotaSubId(
            @Param("approvalQuotaSubId") Long approvalQuotaSubId,
            @Param("status") ContractOccupancy.OccupancyStatus status);
    
    List<ContractOccupancy> findByContractNoAndStatus(String contractNo, ContractOccupancy.OccupancyStatus status);
    
    @Query("SELECT c FROM ContractOccupancy c WHERE c.status = :status AND c.expiryTime < :now")
    List<ContractOccupancy> findExpiredOccupancies(
            @Param("status") ContractOccupancy.OccupancyStatus status,
            @Param("now") LocalDateTime now);
    
    /**
     * 查找超时的占用锁定
     * 
     * @param cutoffTime 截止时间
     * @return 超时的占用记录列表
     */
    @Query("SELECT c FROM ContractOccupancy c WHERE c.status = com.bank.quota.core.domain.ContractOccupancy.OccupancyStatus.OCCUPIED " +
           "AND c.occupyTime < :cutoffTime")
    List<ContractOccupancy> findTimeoutOccupancies(@Param("cutoffTime") LocalDateTime cutoffTime);
}
