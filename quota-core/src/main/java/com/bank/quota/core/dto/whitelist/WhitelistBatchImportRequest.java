package com.bank.quota.core.dto.whitelist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 白名单批量导入请求DTO
 * 
 * <p>用于批量导入白名单记录的请求参数。</p>
 * 
 * <h3>请求示例：</h3>
 * <pre>{@code
 * {
 *   "whitelists": [
 *     {
 *       "customerId": 1001,
 *       "customerName": "客户A",
 *       "whitelistType": "VIP_CUSTOMER",
 *       "businessType": "LOAN"
 *     },
 *     {
 *       "customerId": 1002,
 *       "customerName": "客户B",
 *       "whitelistType": "BUSINESS_PARTNER",
 *       "businessType": "TRADE"
 *     }
 *   ],
 *   "createBy": "admin"
 * }
 * }</pre>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "白名单批量导入请求")
public class WhitelistBatchImportRequest {
    
    @NotEmpty(message = "白名单列表不能为空")
    @Valid
    @Schema(description = "白名单列表", required = true)
    private List<AddWhitelistRequest> whitelists;
    
    @NotBlank(message = "创建人不能为空")
    @Schema(description = "创建人", example = "admin", required = true)
    private String createBy;
}
