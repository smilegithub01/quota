package com.bank.quota.core.dto.lifecycle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 额度创建请求
 * 
 * <p>用于根据授信批复结果创建额度记录，建立六级额度管控体系的初始数据。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Schema(description = "额度创建请求")
public class QuotaCreateRequest {

    @NotBlank(message = "额度类型不能为空")
    @Schema(description = "额度类型", required = true, example = "APPROVAL")
    private String quotaType;

    @NotNull(message = "客户ID不能为空")
    @Schema(description = "客户ID", required = true, example = "12345")
    private Long customerId;

    @Schema(description = "集团ID", example = "12345")
    private Long groupId;

    @NotNull(message = "批复额度不能为空")
    @DecimalMin(value = "0.00", message = "批复额度不能小于0")
    @Schema(description = "批复额度", required = true, example = "1000000.00")
    private BigDecimal approvalQuota;

    @Schema(description = "货币类型", example = "CNY")
    private String currency;

    @Schema(description = "产品类型", example = "LOAN")
    private String productType;

    @Schema(description = "额度有效期限-开始", example = "2026-01-01")
    private LocalDate effectiveDate;

    @Schema(description = "额度有效期限-结束", example = "2026-12-31")
    private LocalDate expiryDate;

    @Schema(description = "授信批复编号", example = "APP2026010001")
    private String approvalNo;

    @Schema(description = "授信批复日期", example = "2026-01-01")
    private LocalDate approvalDate;

    @Schema(description = "授信批复机构", example = "总行营业部")
    private String approvalOrg;

    @Schema(description = "申请人", example = "张三")
    private String applicant;

    @Schema(description = "用途说明", example = "流动资金贷款")
    private String purpose;

    @Schema(description = "担保方式", example = "抵押")
    private String guaranteeMethod;

    @Schema(description = "备注", example = "重要客户")
    private String remark;
}