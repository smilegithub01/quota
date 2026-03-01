package com.bank.quota.core.repository;

import com.bank.quota.core.domain.CustomerQuota;
import com.bank.quota.core.enums.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerQuotaRepository extends JpaRepository<CustomerQuota, Long> {
    
    Optional<CustomerQuota> findByCustomerId(Long customerId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CustomerQuota c WHERE c.customerId = :customerId")
    Optional<CustomerQuota> findByCustomerIdWithLock(@Param("customerId") Long customerId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CustomerQuota c WHERE c.id = :id")
    Optional<CustomerQuota> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT c FROM CustomerQuota c WHERE c.groupId = :groupId")
    List<CustomerQuota> findByGroupId(@Param("groupId") Long groupId);
    
    @Query("SELECT c FROM CustomerQuota c WHERE c.status = 'ENABLED'")
    List<CustomerQuota> findAllEnabled();
    
    @Query("SELECT c FROM CustomerQuota c WHERE c.groupId = :groupId AND c.status = 'ENABLED'")
    List<CustomerQuota> findEnabledByGroupId(@Param("groupId") Long groupId);
    
    @Query("SELECT c FROM CustomerQuota c WHERE c.customerType = :customerType")
    List<CustomerQuota> findByCustomerType(@Param("customerType") CustomerType customerType);
    
    @Query("SELECT c FROM CustomerQuota c WHERE c.industryCode = :industryCode")
    List<CustomerQuota> findByIndustryCode(@Param("industryCode") String industryCode);
    
    @Query("SELECT c FROM CustomerQuota c WHERE c.regionCode = :regionCode")
    List<CustomerQuota> findByRegionCode(@Param("regionCode") String regionCode);
    
    @Query("SELECT c FROM CustomerQuota c WHERE c.productType = :productType")
    List<CustomerQuota> findByProductType(@Param("productType") String productType);
}
