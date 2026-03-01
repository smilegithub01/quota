package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户关联方实体类
 * 
 * <p>管理客户的关联方信息，包括股东、实际控制人、担保人等。</p>
 * 
 * <h3>关联方类型：</h3>
 * <ul>
 *   <li>SHAREHOLDER - 股东：持有客户股份的股东</li>
 *   <li>CONTROLLER - 实际控制人：对客户具有实际控制权的自然人或法人</li>
 *   <li>GUARANTOR - 担保人：为客户贷款提供担保的自然人或法人</li>
 *   <li>RELATED_PERSON - 关联人：与客户存在关联关系的自然人或法人</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Entity
@Table(name = "customer_affiliate", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_affiliate_id", columnList = "affiliate_id"),
    @Index(name = "idx_affiliate_type", columnList = "affiliate_type"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer"})
public class CustomerAffiliate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "affiliate_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private AffiliateType affiliateType;

    @Column(name = "affiliate_id")
    private Long affiliateId;

    @Column(name = "affiliate_name", nullable = false, length = 200)
    private String affiliateName;

    @Column(name = "affiliate_identity", length = 100)
    private String affiliateIdentity;

    @Column(name = "relationship_desc", length = 500)
    private String relationshipDesc;

    @Column(name = "relationship_ratio", precision = 5, scale = 2)
    private BigDecimal relationshipRatio;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AffiliateStatus status;

    @Column(name = "create_by", nullable = false, length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = AffiliateStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public enum AffiliateType {
        SHAREHOLDER("股东"),
        CONTROLLER("实际控制人"),
        GUARANTOR("担保人"),
        RELATED_PERSON("关联人");

        private final String description;

        AffiliateType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum AffiliateStatus {
        ACTIVE("有效"),
        INACTIVE("无效");

        private final String description;

        AffiliateStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
