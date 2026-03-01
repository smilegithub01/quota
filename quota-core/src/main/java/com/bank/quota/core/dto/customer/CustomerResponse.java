package com.bank.quota.core.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户响应DTO
 * 
 * <p>客户信息的响应数据结构。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "客户响应")
public class CustomerResponse {

    @Schema(description = "客户ID", example = "1")
    private Long id;

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

    @Schema(description = "证件类型", example = "USCC")
    private String idType;

    @Schema(description = "证件类型描述", example = "统一社会信用代码")
    private String idTypeDesc;

    @Schema(description = "证件号码", example = "91110000000000000X")
    private String idNumber;

    @Schema(description = "法定代表人", example = "张三")
    private String legalPerson;

    @Schema(description = "注册地址", example = "北京市朝阳区")
    private String registeredAddress;

    @Schema(description = "经营范围", example = "技术开发、技术咨询")
    private String businessScope;

    @Schema(description = "注册资本", example = "10000000.00")
    private BigDecimal registeredCapital;

    @Schema(description = "成立日期", example = "2020-01-01")
    private LocalDate establishDate;

    @Schema(description = "行业代码", example = "J66")
    private String industryCode;

    @Schema(description = "行业名称", example = "货币金融服务")
    private String industryName;

    @Schema(description = "风险等级", example = "R3")
    private String riskLevel;

    @Schema(description = "风险等级描述", example = "中等风险")
    private String riskLevelDesc;

    @Schema(description = "信用评级", example = "AA")
    private String creditRating;

    @Schema(description = "信用评分", example = "750")
    private Integer creditScore;

    @Schema(description = "联系人", example = "李四")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "联系邮箱", example = "contact@example.com")
    private String contactEmail;

    @Schema(description = "客户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "客户状态描述", example = "正常")
    private String statusDesc;

    @Schema(description = "生效日期", example = "2026-02-12")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期", example = "2030-12-31")
    private LocalDate expiryDate;

    @Schema(description = "描述", example = "优质企业客户")
    private String description;

    @Schema(description = "创建人", example = "admin")
    private String createBy;

    @Schema(description = "更新人", example = "admin")
    private String updateBy;

    @Schema(description = "创建时间", example = "2026-02-12T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-02-12T11:00:00")
    private LocalDateTime updateTime;
}
