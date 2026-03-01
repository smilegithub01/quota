package com.bank.quota.core.domain;

import com.bank.quota.core.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_quota", indexes = {
    @Index(name = "idx_contract_no", columnList = "contract_no"),
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_item_id", columnList = "item_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractQuota implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_no", nullable = false, unique = true, length = 50)
    private String contractNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "contract_name", nullable = false, length = 200)
    private String contractName;

    @Column(name = "contract_type", length = 50)
    private String contractType;

    @Column(name = "contract_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal contractQuota;

    @Column(name = "used_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal usedQuota;

    @Column(name = "available_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal availableQuota;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    @Column(name = "sign_date")
    private LocalDateTime signDate;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "description", length = 500)
    private String description;

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
            status = ContractStatus.ACTIVE;
        }
        if (usedQuota == null) {
            usedQuota = BigDecimal.ZERO;
        }
        if (availableQuota == null) {
            availableQuota = contractQuota;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
