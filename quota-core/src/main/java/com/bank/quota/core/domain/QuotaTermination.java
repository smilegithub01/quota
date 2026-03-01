package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "quota_termination", indexes = {
    @Index(name = "idx_termination_no", columnList = "termination_no"),
    @Index(name = "idx_quota_id", columnList = "quota_id"),
    @Index(name = "idx_quota_type", columnList = "quota_type"),
    @Index(name = "idx_terminate_type", columnList = "terminate_type"),
    @Index(name = "idx_terminate_time", columnList = "terminate_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"quota"})
public class QuotaTermination implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "termination_no", nullable = false, unique = true, length = 50)
    private String terminationNo;

    @Column(name = "quota_id", nullable = false)
    private Long quotaId;

    @Column(name = "quota_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuotaType quotaType;

    @Column(name = "terminate_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TerminateType terminateType;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "operator", nullable = false, length = 50)
    private String operator;

    @Column(name = "terminate_time", nullable = false)
    private LocalDateTime terminateTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (terminateTime == null) {
            terminateTime = LocalDateTime.now();
        }
    }

    public enum QuotaType {
        GROUP("集团"),
        CUSTOMER("客户"),
        APPROVAL("批复");

        private final String description;

        QuotaType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TerminateType {
        EXPIRY("到期终止"),
        MANUAL("主动终止"),
        FORCE("强制终止");

        private final String description;

        TerminateType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
