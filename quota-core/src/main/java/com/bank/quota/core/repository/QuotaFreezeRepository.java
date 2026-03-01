package com.bank.quota.core.repository;

import com.bank.quota.core.domain.QuotaFreeze;
import com.bank.quota.core.enums.FreezeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaFreezeRepository extends JpaRepository<QuotaFreeze, Long> {
    
    Optional<QuotaFreeze> findByFreezeNo(String freezeNo);
    
    List<QuotaFreeze> findByQuotaIdOrderByFreezeTimeDesc(Long quotaId);
    
    List<QuotaFreeze> findByQuotaIdAndStatus(Long quotaId, FreezeStatus status);
    
    List<QuotaFreeze> findByStatus(FreezeStatus status);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT qf FROM QuotaFreeze qf WHERE qf.freezeNo = :freezeNo")
    Optional<QuotaFreeze> findByFreezeNoWithLock(@Param("freezeNo") String freezeNo);
    
    @Query("SELECT qf FROM QuotaFreeze qf WHERE qf.quotaId = :quotaId AND qf.status = 'FROZEN'")
    List<QuotaFreeze> findActiveFreezesByQuotaId(@Param("quotaId") Long quotaId);
    
    @Query("SELECT SUM(qf.freezeAmount) FROM QuotaFreeze qf WHERE qf.quotaId = :quotaId AND qf.status = 'FROZEN'")
    Optional<java.math.BigDecimal> sumFreezeAmountByQuotaId(@Param("quotaId") Long quotaId);
}
