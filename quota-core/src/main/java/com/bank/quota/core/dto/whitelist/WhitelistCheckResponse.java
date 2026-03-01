package com.bank.quota.core.dto.whitelist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 白名单检查响应DTO
 * 
 * <p>用于返回客户白名单检查结果，包含客户是否在白名单中、
 * 白名单类型、豁免规则等信息。</p>
 * 
 * <h3>响应示例：</h3>
 * <pre>{@code
 * {
 *   "customerId": 1001,
 *   "inWhitelist": true,
 *   "whitelistType": "VIP_CUSTOMER",
 *   "whitelistNo": "WL20260212001",
 *   "exemptRule": "FULL",
 *   "exemptAmount": null,
 *   "effectiveTime": "2026-02-12T00:00:00",
 *   "expiryTime": "2027-02-12T00:00:00"
 * }
 * }</pre>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "白名单检查响应")
public class WhitelistCheckResponse {
    
    @Schema(description = "客户ID", example = "1001")
    private Long customerId;
    
    @Schema(description = "是否在白名单中", example = "true")
    private Boolean inWhitelist;
    
    @Schema(description = "白名单类型", example = "VIP_CUSTOMER")
    private String whitelistType;
    
    @Schema(description = "白名单编号", example = "WL20260212001")
    private String whitelistNo;
    
    @Schema(description = "豁免规则：FULL-全额豁免, PARTIAL-部分豁免, THRESHOLD-阈值豁免", 
            example = "FULL", allowableValues = {"FULL", "PARTIAL", "THRESHOLD"})
    private String exemptRule;
    
    @Schema(description = "豁免金额（部分豁免或阈值豁免时有效）", example = "100000.00")
    private BigDecimal exemptAmount;
    
    @Schema(description = "生效时间", example = "2026-02-12T00:00:00")
    private LocalDateTime effectiveTime;
    
    @Schema(description = "失效时间", example = "2027-02-12T00:00:00")
    private LocalDateTime expiryTime;
}
