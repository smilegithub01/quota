package com.bank.quota.core.repository;

import com.bank.quota.core.domain.ApprovalQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalQuotaRepository extends JpaRepository<ApprovalQuota, Long> {
    
    Optional<ApprovalQuota> findByApprovalNo(String approvalNo);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApprovalQuota a WHERE a.approvalNo = :approvalNo")
    Optional<ApprovalQuota> findByApprovalNoWithLock(@Param("approvalNo") String approvalNo);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApprovalQuota a WHERE a.id = :id")
    Optional<ApprovalQuota> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT a FROM ApprovalQuota a WHERE a.customerQuotaSubId = :customerQuotaSubId")
    List<ApprovalQuota> findByCustomerQuotaSubId(@Param("customerQuotaSubId") Long customerQuotaSubId);
    
    @Query("SELECT a FROM ApprovalQuota a WHERE a.status = 'ENABLED'")
    List<ApprovalQuota> findAllEnabled();
    
    @Query("SELECT a FROM ApprovalQuota a WHERE a.customerQuotaSubId = :customerQuotaSubId AND a.status = 'ENABLED'")
    List<ApprovalQuota> findEnabledByCustomerQuotaSubId(@Param("customerQuotaSubId") Long customerQuotaSubId);
    
    @Query("SELECT a FROM ApprovalQuota a WHERE a.approvalType = :approvalType")
    List<ApprovalQuota> findByApprovalType(@Param("approvalType") String approvalType);
}
