package com.bank.quota.core.repository;

import com.bank.quota.core.domain.GroupQuota;
import com.bank.quota.core.domain.GroupQuotaSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupQuotaSubRepository extends JpaRepository<GroupQuotaSub, Long> {
    
    Optional<GroupQuotaSub> findByGroupQuotaIdAndSubType(Long groupQuotaId, String subType);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GroupQuotaSub g WHERE g.id = :id")
    Optional<GroupQuotaSub> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT g FROM GroupQuotaSub g WHERE g.groupQuotaId = :groupQuotaId")
    List<GroupQuotaSub> findByGroupQuotaId(@Param("groupQuotaId") Long groupQuotaId);
    
    @Query("SELECT g FROM GroupQuotaSub g WHERE g.status = 'ENABLED'")
    List<GroupQuotaSub> findAllEnabled();
}
