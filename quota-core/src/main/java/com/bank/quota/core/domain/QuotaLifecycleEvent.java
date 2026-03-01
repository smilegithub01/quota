package com.bank.quota.core.domain;

import com.bank.quota.core.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quota_lifecycle_event", indexes = {
    @Index(name = "idx_event_id", columnList = "event_id"),
    @Index(name = "idx_quota_id", columnList = "quota_id"),
    @Index(name = "idx_quota_type", columnList = "quota_type"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_event_time", columnList = "event_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaLifecycleEvent implements Serializable {
    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 50)
    public String eventId;

    @Column(name = "quota_id", nullable = false)
    public Long quotaId;

    @Column(name = "quota_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    public QuotaType quotaType;

    @Column(name = "event_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    public EventType eventType;

    @Column(name = "amount", precision = 24, scale = 6)
    public BigDecimal amount;

    @Column(name = "before_status", length = 20)
    public String beforeStatus;

    @Column(name = "after_status", length = 20)
    public String afterStatus;

    @Column(name = "reason", length = 500)
    public String reason;

    @Column(name = "operator", nullable = false, length = 50)
    public String operator;

    @Column(name = "business_ref_no", length = 50)
    public String businessRefNo;

    @Column(name = "event_time", nullable = false)
    public LocalDateTime eventTime;

    @Column(name = "create_time", nullable = false, updatable = false)
    public LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
    }
}
