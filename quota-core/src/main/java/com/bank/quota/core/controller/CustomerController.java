package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.customer.*;
import com.bank.quota.core.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理控制器
 * 
 * <p>提供银行级信贷额度管控平台的客户管理REST API接口。
 * 支持集团客户和单一客户的分类管理，包含客户类型等基础属性字段。</p>
 * 
 * <h3>功能概述：</h3>
 * <ul>
 *   <li>客户创建、更新、查询、删除</li>
 *   <li>集团客户和单一客户分类管理</li>
 *   <li>客户风险等级管理</li>
 *   <li>客户状态变更（冻结、解冻、停用）</li>
 *   <li>统一客户视图查询</li>
 * </ul>
 * 
 * <h3>客户分类：</h3>
 * <ul>
 *   <li>GROUP_CUSTOMER - 集团客户：属于某个集团，额度受集团限额控制</li>
 *   <li>SINGLE_CUSTOMER - 单一客户：独立客户，不隶属于任何集团</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 * @see CustomerService
 * @see CustomerResponse
 * @see CustomerUnifiedViewResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "客户管理", description = "客户创建、查询、更新、状态变更接口")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 创建客户
     * 
     * <p>创建新的客户记录。支持集团客户和单一客户两种分类。</p>
     * 
     * <h4>业务规则：</h4>
     * <ul>
     *   <li>集团客户必须指定所属集团ID</li>
     *   <li>单一客户不需要指定集团ID</li>
     *   <li>证件号码不能重复</li>
     * </ul>
     * 
     * @param request 创建请求
     * @return 客户信息
     */
    @PostMapping
    @Operation(
        summary = "创建客户", 
        description = "创建新的客户记录，支持集团客户和单一客户分类"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "409", description = "客户已存在")
    })
    public Result<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("Received create customer request: customerName={}, customerType={}, category={}", 
                request.getCustomerName(), request.getCustomerType(), request.getCategory());
        
        CustomerResponse response = customerService.createCustomer(request);
        return Result.success(response);
    }

    /**
     * 更新客户信息
     * 
     * <p>更新客户的基本信息，不包括客户类型和分类。</p>
     * 
     * @param customerId 客户ID
     * @param request 更新请求
     * @return 更新后的客户信息
     */
    @PutMapping("/{customerId}")
    @Operation(
        summary = "更新客户信息", 
        description = "更新客户的基本信息"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "客户不存在")
    })
    public Result<CustomerResponse> updateCustomer(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {
        log.info("Received update customer request: customerId={}", customerId);
        
        CustomerResponse response = customerService.updateCustomer(customerId, request);
        return Result.success(response);
    }

    /**
     * 查询客户详情
     * 
     * <p>根据客户ID查询客户详细信息。</p>
     * 
     * @param customerId 客户ID
     * @return 客户详情
     */
    @GetMapping("/{customerId}")
    @Operation(
        summary = "查询客户详情", 
        description = "根据客户ID查询客户详细信息"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "客户不存在")
    })
    public Result<CustomerResponse> getCustomer(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId) {
        log.info("Received get customer request: customerId={}", customerId);
        
        CustomerResponse response = customerService.getCustomerById(customerId);
        return Result.success(response);
    }

    /**
     * 根据客户编号查询
     * 
     * <p>根据客户编号查询客户详情。</p>
     * 
     * @param customerNo 客户编号
     * @return 客户详情
     */
    @GetMapping("/no/{customerNo}")
    @Operation(
        summary = "根据客户编号查询", 
        description = "根据客户编号查询客户详情"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "客户不存在")
    })
    public Result<CustomerResponse> getCustomerByNo(
            @Parameter(description = "客户编号", required = true) @PathVariable String customerNo) {
        log.info("Received get customer by no request: customerNo={}", customerNo);
        
        CustomerResponse response = customerService.getCustomerByNo(customerNo);
        return Result.success(response);
    }

    /**
     * 查询集团下所有客户
     * 
     * <p>查询指定集团下的所有有效客户。</p>
     * 
     * @param groupId 集团ID
     * @return 客户列表
     */
    @GetMapping("/group/{groupId}")
    @Operation(
        summary = "查询集团下所有客户", 
        description = "查询指定集团下的所有有效客户"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<CustomerResponse>> getCustomersByGroupId(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId) {
        log.info("Received get customers by groupId request: groupId={}", groupId);
        
        List<CustomerResponse> responses = customerService.getCustomersByGroupId(groupId);
        return Result.success(responses);
    }

    /**
     * 按客户类型查询
     * 
     * <p>根据客户类型（对公/个人/同业）查询客户列表。</p>
     * 
     * @param customerType 客户类型
     * @return 客户列表
     */
    @GetMapping("/type/{customerType}")
    @Operation(
        summary = "按客户类型查询", 
        description = "根据客户类型查询客户列表"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<CustomerResponse>> getCustomersByType(
            @Parameter(description = "客户类型", required = true) @PathVariable String customerType) {
        log.info("Received get customers by type request: customerType={}", customerType);
        
        List<CustomerResponse> responses = customerService.getCustomersByType(customerType);
        return Result.success(responses);
    }

    /**
     * 按客户分类查询
     * 
     * <p>根据客户分类（集团客户/单一客户）查询客户列表。</p>
     * 
     * @param category 客户分类
     * @return 客户列表
     */
    @GetMapping("/category/{category}")
    @Operation(
        summary = "按客户分类查询", 
        description = "根据客户分类查询客户列表"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<CustomerResponse>> getCustomersByCategory(
            @Parameter(description = "客户分类", required = true) @PathVariable String category) {
        log.info("Received get customers by category request: category={}", category);
        
        List<CustomerResponse> responses = customerService.getCustomersByCategory(category);
        return Result.success(responses);
    }

    /**
     * 按风险等级查询
     * 
     * <p>根据风险等级（R1-R5）查询客户列表。</p>
     * 
     * @param riskLevel 风险等级
     * @return 客户列表
     */
    @GetMapping("/risk-level/{riskLevel}")
    @Operation(
        summary = "按风险等级查询", 
        description = "根据风险等级查询客户列表"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<CustomerResponse>> getCustomersByRiskLevel(
            @Parameter(description = "风险等级", required = true) @PathVariable String riskLevel) {
        log.info("Received get customers by risk level request: riskLevel={}", riskLevel);
        
        List<CustomerResponse> responses = customerService.getCustomersByRiskLevel(riskLevel);
        return Result.success(responses);
    }

    /**
     * 冻结客户
     * 
     * <p>冻结指定客户。冻结后客户相关业务操作将被暂停。</p>
     * 
     * @param customerId 客户ID
     * @param reason 冻结原因
     * @param operator 操作人
     * @return 客户信息
     */
    @PostMapping("/{customerId}/freeze")
    @Operation(
        summary = "冻结客户", 
        description = "冻结指定客户，暂停相关业务操作"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "冻结成功"),
        @ApiResponse(responseCode = "404", description = "客户不存在"),
        @ApiResponse(responseCode = "400", description = "客户已冻结")
    })
    public Result<CustomerResponse> freezeCustomer(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId,
            @Parameter(description = "冻结原因", required = true) @RequestParam String reason,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        log.info("Received freeze customer request: customerId={}, reason={}", customerId, reason);
        
        CustomerResponse response = customerService.freezeCustomer(customerId, reason, operator);
        return Result.success(response);
    }

    /**
     * 解冻客户
     * 
     * <p>解冻已冻结的客户，恢复正常状态。</p>
     * 
     * @param customerId 客户ID
     * @param operator 操作人
     * @return 客户信息
     */
    @PostMapping("/{customerId}/unfreeze")
    @Operation(
        summary = "解冻客户", 
        description = "解冻已冻结的客户"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "解冻成功"),
        @ApiResponse(responseCode = "404", description = "客户不存在"),
        @ApiResponse(responseCode = "400", description = "客户未冻结")
    })
    public Result<CustomerResponse> unfreezeCustomer(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        log.info("Received unfreeze customer request: customerId={}", customerId);
        
        CustomerResponse response = customerService.unfreezeCustomer(customerId, operator);
        return Result.success(response);
    }

    /**
     * 停用客户
     * 
     * <p>停用指定客户（不可逆操作）。</p>
     * 
     * @param customerId 客户ID
     * @param reason 停用原因
     * @param operator 操作人
     * @return 客户信息
     */
    @PostMapping("/{customerId}/disable")
    @Operation(
        summary = "停用客户", 
        description = "停用指定客户（不可逆操作）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "停用成功"),
        @ApiResponse(responseCode = "404", description = "客户不存在")
    })
    public Result<CustomerResponse> disableCustomer(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId,
            @Parameter(description = "停用原因", required = true) @RequestParam String reason,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        log.info("Received disable customer request: customerId={}, reason={}", customerId, reason);
        
        CustomerResponse response = customerService.disableCustomer(customerId, reason, operator);
        return Result.success(response);
    }

    /**
     * 更新客户风险等级
     * 
     * <p>更新客户的风险等级（R1-R5）。</p>
     * 
     * @param customerId 客户ID
     * @param riskLevel 风险等级
     * @param reason 变更原因
     * @param operator 操作人
     * @return 客户信息
     */
    @PostMapping("/{customerId}/risk-level")
    @Operation(
        summary = "更新客户风险等级", 
        description = "更新客户的风险等级（R1-R5）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "客户不存在")
    })
    public Result<CustomerResponse> updateRiskLevel(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId,
            @Parameter(description = "风险等级", required = true) @RequestParam String riskLevel,
            @Parameter(description = "变更原因", required = true) @RequestParam String reason,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        log.info("Received update risk level request: customerId={}, riskLevel={}", customerId, riskLevel);
        
        CustomerResponse response = customerService.updateRiskLevel(customerId, riskLevel, reason, operator);
        return Result.success(response);
    }

    /**
     * 分页查询客户
     * 
     * <p>支持多条件筛选的分页查询。</p>
     * 
     * @param request 查询请求
     * @return 分页结果
     */
    @PostMapping("/query")
    @Operation(
        summary = "分页查询客户", 
        description = "支持多条件筛选的分页查询"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<CustomerQueryResponse> queryCustomers(
            @RequestBody CustomerQueryRequest request) {
        log.info("Received query customers request: pageNum={}, pageSize={}", 
                request.getPageNum(), request.getPageSize());
        
        CustomerQueryResponse response = customerService.queryCustomers(request);
        return Result.success(response);
    }

    /**
     * 获取统一客户视图
     * 
     * <p>整合展示客户的基本信息、额度总额、已用额度、可用额度、
     * 额度使用记录及客户类型等关键信息。</p>
     * 
     * @param customerId 客户ID
     * @return 统一客户视图
     */
    @GetMapping("/{customerId}/unified-view")
    @Operation(
        summary = "获取统一客户视图", 
        description = "整合展示客户基本信息、额度信息、风险信息、白名单状态等"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = CustomerUnifiedViewResponse.class))),
        @ApiResponse(responseCode = "404", description = "客户不存在")
    })
    public Result<CustomerUnifiedViewResponse> getUnifiedView(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId) {
        log.info("Received get unified view request: customerId={}", customerId);
        
        CustomerUnifiedViewResponse response = customerService.getUnifiedView(customerId);
        return Result.success(response);
    }

    /**
     * 按名称搜索客户
     * 
     * <p>根据客户名称模糊查询客户列表。</p>
     * 
     * @param customerName 客户名称
     * @return 客户列表
     */
    @GetMapping("/search")
    @Operation(
        summary = "按名称搜索客户", 
        description = "根据客户名称模糊查询客户列表"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<CustomerResponse>> searchByName(
            @Parameter(description = "客户名称", required = true) @RequestParam String customerName) {
        log.info("Received search customer by name request: customerName={}", customerName);
        
        List<CustomerResponse> responses = customerService.searchByName(customerName);
        return Result.success(responses);
    }
}
