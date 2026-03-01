package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.quotacontrol.*;
import com.bank.quota.core.service.QuotaControlService;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * 额度控制控制器
 * 
 * <p>提供银行级信贷额度管控平台的核心额度控制REST API接口。
 * 包括额度锁定、占用、释放、校验等核心操作。</p>
 * 
 * <h3>功能概述：</h3>
 * <ul>
 *   <li>额度锁定：临时锁定额度，防止并发操作导致超用</li>
 *   <li>额度占用：实际占用额度，扣减可用额度</li>
 *   <li>额度释放：释放已占用或锁定的额度</li>
 *   <li>额度校验：校验额度充足性</li>
 *   <li>额度查询：查询各级额度使用情况</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-10
 * @see QuotaControlService
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/quota-control")
@RequiredArgsConstructor
@Tag(name = "额度控制管理", description = "额度锁定、占用、释放、校验接口")
public class QuotaControlController {
    
    private final QuotaControlService quotaControlService;

    /**
     * 锁定额度
     * 
     * <p>临时锁定指定金额的额度，防止在业务处理过程中被其他操作占用。
     * 锁定操作会扣减可用额度，增加已用额度，但不会实际占用额度。</p>
     * 
     * @param request 锁定请求
     * @return 锁定结果
     */
    @PostMapping("/lock")
    @Operation(
        summary = "锁定额度", 
        description = "临时锁定指定金额的额度，防止并发操作导致超用"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "锁定成功",
            content = @Content(schema = @Schema(implementation = QuotaLockResult.class))),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "404", description = "额度不存在"),
        @ApiResponse(responseCode = "409", description = "额度不足或已冻结")
    })
    public Result<QuotaLockResult> lockQuota(
            @Valid @RequestBody QuotaLockRequest request) {
        log.info("Received lock quota request: objectType={}, objectId={}, amount={}", 
                request.getObjectType(), request.getObjectId(), request.getAmount());
        
        QuotaLockResult result = quotaControlService.lockQuota(request);
        return Result.success(result);
    }

    /**
     * 解锁额度
     * 
     * <p>释放之前锁定的额度，恢复可用额度。</p>
     * 
     * @param lockId 锁定ID
     * @return 操作结果
     */
    @PostMapping("/unlock")
    @Operation(
        summary = "解锁额度", 
        description = "释放之前锁定的额度，恢复可用额度"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "解锁成功"),
        @ApiResponse(responseCode = "404", description = "锁定记录不存在")
    })
    public Result<Void> unlockQuota(
            @Parameter(description = "锁定ID", required = true) @RequestParam String lockId) {
        log.info("Received unlock quota request: lockId={}", lockId);
        
        quotaControlService.unlockQuota(lockId);
        return Result.success();
    }

    /**
     * 占用额度
     * 
     * <p>实际占用指定金额的额度，这是额度使用的最终确认操作。
     * 占用操作会创建合同占用记录，并扣减各级额度的可用额度。</p>
     * 
     * @param request 占用请求
     * @return 占用结果
     */
    @PostMapping("/occupy")
    @Operation(
        summary = "占用额度", 
        description = "实际占用指定金额的额度，创建合同占用记录"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "占用成功",
            content = @Content(schema = @Schema(implementation = QuotaOccupyResult.class))),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "404", description = "批复额度不存在"),
        @ApiResponse(responseCode = "409", description = "额度不足")
    })
    public Result<QuotaOccupyResult> occupyQuota(
            @Valid @RequestBody QuotaOccupyRequest request) {
        log.info("Received occupy quota request: contractNo={}, amount={}", 
                request.getContractNo(), request.getAmount());
        
        QuotaOccupyResult result = quotaControlService.occupyQuota(request);
        return Result.success(result);
    }

    /**
     * 释放额度
     * 
     * <p>释放已占用的额度，恢复可用额度。支持全额释放和部分释放。</p>
     * 
     * @param occupyId 占用ID
     * @param amount 释放金额（可选，不传则全额释放）
     * @return 操作结果
     */
    @PostMapping("/release")
    @Operation(
        summary = "释放额度", 
        description = "释放已占用的额度，恢复可用额度"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "释放成功"),
        @ApiResponse(responseCode = "404", description = "占用记录不存在"),
        @ApiResponse(responseCode = "400", description = "占用记录已释放")
    })
    public Result<Void> releaseQuota(
            @Parameter(description = "占用ID", required = true) @RequestParam String occupyId,
            @Parameter(description = "释放金额（可选，不传则全额释放）") 
            @RequestParam(required = false) BigDecimal amount) {
        log.info("Received release quota request: occupyId={}, amount={}", occupyId, amount);
        
        quotaControlService.releaseQuota(occupyId, amount);
        return Result.success();
    }

    /**
     * 校验额度充足性
     * 
     * <p>校验指定金额是否在可用额度范围内，不实际扣减额度。
     * 该方法用于在业务操作前预校验额度是否充足。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/validate")
    @Operation(
        summary = "校验额度充足性", 
        description = "校验指定金额是否在可用额度范围内"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "校验完成",
            content = @Content(schema = @Schema(implementation = QuotaValidateResult.class)))
    })
    public Result<QuotaValidateResult> validateQuota(
            @Valid @RequestBody QuotaValidateRequest request) {
        log.info("Received validate quota request: objectType={}, objectId={}, amount={}", 
                request.getObjectType(), request.getObjectId(), request.getAmount());
        
        QuotaValidateResult result = quotaControlService.validateQuota(request);
        return Result.success(result);
    }

    /**
     * 查询额度信息
     * 
     * <p>查询指定对象的额度使用情况，包括总额度、已用额度、可用额度等。</p>
     * 
     * @param objectType 对象类型（GROUP/CUSTOMER/APPROVAL）
     * @param objectId 对象ID
     * @return 额度查询结果
     */
    @GetMapping("/query")
    @Operation(
        summary = "查询额度信息", 
        description = "查询指定对象的额度使用情况"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = QuotaQueryResult.class)))
    })
    public Result<QuotaQueryResult> queryQuota(
            @Parameter(description = "对象类型（GROUP/CUSTOMER/APPROVAL）", required = true) 
            @RequestParam String objectType,
            @Parameter(description = "对象ID", required = true) 
            @RequestParam Long objectId) {
        log.info("Received query quota request: objectType={}, objectId={}", objectType, objectId);
        
        QuotaQueryRequest request = new QuotaQueryRequest();
        request.setObjectType(objectType);
        request.setObjectId(objectId);
        
        QuotaQueryResult result = quotaControlService.queryQuota(request);
        return Result.success(result);
    }

    /**
     * 获取活跃锁定记录
     * 
     * <p>查询指定对象当前活跃的额度锁定记录。</p>
     * 
     * @param objectType 对象类型
     * @param objectId 对象ID
     * @return 锁定记录列表
     */
    @GetMapping("/active-locks")
    @Operation(
        summary = "获取活跃锁定记录", 
        description = "查询指定对象当前活跃的额度锁定记录"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<QuotaLockResult>> getActiveLocks(
            @Parameter(description = "对象类型", required = true) @RequestParam String objectType,
            @Parameter(description = "对象ID", required = true) @RequestParam Long objectId) {
        log.info("Received get active locks request: objectType={}, objectId={}", objectType, objectId);
        
        List<QuotaLockResult> results = quotaControlService.getActiveLocks(objectType, objectId);
        return Result.success(results);
    }

    /**
     * 多占额度
     * 
     * <p>根据多占规则占用额度，支持同一额度被多个业务同时占用。</p>
     * 
     * @param request 占用请求
     * @return 占用结果
     */
    @PostMapping("/multi-occupy")
    @Operation(
        summary = "多占额度", 
        description = "根据多占规则占用额度，支持同一额度被多个业务同时占用"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "多占成功",
            content = @Content(schema = @Schema(implementation = QuotaOccupyResult.class))),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "404", description = "批复额度不存在"),
        @ApiResponse(responseCode = "409", description = "不允许多占或额度不足")
    })
    public Result<QuotaOccupyResult> multiOccupyQuota(
            @Valid @RequestBody QuotaOccupyRequest request) {
        log.info("Received multi-occupy quota request: contractNo={}, amount={}, businessType={}", 
                request.getContractNo(), request.getAmount(), request.getBusinessType());
        
        QuotaOccupyResult result = quotaControlService.multiOccupyQuota(request);
        return Result.success(result);
    }

    /**
     * 多占校验
     * 
     * <p>根据多占规则校验额度是否足够，支持多种占用模式。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/validate-multi")
    @Operation(
        summary = "多占校验", 
        description = "根据多占规则校验额度是否足够"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "校验完成",
            content = @Content(schema = @Schema(implementation = QuotaValidateResult.class)))
    })
    public Result<QuotaValidateResult> validateMultiOccupancy(
            @Valid @RequestBody QuotaValidateRequest request) {
        log.info("Received validate multi-occupancy request: objectType={}, objectId={}, amount={}, businessType={}", 
                request.getObjectType(), request.getObjectId(), request.getAmount(), request.getBusinessType());
        
        QuotaValidateResult result = quotaControlService.validateMultiOccupancy(request);
        return Result.success(result);
    }
}
