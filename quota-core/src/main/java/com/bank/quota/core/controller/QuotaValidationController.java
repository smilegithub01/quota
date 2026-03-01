package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.quotacontrol.QuotaValidateRequest;
import com.bank.quota.core.dto.quotacontrol.QuotaValidateResult;
import com.bank.quota.core.service.QuotaValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 额度验证控制器
 * 
 * <p>提供独立的额度充足性校验接口，支持单层、穿透、批量校验功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Tag(name = "额度验证管理", description = "额度充足性校验接口")
@RestController
@RequestMapping("/api/v1/quota")
@RequiredArgsConstructor
public class QuotaValidationController {
    
    private final QuotaValidationService quotaValidationService;
    
    /**
     * 额度充足性校验
     * 
     * <p>校验指定金额是否在可用额度范围内，支持穿透校验和批量校验。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @Operation(summary = "额度充足性校验", description = "校验指定金额是否在可用额度范围内")
    @PostMapping("/validate")
    public Result<QuotaValidateResult> validateQuota(@Valid @RequestBody QuotaValidateRequest request) {
        QuotaValidateResult result = quotaValidationService.validate(request);
        return Result.success(result);
    }
    
    /**
     * 按层级校验额度
     * 
     * <p>按指定层级校验额度充足性。</p>
     * 
     * @param request 校验请求
     * @param level 校验层级
     * @return 校验结果
     */
    @Operation(summary = "按层级校验额度", description = "按指定层级校验额度充足性")
    @PostMapping("/validate/level/{level}")
    public Result<QuotaValidateResult> validateQuotaByLevel(@Valid @RequestBody QuotaValidateRequest request,
            @PathVariable Integer level) {
        QuotaValidateResult result = quotaValidationService.validateByLevel(request, level);
        return Result.success(result);
    }
    
    /**
     * 校验所有层级
     * 
     * <p>校验所有层级的额度充足性。</p>
     * 
     * @param request 校验请求
     * @return 各层级校验结果
     */
    @Operation(summary = "校验所有层级", description = "校验所有层级的额度充足性")
    @PostMapping("/validate/all-levels")
    public Result<List<QuotaValidateResult>> validateAllLevels(@Valid @RequestBody QuotaValidateRequest request) {
        List<QuotaValidateResult> results = quotaValidationService.validateAllLevels(request);
        return Result.success(results);
    }
}