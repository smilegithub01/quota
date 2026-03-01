package com.bank.quota.core.dto.quotaquery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 额度查询响应
 * 
 * <p>返回各级额度的详细信息，包括总额度、已用额度、可用额度等。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Schema(description = "额度查询响应")
public class QuotaQueryResponse {

    @Schema(description = "额度ID")
    private Long quotaId;

    @Schema(description = "额度类型")
    private String quotaType;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "集团ID")
    private Long groupId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "总额度")
    private BigDecimal totalQuota;

    @Schema(description = "已用额度")
    private BigDecimal usedQuota;

    @Schema(description = "可用额度")
    private BigDecimal availableQuota;

    @Schema(description = "锁定额度")
    private BigDecimal lockedQuota;

    @Schema(description = "货币类型")
    private String currency;

    @Schema(description = "产品类型")
    private String productType;

    @Schema(description = "生效日期")
    private LocalDate effectiveDate;

    @Schema(description = "到期日期")
    private LocalDate expiryDate;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "批复编号")
    private String approvalNo;

    @Schema(description = "子额度列表")
    private List<QuotaQueryResponse> subQuotas;

    @Schema(description = "使用率")
    private BigDecimal usageRate;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

    /**
     * 计算使用率
     */
    public void calculateUsageRate() {
        if (totalQuota != null && totalQuota.compareTo(BigDecimal.ZERO) > 0) {
            this.usageRate = usedQuota.divide(totalQuota, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else {
            this.usageRate = BigDecimal.ZERO;
        }
    }
}