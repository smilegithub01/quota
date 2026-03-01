package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户实体类
 * 
 * <p>银行级信贷额度管控平台的客户基础信息实体。
 * 支持集团客户和单一客户的分类管理，包含客户类型、风险等级等基础属性字段。</p>
 * 
 * <h3>客户分类：</h3>
 * <ul>
 *   <li>GROUP_CUSTOMER - 集团客户：属于某个集团，额度受集团限额控制</li>
 *   <li>SINGLE_CUSTOMER - 单一客户：独立客户，不隶属于任何集团</li>
 * </ul>
 * 
 * <h3>客户类型：</h3>
 * <ul>
 *   <li>CORPORATE - 对公客户：企业、机构等法人客户</li>
 *   <li>INDIVIDUAL - 个人客户：自然人客户</li>
 *   <li>INTERBANK - 同业客户：银行、证券等金融机构客户</li>
 * </ul>
 * 
 * <h3>风险等级：</h3>
 * <ul>
 *   <li>R1 - 低风险：优质客户，信用评级AAA</li>
 *   <li>R2 - 较低风险：良好客户，信用评级AA</li>
 *   <li>R3 - 中等风险：一般客户，信用评级A</li>
 *   <li>R4 - 较高风险：关注客户，信用评级BBB</li>
 *   <li>R5 - 高风险：不良客户，信用评级BB及以下</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Entity
@Table(name = "customer", indexes = {
    @Index(name = "idx_customer_no", columnList = "customer_no"),
    @Index(name = "idx_customer_type", columnList = "customer_type"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_group_id", columnList = "group_id"),
    @Index(name = "idx_risk_level", columnList = "risk_level"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_id_number", columnList = "id_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"groupId"})
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_no", nullable = false, unique = true, length = 32)
    private String customerNo;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "customer_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerType customerType;

    @Column(name = "category", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerCategory category;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", length = 200)
    private String groupName;

    @Column(name = "id_type", length = 20)
    @Enumerated(EnumType.STRING)
    private IdType idType;

    @Column(name = "id_number", length = 50)
    private String idNumber;

    @Column(name = "legal_person", length = 100)
    private String legalPerson;

    @Column(name = "registered_address", length = 500)
    private String registeredAddress;

    @Column(name = "business_scope", length = 1000)
    private String businessScope;

    @Column(name = "registered_capital", precision = 20, scale = 4)
    private BigDecimal registeredCapital;

    @Column(name = "establish_date")
    private LocalDate establishDate;

    @Column(name = "industry_code", length = 20)
    private String industryCode;

    @Column(name = "industry_name", length = 100)
    private String industryName;

    @Column(name = "risk_level", nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "credit_rating", length = 10)
    private String creditRating;

    @Column(name = "credit_score")
    private Integer creditScore;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerStatus status;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

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
            status = CustomerStatus.ACTIVE;
        }
        if (riskLevel == null) {
            riskLevel = RiskLevel.R3;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 客户类型枚举
     */
    public enum CustomerType {
        CORPORATE("对公客户"),
        INDIVIDUAL("个人客户"),
        INTERBANK("同业客户");

        private final String description;

        CustomerType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 客户分类枚举
     */
    public enum CustomerCategory {
        GROUP_CUSTOMER("集团客户"),
        SINGLE_CUSTOMER("单一客户");

        private final String description;

        CustomerCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 证件类型枚举
     */
    public enum IdType {
        USCC("统一社会信用代码"),
        BUSINESS_LICENSE("营业执照"),
        ID_CARD("身份证"),
        PASSPORT("护照"),
        ORG_CODE("组织机构代码"),
        TAX_REG("税务登记证");

        private final String description;

        IdType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        R1("低风险"),
        R2("较低风险"),
        R3("中等风险"),
        R4("较高风险"),
        R5("高风险");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 客户状态枚举
     */
    public enum CustomerStatus {
        ACTIVE("正常"),
        FROZEN("冻结"),
        DISABLED("停用"),
        CANCELLED("注销");

        private final String description;

        CustomerStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
