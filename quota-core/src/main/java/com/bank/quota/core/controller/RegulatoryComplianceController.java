package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.compliance.ComplianceCheckRequest;
import com.bank.quota.core.dto.compliance.ComplianceCheckResult;
import com.bank.quota.core.service.RegulatoryComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 监管指标校验控制器
 * 
 * <p>提供银行级信贷额度管控平台的监管指标校验REST API接口。
 * 包括资本充足率、贷款集中度、流动性比率等监管指标的校验功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 * @see RegulatoryComplianceService
 */
@Tag(name = "监管指标校验", description = "监管合规指标校验接口")
@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
public class RegulatoryComplianceController {
    
    private final RegulatoryComplianceService regulatoryComplianceService;
    
    /**
     * 校验资本充足率
     * 
     * <p>校验银行资本充足率是否符合监管要求（通常不低于8%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @Operation(summary = "校验资本充足率", description = "校验银行资本充足率是否符合监管要求")
    @PostMapping("/capital-adequacy-ratio")
    public Result<ComplianceCheckResult> checkCapitalAdequacyRatio(@Valid @RequestBody ComplianceCheckRequest request) {
        ComplianceCheckResult result = regulatoryComplianceService.checkCapitalAdequacyRatio(request);
        return Result.success(result);
    }
    
    /**
     * 校验贷款集中度
     * 
     * <p>校验单一客户贷款集中度是否符合监管要求（通常不超过10%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @Operation(summary = "校验贷款集中度", description = "校验单一客户贷款集中度是否符合监管要求")
    @PostMapping("/loan-concentration")
    public Result<ComplianceCheckResult> checkLoanConcentration(@Valid @RequestBody ComplianceCheckRequest request) {
        ComplianceCheckResult result = regulatoryComplianceService.checkLoanConcentration(request);
        return Result.success(result);
    }
    
    /**
     * 校验流动性比率
     * 
     * <p>校验银行流动性比率是否符合监管要求。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @Operation(summary = "校验流动性比率", description = "校验银行流动性比率是否符合监管要求")
    @PostMapping("/liquidity-ratio")
    public Result<ComplianceCheckResult> checkLiquidityRatio(@Valid @RequestBody ComplianceCheckRequest request) {
        ComplianceCheckResult result = regulatoryComplianceService.checkLiquidityRatio(request);
        return Result.success(result);
    }
    
    /**
     * 校验不良贷款率
     * 
     * <p>校验银行不良贷款率是否符合监管要求（通常不超过5%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @Operation(summary = "校验不良贷款率", description = "校验银行不良贷款率是否符合监管要求")
    @PostMapping("/non-performing-loan-ratio")
    public Result<ComplianceCheckResult> checkNonPerformingLoanRatio(@Valid @RequestBody ComplianceCheckRequest request) {
        ComplianceCheckResult result = regulatoryComplianceService.checkNonPerformingLoanRatio(request);
        return Result.success(result);
    }
    
    /**
     * 校验拨备覆盖率
     * 
     * <p>校验银行拨备覆盖率是否符合监管要求（通常不低于150%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @Operation(summary = "校验拨备覆盖率", description = "校验银行拨备覆盖率是否符合监管要求")
    @PostMapping("/provision-coverage-ratio")
    public Result<ComplianceCheckResult> checkProvisionCoverageRatio(@Valid @RequestBody ComplianceCheckRequest request) {
        ComplianceCheckResult result = regulatoryComplianceService.checkProvisionCoverageRatio(request);
        return Result.success(result);
    }
    
    /**
     * 校验杠杆率
     * 
     * <p>校验银行杠杆率是否符合监管要求。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @Operation(summary = "校验杠杆率", description = "校验银行杠杆率是否符合监管要求")
    @PostMapping("/leverage-ratio")
    public Result<ComplianceCheckResult> checkLeverageRatio(@Valid @RequestBody ComplianceCheckRequest request) {
        ComplianceCheckResult result = regulatoryComplianceService.checkLeverageRatio(request);
        return Result.success(result);
    }
    
    /**
     * 校验单一集团客户授信集中度
     * 
     * <p>校验对单一集团客户的授信集中度是否符合监管要求（通常不超过15%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @Operation(summary = "校验集团授信集中度", description = "校验对单一集团客户的授信集中度是否符合监管要求")
    @PostMapping("/group-credit-concentration")
    public Result<ComplianceCheckResult> checkGroupCreditConcentration(@Valid @RequestBody ComplianceCheckRequest request) {
        ComplianceCheckResult result = regulatoryComplianceService.checkGroupCreditConcentration(request);
        return Result.success(result);
    }
    
    /**
     * 执行全面监管合规检查
     * 
     * <p>执行所有监管指标的全面合规检查。</p>
     * 
     * @param request 校验请求
     * @return 综合校验结果
     */
    @Operation(summary = "全面监管合规检查", description = "执行所有监管指标的全面合规检查")
    @PostMapping("/full-check")
    public Result<ComplianceCheckResult> performFullComplianceCheck(@Valid @RequestBody ComplianceCheckRequest request) {
        ComplianceCheckResult result = regulatoryComplianceService.performFullComplianceCheck(request);
        return Result.success(result);
    }
    
    /**
     * 获取监管指标阈值配置
     * 
     * <p>获取当前监管指标的阈值配置。</p>
     * 
     * @return 阈值配置信息
     */
    @Operation(summary = "获取监管指标阈值", description = "获取当前监管指标的阈值配置")
    @GetMapping("/thresholds")
    public Result<Object> getRegulatoryThresholds() {
        Object thresholds = regulatoryComplianceService.getRegulatoryThresholds();
        return Result.success(thresholds);
    }
}