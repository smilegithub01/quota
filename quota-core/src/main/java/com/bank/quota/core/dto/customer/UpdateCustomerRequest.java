package com.bank.quota.core.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新客户请求DTO
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "更新客户请求")
public class UpdateCustomerRequest {

    @Schema(description = "客户名称", example = "测试企业有限公司")
    private String customerName;

    @Schema(description = "法定代表人", example = "张三")
    private String legalPerson;

    @Schema(description = "注册地址", example = "北京市朝阳区")
    private String registeredAddress;

    @Schema(description = "经营范围", example = "技术开发、技术咨询")
    private String businessScope;

    @Schema(description = "注册资本", example = "10000000.00")
    private BigDecimal registeredCapital;

    @Schema(description = "行业代码", example = "J66")
    private String industryCode;

    @Schema(description = "行业名称", example = "货币金融服务")
    private String industryName;

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

    @Schema(description = "更新人", example = "admin")
    private String updateBy;
}
