package com.bank.quota.core.repository;

import com.bank.quota.core.domain.ContractQuota;
import com.bank.quota.core.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractQuotaRepository extends JpaRepository<ContractQuota, Long> {
    
    Optional<ContractQuota> findByContractNo(String contractNo);
    
    List<ContractQuota> findByCustomerId(Long customerId);
    
    List<ContractQuota> findByItemId(Long itemId);
    
    List<ContractQuota> findByCustomerIdAndStatus(Long customerId, ContractStatus status);
    
    @Query("SELECT cq FROM ContractQuota cq WHERE cq.customerId = :customerId AND cq.status = 'ACTIVE'")
    List<ContractQuota> findActiveContractsByCustomerId(@Param("customerId") Long customerId);
}
