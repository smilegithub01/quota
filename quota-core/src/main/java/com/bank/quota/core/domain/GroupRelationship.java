package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 集团客户关系实体类
 * 
 * <p>管理集团客户之间的关联关系，支持控股、参股、关联等多种关系类型。</p>
 * 
 * <h3>关系类型：</h3>
 * <ul>
 *   <li>CONTROL - 控股：持股比例超过50%，具有实际控制权</li>
 *   <li>INFLUENCE - 参股：持股比例20%-50%，具有重大影响</li>
 *   <li>AFFILIATE - 关联：持股比例低于20%或存在其他关联关系</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Entity
@Table(name = "group_relationship", indexes = {
    @Index(name = "idx_parent_customer", columnList = "parent_customer_id"),
    @Index(name = "idx_child_customer", columnList = "child_customer_id"),
    @Index(name = "idx_relationship_type", columnList = "relationship_type"),
    @Index(name = "idx_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_parent_child", columnNames = {"parent_customer_id", "child_customer_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"parentCustomer", "childCustomer"})
public class GroupRelationship implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_customer_id", nullable = false)
    private Long parentCustomerId;

    @Column(name = "child_customer_id", nullable = false)
    private Long childCustomerId;

    @Column(name = "relationship_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RelationshipType relationshipType;

    @Column(name = "control_ratio", precision = 5, scale = 2)
    private BigDecimal controlRatio;

    @Column(name = "relationship_desc", length = 500)
    private String relationshipDesc;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RelationshipStatus status;

    @Column(name = "create_by", nullable = false, length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_customer_id", insertable = false, updatable = false)
    private Customer parentCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_customer_id", insertable = false, updatable = false)
    private Customer childCustomer;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = RelationshipStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public enum RelationshipType {
        CONTROL("控股"),
        INFLUENCE("参股"),
        AFFILIATE("关联");

        private final String description;

        RelationshipType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RelationshipStatus {
        ACTIVE("有效"),
        INACTIVE("无效");

        private final String description;

        RelationshipStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
