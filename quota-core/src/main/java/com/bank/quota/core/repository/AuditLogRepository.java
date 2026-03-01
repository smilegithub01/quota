package com.bank.quota.core.repository;

import com.bank.quota.core.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByOperationType(AuditLog.OperationType operationType, Pageable pageable);
    
    Page<AuditLog> findByObjectTypeAndObjectId(AuditLog.AuditObjectType objectType, 
                                               String objectId, Pageable pageable);
    
    Page<AuditLog> findByOperator(String operator, Pageable pageable);
    
    Page<AuditLog> findByStatus(AuditLog.AuditStatus status, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.createTime BETWEEN :startTime AND :endTime ORDER BY a.createTime DESC")
    Page<AuditLog> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.objectId = :objectId ORDER BY a.createTime DESC")
    List<AuditLog> findByObjectIdOrderByCreateTimeDesc(@Param("objectId") String objectId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.operator = :operator AND a.createTime BETWEEN :startTime AND :endTime")
    List<AuditLog> findByOperatorAndTimeRange(@Param("operator") String operator,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.operationType = :operationType AND a.status = :status")
    Long countByOperationTypeAndStatus(@Param("operationType") AuditLog.OperationType operationType,
                                        @Param("status") AuditLog.AuditStatus status);
    
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:operationType IS NULL OR a.operationType = :operationType) AND " +
           "(:objectType IS NULL OR a.objectType = :objectType) AND " +
           "(:objectId IS NULL OR a.objectId = :objectId) AND " +
           "(:operator IS NULL OR a.operator = :operator) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:startTime IS NULL OR a.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR a.createTime <= :endTime) " +
           "ORDER BY a.createTime DESC")
    Page<AuditLog> findAuditLogs(@Param("operationType") AuditLog.OperationType operationType,
                                 @Param("objectType") AuditLog.AuditObjectType objectType,
                                 @Param("objectId") String objectId,
                                 @Param("operator") String operator,
                                 @Param("status") AuditLog.AuditStatus status,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 Pageable pageable);
}
