package com.bank.quota.core.dto.auditlog;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogQueryResponse {
    private Long id;
    private String operationType;
    private String objectType;
    private String objectId;
    private String operationDesc;
    private String beforeValue;
    private String afterValue;
    private String operator;
    private String operatorIp;
    private String status;
    private String errorMessage;
    private Long durationMs;
    private String requestId;
    private String traceId;
    private LocalDateTime createTime;
}
