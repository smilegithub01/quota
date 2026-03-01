package com.bank.quota.core.repository;

import com.bank.quota.core.domain.ItemQuota;
import com.bank.quota.core.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemQuotaRepository extends JpaRepository<ItemQuota, Long> {
    
    Optional<ItemQuota> findByItemNo(String itemNo);
    
    List<ItemQuota> findByCustomerId(Long customerId);
    
    List<ItemQuota> findByApprovalId(Long approvalId);
    
    List<ItemQuota> findByCustomerIdAndStatus(Long customerId, ItemStatus status);
    
    @Query("SELECT iq FROM ItemQuota iq WHERE iq.customerId = :customerId AND iq.status = 'ACTIVE'")
    List<ItemQuota> findActiveItemsByCustomerId(@Param("customerId") Long customerId);
}
