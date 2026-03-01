package com.bank.quota.core.repository;

import com.bank.quota.core.domain.QuotaLifecycleEvent;
import com.bank.quota.core.enums.EventType;
import com.bank.quota.core.enums.QuotaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaLifecycleEventRepository extends JpaRepository<QuotaLifecycleEvent, Long> {
    
    Optional<QuotaLifecycleEvent> findByEventId(String eventId);
    
    List<QuotaLifecycleEvent> findByQuotaIdOrderByEventTimeDesc(Long quotaId);
    
    List<QuotaLifecycleEvent> findByQuotaIdAndEventType(Long quotaId, EventType eventType);
    
    List<QuotaLifecycleEvent> findByQuotaType(QuotaType quotaType);
    
    @Query("SELECT qle FROM QuotaLifecycleEvent qle WHERE qle.quotaId = :quotaId ORDER BY qle.eventTime DESC")
    List<QuotaLifecycleEvent> findLatestEventsByQuotaId(@Param("quotaId") Long quotaId);
    
    @Query("SELECT qle FROM QuotaLifecycleEvent qle WHERE qle.eventTime BETWEEN :startTime AND :endTime ORDER BY qle.eventTime DESC")
    List<QuotaLifecycleEvent> findByEventTimeBetween(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT COUNT(qle) FROM QuotaLifecycleEvent qle WHERE qle.quotaId = :quotaId AND qle.eventType = :eventType")
    long countByQuotaIdAndEventType(@Param("quotaId") Long quotaId, @Param("eventType") EventType eventType);
}
