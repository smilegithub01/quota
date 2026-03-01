package com.bank.quota.core.repository;

import com.bank.quota.core.domain.QuotaTermination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaTerminationRepository extends JpaRepository<QuotaTermination, Long> {
    
    Optional<QuotaTermination> findByTerminationNo(String terminationNo);
    
    List<QuotaTermination> findByQuotaIdOrderByTerminateTimeDesc(Long quotaId);
    
    List<QuotaTermination> findByQuotaType(QuotaTermination.QuotaType quotaType);
    
    List<QuotaTermination> findByTerminateType(QuotaTermination.TerminateType terminateType);
    
    @Query("SELECT qt FROM QuotaTermination qt WHERE qt.terminateTime BETWEEN :startTime AND :endTime")
    List<QuotaTermination> findByTerminateTimeBetween(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime
    );
    
    boolean existsByQuotaId(Long quotaId);
}
