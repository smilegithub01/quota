package com.bank.quota.core.dto.whitelist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 白名单批量导入响应DTO
 * 
 * <p>用于返回批量导入白名单的结果，包含成功数量、失败数量及错误信息。</p>
 * 
 * <h3>响应示例：</h3>
 * <pre>{@code
 * {
 *   "totalCount": 10,
 *   "successCount": 8,
 *   "failCount": 2,
 *   "errorMessages": [
 *     "客户[1005]导入失败: 白名单已存在",
 *     "客户[1008]导入失败: 客户不存在"
 *   ]
 * }
 * }</pre>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "白名单批量导入响应")
public class WhitelistBatchImportResponse {
    
    @Schema(description = "总数量", example = "10")
    private Integer totalCount;
    
    @Schema(description = "成功数量", example = "8")
    private Integer successCount;
    
    @Schema(description = "失败数量", example = "2")
    private Integer failCount;
    
    @Schema(description = "错误信息列表")
    private List<String> errorMessages;
}
