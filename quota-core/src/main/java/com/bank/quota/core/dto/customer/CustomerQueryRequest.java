package com.bank.quota.core.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户查询请求DTO
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "客户查询请求")
public class CustomerQueryRequest {

    @Schema(description = "客户编号", example = "CUS20260212001")
    private String customerNo;

    @Schema(description = "客户名称（模糊查询）", example = "测试")
    private String customerName;

    @Schema(description = "客户类型", example = "CORPORATE")
    private String customerType;

    @Schema(description = "客户分类", example = "SINGLE_CUSTOMER")
    private String category;

    @Schema(description = "集团ID", example = "1001")
    private Long groupId;

    @Schema(description = "风险等级", example = "R3")
    private String riskLevel;

    @Schema(description = "客户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "行业代码", example = "J66")
    private String industryCode;

    @Schema(description = "证件号码", example = "91110000000000000X")
    private String idNumber;

    @Schema(description = "创建开始时间", example = "2026-01-01T00:00:00")
    private LocalDateTime createStartTime;

    @Schema(description = "创建结束时间", example = "2026-12-31T23:59:59")
    private LocalDateTime createEndTime;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
