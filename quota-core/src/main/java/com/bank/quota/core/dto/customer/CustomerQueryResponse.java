package com.bank.quota.core.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 客户查询响应DTO
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Data
@Schema(description = "客户查询响应")
public class CustomerQueryResponse {

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "总页数", example = "5")
    private Integer pages;

    @Schema(description = "客户列表")
    private List<CustomerResponse> list;
}
