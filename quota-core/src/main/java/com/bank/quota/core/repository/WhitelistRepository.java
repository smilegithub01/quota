package com.bank.quota.core.repository;

import com.bank.quota.core.domain.Whitelist;
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
public interface WhitelistRepository extends JpaRepository<Whitelist, Long> {
    
    Optional<Whitelist> findByWhitelistNo(String whitelistNo);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Whitelist w WHERE w.whitelistNo = :whitelistNo")
    Optional<Whitelist> findByWhitelistNoWithLock(@Param("whitelistNo") String whitelistNo);
    
    @Query("SELECT w FROM Whitelist w WHERE w.customerId = :customerId AND w.status = 'ACTIVE' " +
           "AND w.effectiveTime <= :now AND w.expiryTime > :now")
    List<Whitelist> findActiveByCustomerId(@Param("customerId") Long customerId, @Param("now") LocalDateTime now);
    
    @Query("SELECT w FROM Whitelist w WHERE w.customerId = :customerId AND w.businessType = :businessType " +
           "AND w.status = 'ACTIVE' AND w.effectiveTime <= :now AND w.expiryTime > :now")
    List<Whitelist> findActiveByCustomerIdAndBusinessType(@Param("customerId") Long customerId,
                                                          @Param("businessType") String businessType,
                                                          @Param("now") LocalDateTime now);
    
    @Query("SELECT w FROM Whitelist w WHERE w.status = :status")
    List<Whitelist> findByStatus(@Param("status") Whitelist.WhitelistStatus status);
    
    @Query("SELECT w FROM Whitelist w WHERE w.applicant = :applicant")
    List<Whitelist> findByApplicant(@Param("applicant") String applicant);
    
    @Query("SELECT w FROM Whitelist w WHERE w.effectiveTime <= :now AND w.expiryTime > :now " +
           "AND w.status = 'ACTIVE'")
    List<Whitelist> findAllActive(@Param("now") LocalDateTime now);
    
    @Query("SELECT w FROM Whitelist w WHERE w.status = 'PENDING'")
    List<Whitelist> findAllPending();
    
    @Query("SELECT w FROM Whitelist w WHERE w.expiryTime <= :now AND w.status = 'ACTIVE'")
    List<Whitelist> findExpiredWhitelists(@Param("now") LocalDateTime now);
    
    boolean existsByCustomerIdAndStatusAndEffectiveTimeBeforeAndExpiryTimeAfter(
            Long customerId, Whitelist.WhitelistStatus status, 
            LocalDateTime effectiveTime, LocalDateTime expiryTime);
}
