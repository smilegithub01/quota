package com.bank.quota.core.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一客户视图响应DTO
 * 
 * <p>整合展示客户的基本信息、额度总额、已用额度、可用额度、
 * 额度使用记录及客户类型等关键信息。</p>
 * 
 * <h3>视图内容：</h3>
 * <ul>
 *   <li>客户基本信息</li>
 *   <li>额度概览（总额度、已用额度、可用额度、使用率）</li>
 *   <li>额度明细（各层级额度详情）</li>
 *   <li>风险信息（风险等级、信用评分）</li>
 *   <li>白名单状态</li>
 *   <li>近期额度使用记录</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "统一客户视图响应")
public class CustomerUnifiedViewResponse {

    @Schema(description = "客户基本信息")
    private CustomerBasicInfo customerInfo;

    @Schema(description = "额度概览")
    private QuotaOverview quotaOverview;

    @Schema(description = "额度明细列表")
    private List<QuotaDetail> quotaDetails;

    @Schema(description = "风险信息")
    private RiskInfo riskInfo;

    @Schema(description = "白名单状态")
    private WhitelistStatus whitelistStatus;

    @Schema(description = "近期额度使用记录")
    private List<QuotaUsageRecord> recentUsageRecords;

    /**
     * 客户基本信息
     */
    @Data
    @Schema(description = "客户基本信息")
    public static class CustomerBasicInfo {
        @Schema(description = "客户ID", example = "1")
        private Long customerId;

        @Schema(description = "客户编号", example = "CUS20260212001")
        private String customerNo;

        @Schema(description = "客户名称", example = "测试企业有限公司")
        private String customerName;

        @Schema(description = "客户类型", example = "CORPORATE")
        private String customerType;

        @Schema(description = "客户类型描述", example = "对公客户")
        private String customerTypeDesc;

        @Schema(description = "客户分类", example = "SINGLE_CUSTOMER")
        private String category;

        @Schema(description = "客户分类描述", example = "单一客户")
        private String categoryDesc;

        @Schema(description = "集团ID", example = "1001")
        private Long groupId;

        @Schema(description = "集团名称", example = "测试集团")
        private String groupName;

        @Schema(description = "行业名称", example = "货币金融服务")
        private String industryName;

        @Schema(description = "客户状态", example = "ACTIVE")
        private String status;

        @Schema(description = "客户状态描述", example = "正常")
        private String statusDesc;

        @Schema(description = "联系人", example = "李四")
        private String contactPerson;

        @Schema(description = "联系电话", example = "13800138000")
        private String contactPhone;

        @Schema(description = "创建时间", example = "2026-02-12T10:30:00")
        private LocalDateTime createTime;
    }

    /**
     * 额度概览
     */
    @Data
    @Schema(description = "额度概览")
    public static class QuotaOverview {
        @Schema(description = "总额度", example = "10000000.00")
        private BigDecimal totalQuota;

        @Schema(description = "已用额度", example = "3000000.00")
        private BigDecimal usedQuota;

        @Schema(description = "锁定额度", example = "500000.00")
        private BigDecimal lockedQuota;

        @Schema(description = "可用额度", example = "6500000.00")
        private BigDecimal availableQuota;

        @Schema(description = "额度使用率（百分比）", example = "30.00")
        private BigDecimal usageRate;

        @Schema(description = "额度状态", example = "ENABLED")
        private String quotaStatus;

        @Schema(description = "额度状态描述", example = "有效")
        private String quotaStatusDesc;
    }

    /**
     * 额度明细
     */
    @Data
    @Schema(description = "额度明细")
    public static class QuotaDetail {
        @Schema(description = "额度层级", example = "CUSTOMER")
        private String level;

        @Schema(description = "额度层级描述", example = "客户额度")
        private String levelDesc;

        @Schema(description = "额度编号", example = "CQ20260212001")
        private String quotaNo;

        @Schema(description = "额度名称", example = "客户综合额度")
        private String quotaName;

        @Schema(description = "总额度", example = "10000000.00")
        private BigDecimal totalQuota;

        @Schema(description = "已用额度", example = "3000000.00")
        private BigDecimal usedQuota;

        @Schema(description = "可用额度", example = "7000000.00")
        private BigDecimal availableQuota;

        @Schema(description = "使用率", example = "30.00")
        private BigDecimal usageRate;

        @Schema(description = "状态", example = "ENABLED")
        private String status;
    }

    /**
     * 风险信息
     */
    @Data
    @Schema(description = "风险信息")
    public static class RiskInfo {
        @Schema(description = "风险等级", example = "R3")
        private String riskLevel;

        @Schema(description = "风险等级描述", example = "中等风险")
        private String riskLevelDesc;

        @Schema(description = "信用评级", example = "AA")
        private String creditRating;

        @Schema(description = "信用评分", example = "750")
        private Integer creditScore;

        @Schema(description = "预警状态", example = "NORMAL")
        private String warningStatus;

        @Schema(description = "预警信息", example = "额度使用率已达70%")
        private String warningMessage;
    }

    /**
     * 白名单状态
     */
    @Data
    @Schema(description = "白名单状态")
    public static class WhitelistStatus {
        @Schema(description = "是否在白名单", example = "true")
        private Boolean inWhitelist;

        @Schema(description = "白名单类型", example = "VIP_CUSTOMER")
        private String whitelistType;

        @Schema(description = "白名单编号", example = "WL20260212001")
        private String whitelistNo;

        @Schema(description = "豁免规则", example = "FULL")
        private String exemptRule;

        @Schema(description = "生效时间", example = "2026-02-12T00:00:00")
        private LocalDateTime effectiveTime;

        @Schema(description = "失效时间", example = "2027-02-12T00:00:00")
        private LocalDateTime expiryTime;
    }

    /**
     * 额度使用记录
     */
    @Data
    @Schema(description = "额度使用记录")
    public static class QuotaUsageRecord {
        @Schema(description = "记录ID", example = "1")
        private Long recordId;

        @Schema(description = "操作类型", example = "QUOTA_OCCUPY")
        private String operationType;

        @Schema(description = "操作类型描述", example = "额度占用")
        private String operationTypeDesc;

        @Schema(description = "金额", example = "100000.00")
        private BigDecimal amount;

        @Schema(description = "合同编号", example = "CT20260212001")
        private String contractNo;

        @Schema(description = "业务类型", example = "LOAN")
        private String businessType;

        @Schema(description = "操作时间", example = "2026-02-12T10:30:00")
        private LocalDateTime operationTime;

        @Schema(description = "操作人", example = "admin")
        private String operator;

        @Schema(description = "备注", example = "贷款发放")
        private String remark;
    }
}
