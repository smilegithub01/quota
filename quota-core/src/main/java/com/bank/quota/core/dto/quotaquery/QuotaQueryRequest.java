package com.bank.quota.core.dto.quotaquery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 额度查询请求
 * 
 * <p>用于查询各级额度信息，包括集团额度、客户额度、批复额度等。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Schema(description = "额度查询请求")
public class QuotaQueryRequest {

    @Schema(description = "对象类型", example = "GROUP")
    private String objectType;

    @Schema(description = "对象ID", example = "12345")
    private Long objectId;

    @Schema(description = "客户ID", example = "12345")
    private Long customerId;

    @Schema(description = "集团ID", example = "12345")
    private Long groupId;

    @Schema(description = "批复额度ID", example = "12345")
    private Long approvalId;

    @Schema(description = "查询深度（穿透查询层级）", example = "2")
    private Integer depth;

    @Schema(description = "是否返回明细", example = "true")
    private Boolean includeDetails;

    @Schema(description = "开始日期", example = "2026-01-01")
    private String startDate;

    @Schema(description = "结束日期", example = "2026-12-31")
    private String endDate;
}