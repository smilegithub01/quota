package com.bank.quota.core.domain;

import com.bank.quota.core.enums.MappingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_product_mapping", indexes = {
    @Index(name = "idx_business_type", columnList = "business_type"),
    @Index(name = "idx_product_type", columnList = "product_type"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProductMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Column(name = "product_type", nullable = false, length = 50)
    private String productType;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "risk_level", length = 10)
    private String riskLevel;

    @Column(name = "quota_check_rule", length = 50)
    private String quotaCheckRule;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MappingStatus status;

    @Column(name = "create_by", nullable = false, length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = MappingStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
