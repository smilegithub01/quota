package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(name = "object_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditObjectType objectType;

    @Column(name = "object_id", nullable = false, length = 100)
    private String objectId;

    @Column(name = "operation_desc", nullable = false, length = 500)
    private String operationDesc;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @Column(name = "operator", nullable = false, length = 50)
    private String operator;

    @Column(name = "operator_ip", length = 50)
    private String operatorIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuditStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) {
            status = AuditStatus.SUCCESS;
        }
    }

    public enum OperationType {
        QUOTA_LOCK, QUOTA_UNLOCK, QUOTA_OCCUPY, QUOTA_RELEASE,
        QUOTA_CREATE, QUOTA_UPDATE, QUOTA_DELETE,
        WHITELIST_APPLY, WHITELIST_APPROVE, WHITELIST_INVALID,
        RULE_CREATE, RULE_UPDATE, RULE_DELETE,
        SYSTEM_CONFIG, SYSTEM_CONFIG_UPDATE
    }

    public enum AuditObjectType {
        GROUP_QUOTA, CUSTOMER_QUOTA, APPROVAL_QUOTA,
        WHITELIST, WARNING_RULE, SYSTEM_CONFIG
    }

    public enum AuditStatus {
        SUCCESS, FAILED, PARTIAL
    }
}
