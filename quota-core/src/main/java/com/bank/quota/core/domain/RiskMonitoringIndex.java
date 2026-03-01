package com.bank.quota.core.domain;

import com.bank.quota.core.enums.IndexRiskLevel;
import com.bank.quota.core.enums.IndexStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_monitoring_index", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_index_code", columnList = "index_code"),
    @Index(name = "idx_calc_date", columnList = "calc_date"),
    @Index(name = "idx_risk_level", columnList = "risk_level"),
    @Index(name = "idx_index_type", columnList = "index_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer"})
public class RiskMonitoringIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "index_type", nullable = false, length = 50)
    private String indexType;

    @Column(name = "index_code", nullable = false, length = 50)
    private String indexCode;

    @Column(name = "index_name", nullable = false, length = 200)
    private String indexName;

    @Column(name = "index_value", precision = 20, scale = 4)
    private BigDecimal indexValue;

    @Column(name = "index_unit", length = 20)
    private String indexUnit;

    @Column(name = "threshold_value", precision = 20, scale = 4)
    private BigDecimal thresholdValue;

    @Column(name = "threshold_min", precision = 20, scale = 4)
    private BigDecimal thresholdMin;

    @Column(name = "threshold_max", precision = 20, scale = 4)
    private BigDecimal thresholdMax;

    @Column(name = "risk_level", length = 10)
    @Enumerated(EnumType.STRING)
    private IndexRiskLevel riskLevel;

    @Column(name = "calc_date", nullable = false)
    private LocalDate calcDate;

    @Column(name = "calc_method", length = 200)
    private String calcMethod;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private IndexStatus status;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) {
            status = IndexStatus.ACTIVE;
        }
    }
}
