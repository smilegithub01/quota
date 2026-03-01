package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.domain.Whitelist;
import com.bank.quota.core.dto.whitelist.*;
import com.bank.quota.core.service.WhitelistService;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 白名单管理控制器
 * 
 * <p>提供银行级信贷额度管控平台的白名单管理REST API接口。
 * 白名单机制用于对特定客户或业务场景提供额度管控豁免或优惠处理。</p>
 * 
 * <h3>功能概述：</h3>
 * <ul>
 *   <li>白名单申请与审批管理</li>
 *   <li>白名单状态查询与校验</li>
 *   <li>白名单批量导入与管理</li>
 *   <li>白名单豁免规则应用</li>
 * </ul>
 * 
 * <h3>业务规则：</h3>
 * <ul>
 *   <li>白名单创建后需经审批才能生效</li>
 *   <li>白名单具有有效期，过期自动失效</li>
 *   <li>支持全额豁免、部分豁免、阈值豁免三种规则</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-10
 * @see WhitelistService
 * @see WhitelistResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/whitelist")
@RequiredArgsConstructor
@Tag(name = "白名单管理", description = "白名单申请、审批、查询、删除接口")
public class WhitelistController {
    
    private final WhitelistService whitelistService;
    
    /**
     * 添加白名单
     * 
     * <p>将客户添加到白名单。此接口为简化版添加接口，
     * 创建后白名单状态为待审批，需经过审批流程才能生效。</p>
     * 
     * <h4>请求示例：</h4>
     * <pre>{@code
     * {
     *   "customerId": 1001,
     *   "customerName": "测试客户",
     *   "whitelistType": "VIP_CUSTOMER",
     *   "description": "VIP客户白名单",
     *   "createBy": "admin"
     * }
     * }</pre>
     * 
     * @param request 添加白名单请求
     * @return 白名单信息
     */
    @PostMapping
    @Operation(
        summary = "添加白名单", 
        description = "将客户添加到白名单，创建后需审批才能生效"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "添加成功"),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "409", description = "白名单已存在")
    })
    public Result<WhitelistResponse> addWhitelist(
            @Valid @RequestBody AddWhitelistRequest request) {
        log.info("Received add whitelist request: customerId={}, type={}", 
                request.getCustomerId(), request.getWhitelistType());
        
        Whitelist whitelist = Whitelist.builder()
                .whitelistType(Whitelist.WhitelistType.valueOf(request.getWhitelistType()))
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .businessType(request.getBusinessType())
                .exemptRule(Whitelist.ExemptRule.FULL)
                .effectiveTime(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusYears(1))
                .applyReason(request.getDescription())
                .applicant(request.getCreateBy())
                .build();
        
        Whitelist saved = whitelistService.applyWhitelist(whitelist);
        return Result.success(buildResponseFromEntity(saved));
    }
    
    /**
     * 申请白名单
     * 
     * <p>提交白名单申请，等待审批。此接口为完整版申请接口，
     * 支持配置豁免规则、有效期等详细信息。</p>
     * 
     * @param request 申请请求
     * @return 申请结果
     */
    @PostMapping("/apply")
    @Operation(
        summary = "申请白名单", 
        description = "提交白名单申请，等待审批（完整版，支持配置豁免规则）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "申请成功"),
        @ApiResponse(responseCode = "400", description = "参数校验失败")
    })
    public Result<WhitelistApplyResponse> applyWhitelist(
            @Valid @RequestBody WhitelistApplyRequest request) {
        log.info("Received whitelist apply request: customerId={}, type={}", 
                request.getCustomerId(), request.getWhitelistType());
        
        Whitelist whitelist = buildWhitelistFromRequest(request);
        Whitelist saved = whitelistService.applyWhitelist(whitelist);
        
        WhitelistApplyResponse response = new WhitelistApplyResponse();
        response.setWhitelistNo(saved.getWhitelistNo());
        response.setStatus(saved.getStatus().name());
        response.setMessage("白名单申请已提交，等待审批");
        
        return Result.success(response);
    }
    
    /**
     * 审批白名单
     * 
     * <p>审批已提交的白名单申请。审批通过后白名单状态变为生效。</p>
     * 
     * @param request 审批请求
     * @return 审批后的白名单信息
     */
    @PostMapping("/approve")
    @Operation(
        summary = "审批白名单", 
        description = "审批已提交的白名单申请"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "审批成功"),
        @ApiResponse(responseCode = "404", description = "白名单不存在"),
        @ApiResponse(responseCode = "400", description = "白名单状态不允许审批")
    })
    public Result<WhitelistResponse> approveWhitelist(
            @Valid @RequestBody WhitelistApproveRequest request) {
        log.info("Approving whitelist: {}, approver: {}", 
                request.getWhitelistNo(), request.getApprover());
        
        whitelistService.approveWhitelist(
                request.getWhitelistNo(),
                request.isApproved(),
                request.getApproveRemark(),
                request.getApprover());
        
        Whitelist whitelist = whitelistService.getByWhitelistNo(request.getWhitelistNo())
                .orElseThrow(() -> new RuntimeException("白名单不存在"));
        
        return Result.success(buildResponseFromEntity(whitelist));
    }
    
    /**
     * 查询白名单详情
     * 
     * <p>根据白名单ID查询白名单详情。</p>
     * 
     * @param id 白名单ID
     * @return 白名单详情
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "查询白名单", 
        description = "根据ID查询白名单详情"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "白名单不存在")
    })
    public Result<WhitelistResponse> getWhitelistById(
            @Parameter(description = "白名单ID", required = true) @PathVariable Long id) {
        log.info("Getting whitelist by id: {}", id);
        
        Whitelist whitelist = whitelistService.getByWhitelistNo(id.toString())
                .orElseThrow(() -> new RuntimeException("白名单不存在: " + id));
        
        return Result.success(buildResponseFromEntity(whitelist));
    }
    
    /**
     * 根据白名单编号查询
     * 
     * <p>根据白名单编号查询白名单详情。</p>
     * 
     * @param whitelistNo 白名单编号
     * @return 白名单详情
     */
    @GetMapping("/no/{whitelistNo}")
    @Operation(
        summary = "根据编号查询白名单", 
        description = "根据白名单编号查询详情"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "白名单不存在")
    })
    public Result<WhitelistResponse> getWhitelistByNo(
            @Parameter(description = "白名单编号", required = true) @PathVariable String whitelistNo) {
        log.info("Getting whitelist by no: {}", whitelistNo);
        
        Whitelist whitelist = whitelistService.getByWhitelistNo(whitelistNo)
                .orElseThrow(() -> new RuntimeException("白名单不存在: " + whitelistNo));
        
        return Result.success(buildResponseFromEntity(whitelist));
    }
    
    /**
     * 检查客户是否在白名单
     * 
     * <p>检查指定客户是否在白名单中，返回白名单状态和类型。
     * 此接口用于快速判断客户是否享有白名单特权。</p>
     * 
     * @param customerId 客户ID
     * @return 白名单检查结果
     */
    @GetMapping("/check/{customerId}")
    @Operation(
        summary = "检查客户是否在白名单", 
        description = "检查客户是否在白名单中，返回白名单状态"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<WhitelistCheckResponse> checkWhitelist(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId) {
        log.info("Received check whitelist request: customerId={}", customerId);
        
        List<Whitelist> whitelists = whitelistService.getActiveByCustomerId(customerId);
        
        WhitelistCheckResponse response = new WhitelistCheckResponse();
        response.setCustomerId(customerId);
        response.setInWhitelist(!whitelists.isEmpty());
        
        if (!whitelists.isEmpty()) {
            Whitelist whitelist = whitelists.get(0);
            response.setWhitelistType(whitelist.getWhitelistType().name());
            response.setWhitelistNo(whitelist.getWhitelistNo());
            response.setEffectiveTime(whitelist.getEffectiveTime());
            response.setExpiryTime(whitelist.getExpiryTime());
            response.setExemptRule(whitelist.getExemptRule().name());
            response.setExemptAmount(whitelist.getExemptAmount());
        }
        
        return Result.success(response);
    }
    
    /**
     * 根据客户ID查询白名单
     * 
     * <p>查询指定客户的所有白名单记录。</p>
     * 
     * @param customerId 客户ID
     * @return 白名单列表
     */
    @GetMapping("/customer/{customerId}")
    @Operation(
        summary = "根据客户ID查询白名单", 
        description = "查询指定客户的所有白名单记录"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<WhitelistResponse>> getWhitelistsByCustomerId(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId) {
        log.info("Getting whitelists for customer: {}", customerId);
        
        List<Whitelist> whitelists = whitelistService.getActiveByCustomerId(customerId);
        
        List<WhitelistResponse> responses = whitelists.stream()
                .map(this::buildResponseFromEntity)
                .collect(Collectors.toList());
        
        return Result.success(responses);
    }
    
    /**
     * 查询客户有效白名单
     * 
     * <p>查询指定客户当前有效的白名单列表。</p>
     * 
     * @param customerId 客户ID
     * @return 有效白名单列表
     */
    @GetMapping("/customer/{customerId}/active")
    @Operation(
        summary = "查询客户有效白名单", 
        description = "查询指定客户当前有效的白名单列表"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<WhitelistResponse>> getActiveWhitelistsByCustomer(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId) {
        log.info("Getting active whitelists for customer: {}", customerId);
        
        List<Whitelist> whitelists = whitelistService.getActiveByCustomerId(customerId);
        
        List<WhitelistResponse> responses = whitelists.stream()
                .map(this::buildResponseFromEntity)
                .collect(Collectors.toList());
        
        return Result.success(responses);
    }
    
    /**
     * 查询客户业务类型有效白名单
     * 
     * <p>查询指定客户在指定业务类型下的有效白名单。</p>
     * 
     * @param customerId 客户ID
     * @param businessType 业务类型
     * @return 有效白名单列表
     */
    @GetMapping("/customer/{customerId}/business/{businessType}/active")
    @Operation(
        summary = "查询客户业务类型有效白名单", 
        description = "查询指定客户在指定业务类型下的有效白名单"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<WhitelistResponse>> getActiveWhitelistsByCustomerAndBusiness(
            @Parameter(description = "客户ID", required = true) @PathVariable Long customerId,
            @Parameter(description = "业务类型", required = true) @PathVariable String businessType) {
        log.info("Getting active whitelists for customer: {}, businessType: {}", 
                customerId, businessType);
        
        List<Whitelist> whitelists = whitelistService.getActiveByCustomerIdAndBusinessType(
                customerId, businessType);
        
        List<WhitelistResponse> responses = whitelists.stream()
                .map(this::buildResponseFromEntity)
                .collect(Collectors.toList());
        
        return Result.success(responses);
    }
    
    /**
     * 按状态查询白名单
     * 
     * <p>根据状态查询白名单列表。</p>
     * 
     * @param status 状态（PENDING/ACTIVE/INVALID）
     * @return 白名单列表
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "按状态查询白名单", 
        description = "根据状态查询白名单列表"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<WhitelistResponse>> getWhitelistsByStatus(
            @Parameter(description = "状态", required = true) @PathVariable String status) {
        log.info("Getting whitelists by status: {}", status);
        
        Whitelist.WhitelistStatus whitelistStatus = Whitelist.WhitelistStatus.valueOf(status.toUpperCase());
        List<Whitelist> whitelists = whitelistService.getByStatus(whitelistStatus);
        
        List<WhitelistResponse> responses = whitelists.stream()
                .map(this::buildResponseFromEntity)
                .collect(Collectors.toList());
        
        return Result.success(responses);
    }
    
    /**
     * 按申请人查询白名单
     * 
     * <p>根据申请人查询其提交的白名单列表。</p>
     * 
     * @param applicant 申请人
     * @return 白名单列表
     */
    @GetMapping("/applicant/{applicant}")
    @Operation(
        summary = "按申请人查询白名单", 
        description = "根据申请人查询其提交的白名单列表"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<WhitelistResponse>> getWhitelistsByApplicant(
            @Parameter(description = "申请人", required = true) @PathVariable String applicant) {
        log.info("Getting whitelists by applicant: {}", applicant);
        
        List<Whitelist> whitelists = whitelistService.getByApplicant(applicant);
        
        List<WhitelistResponse> responses = whitelists.stream()
                .map(this::buildResponseFromEntity)
                .collect(Collectors.toList());
        
        return Result.success(responses);
    }
    
    /**
     * 查询所有有效白名单
     * 
     * <p>查询当前所有有效的白名单。</p>
     * 
     * @return 有效白名单列表
     */
    @GetMapping("/active")
    @Operation(
        summary = "查询所有有效白名单", 
        description = "查询当前所有有效的白名单"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<WhitelistResponse>> getAllActiveWhitelists() {
        log.info("Getting all active whitelists");
        
        List<Whitelist> whitelists = whitelistService.getAllActive(LocalDateTime.now());
        
        List<WhitelistResponse> responses = whitelists.stream()
                .map(this::buildResponseFromEntity)
                .collect(Collectors.toList());
        
        return Result.success(responses);
    }
    
    /**
     * 删除白名单
     * 
     * <p>从白名单中移除客户。此操作将白名单状态置为失效，
     * 并非物理删除，保留审计记录。</p>
     * 
     * @param id 白名单ID
     * @param operator 操作人
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "删除白名单", 
        description = "从白名单中移除客户（逻辑删除，保留审计记录）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "白名单不存在")
    })
    public Result<Void> deleteWhitelist(
            @Parameter(description = "白名单ID", required = true) @PathVariable Long id,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        log.info("Received delete whitelist request: id={}, operator={}", id, operator);
        
        whitelistService.invalidateWhitelist(id.toString(), "管理员删除", operator);
        return Result.success();
    }
    
    /**
     * 批量导入白名单
     * 
     * <p>批量导入白名单记录。导入的白名单默认状态为待审批，
     * 需经过审批流程才能生效。</p>
     * 
     * <h4>请求示例：</h4>
     * <pre>{@code
     * {
     *   "whitelists": [
     *     {
     *       "customerId": 1001,
     *       "customerName": "客户A",
     *       "whitelistType": "VIP_CUSTOMER",
     *       "businessType": "LOAN"
     *     }
     *   ],
     *   "createBy": "admin"
     * }
     * }</pre>
     * 
     * @param request 批量导入请求
     * @return 导入结果
     */
    @PostMapping("/batch-import")
    @Operation(
        summary = "批量导入白名单", 
        description = "批量导入白名单记录，导入后需审批才能生效"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导入成功"),
        @ApiResponse(responseCode = "400", description = "参数校验失败")
    })
    public Result<WhitelistBatchImportResponse> batchImportWhitelist(
            @Valid @RequestBody WhitelistBatchImportRequest request) {
        log.info("Received batch import whitelist request: count={}, createBy={}", 
                request.getWhitelists().size(), request.getCreateBy());
        
        int successCount = 0;
        int failCount = 0;
        List<String> errorMessages = new java.util.ArrayList<>();
        
        for (AddWhitelistRequest item : request.getWhitelists()) {
            try {
                Whitelist whitelist = Whitelist.builder()
                        .whitelistType(Whitelist.WhitelistType.valueOf(item.getWhitelistType()))
                        .customerId(item.getCustomerId())
                        .customerName(item.getCustomerName())
                        .businessType(item.getBusinessType())
                        .exemptRule(Whitelist.ExemptRule.FULL)
                        .effectiveTime(LocalDateTime.now())
                        .expiryTime(LocalDateTime.now().plusYears(1))
                        .applyReason(item.getDescription() != null ? item.getDescription() : "批量导入")
                        .applicant(request.getCreateBy())
                        .build();
                
                whitelistService.applyWhitelist(whitelist);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errorMessages.add(String.format("客户[%d]导入失败: %s", 
                        item.getCustomerId(), e.getMessage()));
                log.error("Failed to import whitelist for customer: {}", 
                        item.getCustomerId(), e);
            }
        }
        
        WhitelistBatchImportResponse response = new WhitelistBatchImportResponse();
        response.setTotalCount(request.getWhitelists().size());
        response.setSuccessCount(successCount);
        response.setFailCount(failCount);
        response.setErrorMessages(errorMessages);
        
        log.info("Batch import completed: total={}, success={}, fail={}", 
                request.getWhitelists().size(), successCount, failCount);
        
        return Result.success(response);
    }
    
    private Whitelist buildWhitelistFromRequest(WhitelistApplyRequest request) {
        return Whitelist.builder()
                .whitelistType(Whitelist.WhitelistType.valueOf(request.getWhitelistType()))
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .businessType(request.getBusinessType())
                .exemptRule(Whitelist.ExemptRule.valueOf(request.getExemptRule()))
                .exemptAmount(request.getExemptAmount())
                .effectiveTime(request.getEffectiveTime())
                .expiryTime(request.getExpiryTime())
                .applyReason(request.getApplyReason())
                .applicant(request.getApplicant())
                .applyTime(LocalDateTime.now())
                .build();
    }
    
    private WhitelistResponse buildResponseFromEntity(Whitelist whitelist) {
        WhitelistResponse response = new WhitelistResponse();
        response.setId(whitelist.getId());
        response.setWhitelistNo(whitelist.getWhitelistNo());
        response.setWhitelistType(whitelist.getWhitelistType() != null ? 
                whitelist.getWhitelistType().name() : null);
        response.setCustomerId(whitelist.getCustomerId());
        response.setCustomerName(whitelist.getCustomerName());
        response.setBusinessType(whitelist.getBusinessType());
        response.setExemptRule(whitelist.getExemptRule() != null ? 
                whitelist.getExemptRule().name() : null);
        response.setExemptAmount(whitelist.getExemptAmount());
        response.setEffectiveTime(whitelist.getEffectiveTime());
        response.setExpiryTime(whitelist.getExpiryTime());
        response.setStatus(whitelist.getStatus() != null ? whitelist.getStatus().name() : null);
        response.setApplyReason(whitelist.getApplyReason());
        response.setApproveRemark(whitelist.getApproveRemark());
        response.setApplicant(whitelist.getApplicant());
        response.setApprover(whitelist.getApprover());
        response.setApplyTime(whitelist.getApplyTime());
        response.setApproveTime(whitelist.getApproveTime());
        response.setCreateTime(whitelist.getCreateTime());
        response.setUpdateTime(whitelist.getUpdateTime());
        return response;
    }
}
