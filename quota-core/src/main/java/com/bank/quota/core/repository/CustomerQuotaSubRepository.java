package com.bank.quota.core.repository;

import com.bank.quota.core.domain.CustomerQuotaSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerQuotaSubRepository extends JpaRepository<CustomerQuotaSub, Long> {
    
    Optional<CustomerQuotaSub> findByCustomerQuotaIdAndSubType(Long customerQuotaId, String subType);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CustomerQuotaSub c WHERE c.id = :id")
    Optional<CustomerQuotaSub> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT c FROM CustomerQuotaSub c WHERE c.customerQuotaId = :customerQuotaId")
    List<CustomerQuotaSub> findByCustomerQuotaId(@Param("customerQuotaId") Long customerQuotaId);
    
    @Query("SELECT c FROM CustomerQuotaSub c WHERE c.status = 'ENABLED'")
    List<CustomerQuotaSub> findAllEnabled();
}
