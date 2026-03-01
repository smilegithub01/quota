package com.bank.quota.core.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建客户请求DTO
 * 
 * <p>用于创建新客户的请求参数。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "创建客户请求")
public class CreateCustomerRequest {

    @NotBlank(message = "客户名称不能为空")
    @Schema(description = "客户名称", example = "测试企业有限公司", required = true)
    private String customerName;

    @NotBlank(message = "客户类型不能为空")
    @Schema(description = "客户类型：CORPORATE-对公, INDIVIDUAL-个人, INTERBANK-同业", 
            example = "CORPORATE", required = true, allowableValues = {"CORPORATE", "INDIVIDUAL", "INTERBANK"})
    private String customerType;

    @NotBlank(message = "客户分类不能为空")
    @Schema(description = "客户分类：GROUP_CUSTOMER-集团客户, SINGLE_CUSTOMER-单一客户", 
            example = "SINGLE_CUSTOMER", required = true, allowableValues = {"GROUP_CUSTOMER", "SINGLE_CUSTOMER"})
    private String category;

    @Schema(description = "集团ID（集团客户必填）", example = "1001")
    private Long groupId;

    @Schema(description = "集团名称", example = "测试集团")
    private String groupName;

    @Schema(description = "证件类型：USCC-统一社会信用代码, ID_CARD-身份证等", example = "USCC")
    private String idType;

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

    @Schema(description = "风险等级：R1-R5", example = "R3", allowableValues = {"R1", "R2", "R3", "R4", "R5"})
    private String riskLevel;

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

    @Schema(description = "生效日期", example = "2026-02-12")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期", example = "2030-12-31")
    private LocalDate expiryDate;

    @Schema(description = "描述", example = "优质企业客户")
    private String description;

    @NotBlank(message = "创建人不能为空")
    @Schema(description = "创建人", example = "admin", required = true)
    private String createBy;
}
