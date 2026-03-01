package com.bank.quota.core.service.impl;

import com.bank.quota.core.domain.AuditLog;
import com.bank.quota.core.repository.AuditLogRepository;
import com.bank.quota.core.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Override
    @Transactional
    public AuditLog logOperation(AuditLog.OperationType operationType,
                                AuditLog.AuditObjectType objectType,
                                String objectId,
                                String operationDesc,
                                String operator,
                                String status) {
        return logOperation(operationType, objectType, objectId, operationDesc, 
                operator, status, null, null);
    }
    
    @Override
    @Transactional
    public AuditLog logOperation(AuditLog.OperationType operationType,
                                 AuditLog.AuditObjectType objectType,
                                 String objectId,
                                 String operationDesc,
                                 String operator,
                                 String status,
                                 String beforeValue,
                                 String afterValue) {
        AuditLog auditLog = AuditLog.builder()
                .operationType(operationType)
                .objectType(objectType)
                .objectId(objectId)
                .operationDesc(operationDesc)
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .operator(operator)
                .status(AuditLog.AuditStatus.valueOf(status))
                .build();
        
        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Audit log recorded: operationType={}, objectId={}, operator={}, status={}", 
                operationType, objectId, operator, status);
        
        return saved;
    }
    
    @Override
    @Transactional
    public AuditLog logOperation(AuditLog.OperationType operationType,
                                 AuditLog.AuditObjectType objectType,
                                 String objectId,
                                 String operationDesc,
                                 String operator,
                                 String status,
                                 String errorMessage) {
        AuditLog auditLog = AuditLog.builder()
                .operationType(operationType)
                .objectType(objectType)
                .objectId(objectId)
                .operationDesc(operationDesc)
                .operator(operator)
                .status(AuditLog.AuditStatus.valueOf(status))
                .errorMessage(errorMessage)
                .build();
        
        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Audit log recorded: operationType={}, objectId={}, operator={}, status={}", 
                operationType, objectId, operator, status);
        
        return saved;
    }
    
    @Override
    public List<AuditLog> getAuditLogsByObjectId(String objectId) {
        return auditLogRepository.findByObjectIdOrderByCreateTimeDesc(objectId);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByOperatorAndTimeRange(String operator,
                                                              LocalDateTime startTime,
                                                              LocalDateTime endTime) {
        return auditLogRepository.findByOperatorAndTimeRange(operator, startTime, endTime);
    }
    
    @Override
    @Async
    public void asyncLogOperation(AuditLog.OperationType operationType,
                                  AuditLog.AuditObjectType objectType,
                                  String objectId,
                                  String operationDesc,
                                  String operator,
                                  String status) {
        try {
            logOperation(operationType, objectType, objectId, operationDesc, operator, status);
        } catch (Exception e) {
            log.error("Failed to async log operation: {}", e.getMessage(), e);
        }
    }

    @Override
    public Page<AuditLog> queryAuditLogs(String operationType, String objectType, String objectId, String operator, String status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        AuditLog.OperationType opType = null;
        if (operationType != null && !operationType.isEmpty()) {
            opType = AuditLog.OperationType.valueOf(operationType);
        }
        
        AuditLog.AuditObjectType objType = null;
        if (objectType != null && !objectType.isEmpty()) {
            objType = AuditLog.AuditObjectType.valueOf(objectType);
        }
        
        AuditLog.AuditStatus auditStatus = null;
        if (status != null && !status.isEmpty()) {
            auditStatus = AuditLog.AuditStatus.valueOf(status);
        }
        
        return auditLogRepository.findAuditLogs(opType, objType, objectId, operator, auditStatus, startTime, endTime, pageable);
    }
}
