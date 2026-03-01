package com.bank.quota.core.controller;

import com.bank.quota.common.result.PageResult;
import com.bank.quota.common.result.Result;
import com.bank.quota.core.domain.AuditLog;
import com.bank.quota.core.dto.auditlog.*;
import com.bank.quota.core.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "审计日志管理", description = "审计日志查询、统计接口")
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    @GetMapping("/logs")
    @Operation(summary = "分页查询审计日志", description = "根据条件分页查询审计日志")
    public Result<AuditLogPageResponse> queryAuditLogs(
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) String objectId,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("Received query audit logs request: operationType={}, objectType={}, pageNum={}, pageSize={}", 
                operationType, objectType, pageNum, pageSize);
        
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<AuditLog> page = auditLogService.queryAuditLogs(operationType, objectType, objectId, 
                operator, status, startTime, endTime, pageable);
        
        AuditLogPageResponse response = new AuditLogPageResponse();
        response.setTotal(page.getTotalElements());
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setPages(page.getTotalPages());
        response.setList(page.getContent().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList()));
        
        return Result.success(response);
    }
    
    @GetMapping("/logs/{id}")
    @Operation(summary = "查询审计日志详情", description = "根据ID查询审计日志详情")
    public Result<AuditLogQueryResponse> getAuditLog(@PathVariable Long id) {
        log.info("Received get audit log request: id={}", id);
        
        List<AuditLog> logs = auditLogService.getAuditLogsByObjectId(id.toString());
        if (logs.isEmpty()) {
            return Result.error("100003", "审计日志不存在");
        }
        
        return Result.success(buildResponse(logs.get(0)));
    }
    
    @GetMapping("/logs/object/{objectId}")
    @Operation(summary = "查询对象操作日志", description = "根据对象ID查询所有操作日志")
    public Result<List<AuditLogQueryResponse>> getAuditLogsByObjectId(@PathVariable String objectId) {
        log.info("Received get audit logs by objectId request: objectId={}", objectId);
        
        List<AuditLog> logs = auditLogService.getAuditLogsByObjectId(objectId);
        
        List<AuditLogQueryResponse> responses = logs.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
        
        return Result.success(responses);
    }
    
    @GetMapping("/logs/operator/{operator}")
    @Operation(summary = "查询操作人日志", description = "根据操作人查询日志")
    public Result<List<AuditLogQueryResponse>> getAuditLogsByOperator(
            @PathVariable String operator,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("Received get audit logs by operator request: operator={}", operator);
        
        List<AuditLog> logs = auditLogService.getAuditLogsByOperatorAndTimeRange(operator, startTime, endTime);
        
        List<AuditLogQueryResponse> responses = logs.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
        
        return Result.success(responses);
    }
    
    private AuditLogQueryResponse buildResponse(AuditLog auditLog) {
        AuditLogQueryResponse response = new AuditLogQueryResponse();
        response.setId(auditLog.getId());
        response.setOperationType(auditLog.getOperationType() != null ? 
                auditLog.getOperationType().name() : null);
        response.setObjectType(auditLog.getObjectType() != null ? 
                auditLog.getObjectType().name() : null);
        response.setObjectId(auditLog.getObjectId());
        response.setOperationDesc(auditLog.getOperationDesc());
        response.setBeforeValue(auditLog.getBeforeValue());
        response.setAfterValue(auditLog.getAfterValue());
        response.setOperator(auditLog.getOperator());
        response.setOperatorIp(auditLog.getOperatorIp());
        response.setStatus(auditLog.getStatus() != null ? auditLog.getStatus().name() : null);
        response.setErrorMessage(auditLog.getErrorMessage());
        response.setDurationMs(auditLog.getDurationMs());
        response.setRequestId(auditLog.getRequestId());
        response.setTraceId(auditLog.getTraceId());
        response.setCreateTime(auditLog.getCreateTime());
        return response;
    }
}
