package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_warning_rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskWarningRule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 50)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    @Column(name = "warning_level", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private WarningLevel warningLevel;

    @Column(name = "threshold_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ThresholdType thresholdType;

    @Column(name = "threshold_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdValue;

    @Column(name = "object_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private QuotaObjectType objectType;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RuleStatus status;

    @Column(name = "notify_channels", length = 500)
    private String notifyChannels;

    @Column(name = "notify_recipients", length = 500)
    private String notifyRecipients;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public enum RuleType {
        USAGE_RATE, ABSOLUTE_AMOUNT, CONCENTRATION, COMPLIANCE
    }

    public enum WarningLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ThresholdType {
        GREATER_THAN, LESS_THAN, EQUAL, BETWEEN
    }

    public enum QuotaObjectType {
        GROUP, CUSTOMER, APPROVAL
    }

    public enum RuleStatus {
        ENABLED, DISABLED
    }
}
