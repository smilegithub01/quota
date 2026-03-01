package com.bank.quota.core.repository;

import com.bank.quota.core.domain.QuotaAdjustment;
import com.bank.quota.core.enums.AdjustmentStatus;
import com.bank.quota.core.enums.QuotaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaAdjustmentRepository extends JpaRepository<QuotaAdjustment, Long> {
    
    Optional<QuotaAdjustment> findByAdjustmentNo(String adjustmentNo);
    
    List<QuotaAdjustment> findByQuotaIdOrderByCreateTimeDesc(Long quotaId);
    
    List<QuotaAdjustment> findByQuotaTypeAndStatus(
        QuotaType quotaType,
        AdjustmentStatus status
    );
    
    List<QuotaAdjustment> findByApplicant(String applicant);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT qa FROM QuotaAdjustment qa WHERE qa.adjustmentNo = :adjustmentNo")
    Optional<QuotaAdjustment> findByAdjustmentNoWithLock(@Param("adjustmentNo") String adjustmentNo);
    
    @Query("SELECT COUNT(qa) FROM QuotaAdjustment qa WHERE qa.quotaId = :quotaId AND qa.status = :status")
    long countByQuotaIdAndStatus(@Param("quotaId") Long quotaId, @Param("status") AdjustmentStatus status);
}
