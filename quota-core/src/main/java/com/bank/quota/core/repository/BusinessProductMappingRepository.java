package com.bank.quota.core.repository;

import com.bank.quota.core.domain.BusinessProductMapping;
import com.bank.quota.core.enums.MappingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessProductMappingRepository extends JpaRepository<BusinessProductMapping, Long> {
    
    Optional<BusinessProductMapping> findByBusinessTypeAndProductType(
        String businessType, String productType);
    
    List<BusinessProductMapping> findByBusinessType(String businessType);
    
    List<BusinessProductMapping> findByProductType(String productType);
    
    List<BusinessProductMapping> findByStatus(MappingStatus status);
    
    @Query("SELECT bpm FROM BusinessProductMapping bpm WHERE bpm.businessType = :businessType AND bpm.status = 'ACTIVE'")
    List<BusinessProductMapping> findActiveByBusinessType(@Param("businessType") String businessType);
}
