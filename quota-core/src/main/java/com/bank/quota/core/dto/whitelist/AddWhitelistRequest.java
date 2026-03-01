package com.bank.quota.core.dto.whitelist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加白名单请求DTO
 * 
 * <p>用于简化版白名单添加接口的请求参数。</p>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>快速添加客户到白名单</li>
 *   <li>批量导入时的单条记录</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "添加白名单请求")
public class AddWhitelistRequest {
    
    @NotNull(message = "客户ID不能为空")
    @Schema(description = "客户ID", example = "1001", required = true)
    private Long customerId;
    
    @NotBlank(message = "客户名称不能为空")
    @Schema(description = "客户名称", example = "测试客户", required = true)
    private String customerName;
    
    @NotBlank(message = "白名单类型不能为空")
    @Schema(description = "白名单类型：VIP_CUSTOMER- VIP客户, BUSINESS_PARTNER-合作伙伴", 
            example = "VIP_CUSTOMER", required = true, allowableValues = {"VIP_CUSTOMER", "BUSINESS_PARTNER"})
    private String whitelistType;
    
    @Schema(description = "业务类型", example = "LOAN")
    private String businessType;
    
    @Schema(description = "描述", example = "VIP客户白名单")
    private String description;
    
    @NotBlank(message = "创建人不能为空")
    @Schema(description = "创建人", example = "admin", required = true)
    private String createBy;
}
