package com.bank.quota.core.dto.auditlog;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogQueryRequest {
    private String operationType;
    private String objectType;
    private String objectId;
    private String operator;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum;
    private Integer pageSize;
}
