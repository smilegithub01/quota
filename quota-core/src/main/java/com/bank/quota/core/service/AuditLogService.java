package com.bank.quota.core.service;

import com.bank.quota.core.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {
    
    AuditLog logOperation(AuditLog.OperationType operationType,
                          AuditLog.AuditObjectType objectType,
                          String objectId,
                          String operationDesc,
                          String operator,
                          String status);
    
    AuditLog logOperation(AuditLog.OperationType operationType,
                          AuditLog.AuditObjectType objectType,
                          String objectId,
                          String operationDesc,
                          String operator,
                          String status,
                          String beforeValue,
                          String afterValue);
    
    AuditLog logOperation(AuditLog.OperationType operationType,
                          AuditLog.AuditObjectType objectType,
                          String objectId,
                          String operationDesc,
                          String operator,
                          String status,
                          String errorMessage);
    
    List<AuditLog> getAuditLogsByObjectId(String objectId);
    
    List<AuditLog> getAuditLogsByOperatorAndTimeRange(String operator, 
                                                        LocalDateTime startTime, 
                                                        LocalDateTime endTime);
    
    void asyncLogOperation(AuditLog.OperationType operationType,
                           AuditLog.AuditObjectType objectType,
                           String objectId,
                           String operationDesc,
                           String operator,
                           String status);

    Page<AuditLog> queryAuditLogs(String operationType, String objectType, String objectId, String operator, String status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}
