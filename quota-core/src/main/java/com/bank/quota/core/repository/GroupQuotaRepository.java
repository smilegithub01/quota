package com.bank.quota.core.repository;

import com.bank.quota.core.domain.GroupQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupQuotaRepository extends JpaRepository<GroupQuota, Long> {
    
    Optional<GroupQuota> findByGroupId(Long groupId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GroupQuota g WHERE g.groupId = :groupId")
    Optional<GroupQuota> findByGroupIdWithLock(@Param("groupId") Long groupId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GroupQuota g WHERE g.id = :id")
    Optional<GroupQuota> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT g FROM GroupQuota g WHERE g.status = 'ENABLED'")
    List<GroupQuota> findAllEnabled();
    
    @Query("SELECT g FROM GroupQuota g WHERE g.groupId IN :groupIds")
    List<GroupQuota> findByGroupIds(@Param("groupIds") List<Long> groupIds);
}
