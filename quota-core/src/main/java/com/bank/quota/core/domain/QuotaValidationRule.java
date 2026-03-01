package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quota_validation_rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotaValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", unique = true, nullable = false, length = 50)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 30)
    private RuleType ruleType;

    @Column(name = "validation_level", nullable = false)
    private Integer validationLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_object", nullable = false, length = 30)
    private ValidationObject validationObject;

    @Column(name = "rule_expression", nullable = false, columnDefinition = "TEXT")
    private String ruleExpression;

    @Column(name = "error_code", nullable = false, length = 20)
    private String errorCode;

    @Column(name = "error_message", nullable = false, length = 500)
    private String errorMessage;

    @Column(name = "business_types", length = 200)
    private String businessTypes;

    @Column(name = "risk_levels", length = 50)
    private String riskLevels;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RuleStatus status = RuleStatus.ENABLED;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "create_by", nullable = false, length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public enum RuleType {
        LIMIT,
        STATUS,
        COMPLIANCE,
        BUSINESS
    }

    public enum ValidationObject {
        GROUP,
        CUSTOMER,
        APPROVAL,
        CONTRACT
    }

    public enum RuleStatus {
        ENABLED,
        DISABLED
    }
}
