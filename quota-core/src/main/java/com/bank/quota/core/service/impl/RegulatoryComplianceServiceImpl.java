package com.bank.quota.core.service.impl;

import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.ApprovalQuota;
import com.bank.quota.core.domain.CustomerQuota;
import com.bank.quota.core.domain.GroupQuota;
import com.bank.quota.core.dto.compliance.ComplianceCheckRequest;
import com.bank.quota.core.dto.compliance.ComplianceCheckResult;
import com.bank.quota.core.repository.ApprovalQuotaRepository;
import com.bank.quota.core.repository.CustomerQuotaRepository;
import com.bank.quota.core.repository.GroupQuotaRepository;
import com.bank.quota.core.service.RegulatoryComplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 监管指标校验服务实现类
 * 
 * <p>提供银行级信贷额度管控平台的监管指标校验功能实现。
 * 用于校验各项业务指标是否符合监管要求。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegulatoryComplianceServiceImpl implements RegulatoryComplianceService {
    
    private final CustomerQuotaRepository customerQuotaRepository;
    private final GroupQuotaRepository groupQuotaRepository;
    private final ApprovalQuotaRepository approvalQuotaRepository;
    
    // 监管指标阈值常量
    private static final BigDecimal CAPITAL_ADEQUACY_RATIO_THRESHOLD = new BigDecimal("0.08"); // 8%
    private static final BigDecimal LOAN_CONCENTRATION_THRESHOLD = new BigDecimal("0.10"); // 10%
    private static final BigDecimal GROUP_CREDIT_CONCENTRATION_THRESHOLD = new BigDecimal("0.15"); // 15%
    private static final BigDecimal NON_PERFORMING_LOAN_RATIO_THRESHOLD = new BigDecimal("0.05"); // 5%
    private static final BigDecimal PROVISION_COVERAGE_RATIO_THRESHOLD = new BigDecimal("1.50"); // 150%
    private static final BigDecimal LIQUIDITY_RATIO_MIN = new BigDecimal("0.25"); // 25%
    private static final BigDecimal LEVERAGE_RATIO_THRESHOLD = new BigDecimal("0.04"); // 4%
    
    @Override
    public ComplianceCheckResult checkCapitalAdequacyRatio(ComplianceCheckRequest request) {
        log.info("Checking capital adequacy ratio: objectId={}, objectType={}", 
                request.getObjectId(), request.getObjectType());
        
        // 在实际应用中，这将连接到银行的核心系统获取真实的资本数据
        // 这里我们模拟一个计算过程
        BigDecimal tier1Capital = request.getBaselineValue(); // 一级资本
        BigDecimal riskWeightedAssets = request.getCheckValue(); // 风险加权资产
        
        if (tier1Capital == null || riskWeightedAssets == null || 
            riskWeightedAssets.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid input values for capital adequacy ratio calculation");
            return buildFailedResult("CAPITAL_ADEQUACY_CALC_ERROR", 
                "资本充足率计算参数无效", "CAPITAL_ADEQUACY_RATIO", request);
        }
        
        BigDecimal capitalAdequacyRatio = tier1Capital.divide(riskWeightedAssets, 4, RoundingMode.HALF_UP);
        boolean passed = capitalAdequacyRatio.compareTo(CAPITAL_ADEQUACY_RATIO_THRESHOLD) >= 0;
        
        String message = passed ? 
            String.format("资本充足率为%.2f%%，符合监管要求", capitalAdequacyRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)) :
            String.format("资本充足率为%.2f%%，低于监管要求的%.2f%%", 
                capitalAdequacyRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                CAPITAL_ADEQUACY_RATIO_THRESHOLD.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        log.info("Capital adequacy ratio check result: passed={}, ratio={}", passed, capitalAdequacyRatio);
        
        return ComplianceCheckResult.builder()
            .passed(passed)
            .resultCode(passed ? "COMPLIANT" : "NON_COMPLIANT")
            .message(message)
            .checkValue(capitalAdequacyRatio)
            .threshold(CAPITAL_ADEQUACY_RATIO_THRESHOLD)
            .actualValue(capitalAdequacyRatio)
            .checkType("CAPITAL_ADEQUACY_RATIO")
            .checkTime(LocalDateTime.now())
            .details("资本充足率 = 一级资本 / 风险加权资产")
            .build();
    }
    
    @Override
    public ComplianceCheckResult checkLoanConcentration(ComplianceCheckRequest request) {
        log.info("Checking loan concentration: customerId={}", request.getCustomerId());
        
        if (request.getCustomerId() == null) {
            log.warn("Customer ID is required for loan concentration check");
            return buildFailedResult("LOAN_CONCENTRATION_ERROR", 
                "客户ID不能为空", "LOAN_CONCENTRATION", request);
        }
        
        // 获取客户额度
        Optional<CustomerQuota> customerQuotaOpt = customerQuotaRepository.findByCustomerId(request.getCustomerId());
        if (!customerQuotaOpt.isPresent()) {
            log.warn("Customer quota not found: customerId={}", request.getCustomerId());
            return buildFailedResult("CUSTOMER_QUOTA_NOT_FOUND", 
                "客户额度不存在", "LOAN_CONCENTRATION", request);
        }
        
        CustomerQuota customerQuota = customerQuotaOpt.get();
        BigDecimal customerTotalQuota = customerQuota.getTotalQuota();
        
        // 获取集团总额度
        Optional<GroupQuota> groupQuotaOpt = groupQuotaRepository.findByGroupId(customerQuota.getGroupId());
        if (!groupQuotaOpt.isPresent()) {
            log.warn("Group quota not found: groupId={}", customerQuota.getGroupId());
            return buildFailedResult("GROUP_QUOTA_NOT_FOUND", 
                "集团额度不存在", "LOAN_CONCENTRATION", request);
        }
        
        GroupQuota groupQuota = groupQuotaOpt.get();
        BigDecimal groupTotalQuota = groupQuota.getTotalQuota();
        
        if (groupTotalQuota.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid group total quota: {}", groupTotalQuota);
            return buildFailedResult("INVALID_GROUP_QUOTA", 
                "集团总额度无效", "LOAN_CONCENTRATION", request);
        }
        
        // 计算贷款集中度
        BigDecimal loanConcentration = customerTotalQuota.divide(groupTotalQuota, 4, RoundingMode.HALF_UP);
        boolean passed = loanConcentration.compareTo(LOAN_CONCENTRATION_THRESHOLD) <= 0;
        
        String message = passed ? 
            String.format("客户贷款集中度为%.2f%%，符合监管要求", loanConcentration.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)) :
            String.format("客户贷款集中度为%.2f%%，超过监管要求的%.2f%%", 
                loanConcentration.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                LOAN_CONCENTRATION_THRESHOLD.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        log.info("Loan concentration check result: customerId={}, passed={}, ratio={}", 
                request.getCustomerId(), passed, loanConcentration);
        
        ComplianceCheckResult.ViolationDetail violation = null;
        if (!passed) {
            violation = ComplianceCheckResult.ViolationDetail.builder()
                .violationCode("HIGH_CONCENTRATION")
                .violationDescription("单一客户贷款集中度过高")
                .severity("ERROR")
                .currentValue(loanConcentration)
                .thresholdValue(LOAN_CONCENTRATION_THRESHOLD)
                .suggestion("考虑分散风险或将部分额度分配给其他客户")
                .build();
        }
        
        return ComplianceCheckResult.builder()
            .passed(passed)
            .resultCode(passed ? "COMPLIANT" : "NON_COMPLIANT")
            .message(message)
            .checkValue(loanConcentration)
            .threshold(LOAN_CONCENTRATION_THRESHOLD)
            .actualValue(loanConcentration)
            .checkType("LOAN_CONCENTRATION")
            .checkTime(LocalDateTime.now())
            .details("贷款集中度 = 客户额度 / 集团总额度")
            .violations(violation != null ? Collections.singletonList(violation) : null)
            .build();
    }
    
    @Override
    public ComplianceCheckResult checkLiquidityRatio(ComplianceCheckRequest request) {
        log.info("Checking liquidity ratio: objectId={}, objectType={}", 
                request.getObjectId(), request.getObjectType());
        
        // 流动性比率 = 流动性资产 / 流动性负债
        BigDecimal liquidAssets = request.getBaselineValue(); // 流动性资产
        BigDecimal liquidLiabilities = request.getCheckValue(); // 流动性负债
        
        if (liquidAssets == null || liquidLiabilities == null || 
            liquidLiabilities.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid input values for liquidity ratio calculation");
            return buildFailedResult("LIQUIDITY_RATIO_CALC_ERROR", 
                "流动性比率计算参数无效", "LIQUIDITY_RATIO", request);
        }
        
        BigDecimal liquidityRatio = liquidAssets.divide(liquidLiabilities, 4, RoundingMode.HALF_UP);
        boolean passed = liquidityRatio.compareTo(LIQUIDITY_RATIO_MIN) >= 0;
        
        String message = passed ? 
            String.format("流动性比率为%.2f%%，符合监管要求", liquidityRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)) :
            String.format("流动性比率为%.2f%%，低于监管要求的%.2f%%", 
                liquidityRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                LIQUIDITY_RATIO_MIN.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        log.info("Liquidity ratio check result: passed={}, ratio={}", passed, liquidityRatio);
        
        return ComplianceCheckResult.builder()
            .passed(passed)
            .resultCode(passed ? "COMPLIANT" : "NON_COMPLIANT")
            .message(message)
            .checkValue(liquidityRatio)
            .threshold(LIQUIDITY_RATIO_MIN)
            .actualValue(liquidityRatio)
            .checkType("LIQUIDITY_RATIO")
            .checkTime(LocalDateTime.now())
            .details("流动性比率 = 流动性资产 / 流动性负债")
            .build();
    }
    
    @Override
    public ComplianceCheckResult checkNonPerformingLoanRatio(ComplianceCheckRequest request) {
        log.info("Checking non-performing loan ratio: objectId={}, objectType={}", 
                request.getObjectId(), request.getObjectType());
        
        // 不良贷款率 = 不良贷款余额 / 贷款总余额
        BigDecimal nplBalance = request.getBaselineValue(); // 不良贷款余额
        BigDecimal totalLoanBalance = request.getCheckValue(); // 贷款总余额
        
        if (nplBalance == null || totalLoanBalance == null || 
            totalLoanBalance.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid input values for non-performing loan ratio calculation");
            return buildFailedResult("NPL_RATIO_CALC_ERROR", 
                "不良贷款率计算参数无效", "NON_PERFORMING_LOAN_RATIO", request);
        }
        
        BigDecimal nplRatio = nplBalance.divide(totalLoanBalance, 4, RoundingMode.HALF_UP);
        boolean passed = nplRatio.compareTo(NON_PERFORMING_LOAN_RATIO_THRESHOLD) <= 0;
        
        String message = passed ? 
            String.format("不良贷款率为%.2f%%，符合监管要求", nplRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)) :
            String.format("不良贷款率为%.2f%%，超过监管要求的%.2f%%", 
                nplRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                NON_PERFORMING_LOAN_RATIO_THRESHOLD.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        log.info("Non-performing loan ratio check result: passed={}, ratio={}", passed, nplRatio);
        
        return ComplianceCheckResult.builder()
            .passed(passed)
            .resultCode(passed ? "COMPLIANT" : "NON_COMPLIANT")
            .message(message)
            .checkValue(nplRatio)
            .threshold(NON_PERFORMING_LOAN_RATIO_THRESHOLD)
            .actualValue(nplRatio)
            .checkType("NON_PERFORMING_LOAN_RATIO")
            .checkTime(LocalDateTime.now())
            .details("不良贷款率 = 不良贷款余额 / 贷款总余额")
            .build();
    }
    
    @Override
    public ComplianceCheckResult checkProvisionCoverageRatio(ComplianceCheckRequest request) {
        log.info("Checking provision coverage ratio: objectId={}, objectType={}", 
                request.getObjectId(), request.getObjectType());
        
        // 拨备覆盖率 = 贷款损失准备金 / 不良贷款余额
        BigDecimal loanLossProvisions = request.getBaselineValue(); // 贷款损失准备金
        BigDecimal nplBalance = request.getCheckValue(); // 不良贷款余额
        
        if (loanLossProvisions == null || nplBalance == null || 
            nplBalance.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid input values for provision coverage ratio calculation");
            return buildFailedResult("PCR_CALC_ERROR", 
                "拨备覆盖率计算参数无效", "PROVISION_COVERAGE_RATIO", request);
        }
        
        BigDecimal provisionCoverageRatio = loanLossProvisions.divide(nplBalance, 4, RoundingMode.HALF_UP);
        boolean passed = provisionCoverageRatio.compareTo(PROVISION_COVERAGE_RATIO_THRESHOLD) >= 0;
        
        String message = passed ? 
            String.format("拨备覆盖率为%.2f%%，符合监管要求", provisionCoverageRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)) :
            String.format("拨备覆盖率为%.2f%%，低于监管要求的%.2f%%", 
                provisionCoverageRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                PROVISION_COVERAGE_RATIO_THRESHOLD.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        log.info("Provision coverage ratio check result: passed={}, ratio={}", passed, provisionCoverageRatio);
        
        return ComplianceCheckResult.builder()
            .passed(passed)
            .resultCode(passed ? "COMPLIANT" : "NON_COMPLIANT")
            .message(message)
            .checkValue(provisionCoverageRatio)
            .threshold(PROVISION_COVERAGE_RATIO_THRESHOLD)
            .actualValue(provisionCoverageRatio)
            .checkType("PROVISION_COVERAGE_RATIO")
            .checkTime(LocalDateTime.now())
            .details("拨备覆盖率 = 贷款损失准备金 / 不良贷款余额")
            .build();
    }
    
    @Override
    public ComplianceCheckResult checkLeverageRatio(ComplianceCheckRequest request) {
        log.info("Checking leverage ratio: objectId={}, objectType={}", 
                request.getObjectId(), request.getObjectType());
        
        // 杠杆率 = 一级资本 / 表内外总敞口
        BigDecimal tier1Capital = request.getBaselineValue(); // 一级资本
        BigDecimal totalExposure = request.getCheckValue(); // 表内外总敞口
        
        if (tier1Capital == null || totalExposure == null || 
            totalExposure.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid input values for leverage ratio calculation");
            return buildFailedResult("LEVERAGE_RATIO_CALC_ERROR", 
                "杠杆率计算参数无效", "LEVERAGE_RATIO", request);
        }
        
        BigDecimal leverageRatio = tier1Capital.divide(totalExposure, 4, RoundingMode.HALF_UP);
        boolean passed = leverageRatio.compareTo(LEVERAGE_RATIO_THRESHOLD) >= 0;
        
        String message = passed ? 
            String.format("杠杆率为%.2f%%，符合监管要求", leverageRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)) :
            String.format("杠杆率为%.2f%%，低于监管要求的%.2f%%", 
                leverageRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                LEVERAGE_RATIO_THRESHOLD.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        log.info("Leverage ratio check result: passed={}, ratio={}", passed, leverageRatio);
        
        return ComplianceCheckResult.builder()
            .passed(passed)
            .resultCode(passed ? "COMPLIANT" : "NON_COMPLIANT")
            .message(message)
            .checkValue(leverageRatio)
            .threshold(LEVERAGE_RATIO_THRESHOLD)
            .actualValue(leverageRatio)
            .checkType("LEVERAGE_RATIO")
            .checkTime(LocalDateTime.now())
            .details("杠杆率 = 一级资本 / 表内外总敞口")
            .build();
    }
    
    @Override
    public ComplianceCheckResult checkGroupCreditConcentration(ComplianceCheckRequest request) {
        log.info("Checking group credit concentration: groupId={}", request.getGroupId());
        
        if (request.getGroupId() == null) {
            log.warn("Group ID is required for group credit concentration check");
            return buildFailedResult("GROUP_CREDIT_CONCENTRATION_ERROR", 
                "集团ID不能为空", "GROUP_CREDIT_CONCENTRATION", request);
        }
        
        // 获取集团额度
        Optional<GroupQuota> groupQuotaOpt = groupQuotaRepository.findByGroupId(request.getGroupId());
        if (!groupQuotaOpt.isPresent()) {
            log.warn("Group quota not found: groupId={}", request.getGroupId());
            return buildFailedResult("GROUP_QUOTA_NOT_FOUND", 
                "集团额度不存在", "GROUP_CREDIT_CONCENTRATION", request);
        }
        
        GroupQuota groupQuota = groupQuotaOpt.get();
        BigDecimal groupTotalQuota = groupQuota.getTotalQuota();
        
        // 获取银行总额度（这里简化为获取所有集团额度的总和作为示例）
        List<GroupQuota> allGroups = groupQuotaRepository.findAll();
        BigDecimal bankTotalQuota = allGroups.stream()
                .map(GroupQuota::getTotalQuota)
                .reduce(BigDecimal.ZERO, MonetaryUtils::add);
        
        if (bankTotalQuota.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid bank total quota: {}", bankTotalQuota);
            return buildFailedResult("INVALID_BANK_QUOTA", 
                "银行总额度无效", "GROUP_CREDIT_CONCENTRATION", request);
        }
        
        // 计算集团授信集中度
        BigDecimal groupCreditConcentration = groupTotalQuota.divide(bankTotalQuota, 4, RoundingMode.HALF_UP);
        boolean passed = groupCreditConcentration.compareTo(GROUP_CREDIT_CONCENTRATION_THRESHOLD) <= 0;
        
        String message = passed ? 
            String.format("集团授信集中度为%.2f%%，符合监管要求", groupCreditConcentration.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)) :
            String.format("集团授信集中度为%.2f%%，超过监管要求的%.2f%%", 
                groupCreditConcentration.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                GROUP_CREDIT_CONCENTRATION_THRESHOLD.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        log.info("Group credit concentration check result: groupId={}, passed={}, ratio={}", 
                request.getGroupId(), passed, groupCreditConcentration);
        
        ComplianceCheckResult.ViolationDetail violation = null;
        if (!passed) {
            violation = ComplianceCheckResult.ViolationDetail.builder()
                .violationCode("HIGH_GROUP_CONCENTRATION")
                .violationDescription("单一集团客户授信集中度过高")
                .severity("ERROR")
                .currentValue(groupCreditConcentration)
                .thresholdValue(GROUP_CREDIT_CONCENTRATION_THRESHOLD)
                .suggestion("考虑分散风险或降低对该集团的授信额度")
                .build();
        }
        
        return ComplianceCheckResult.builder()
            .passed(passed)
            .resultCode(passed ? "COMPLIANT" : "NON_COMPLIANT")
            .message(message)
            .checkValue(groupCreditConcentration)
            .threshold(GROUP_CREDIT_CONCENTRATION_THRESHOLD)
            .actualValue(groupCreditConcentration)
            .checkType("GROUP_CREDIT_CONCENTRATION")
            .checkTime(LocalDateTime.now())
            .details("集团授信集中度 = 集团额度 / 银行总额度")
            .violations(violation != null ? Collections.singletonList(violation) : null)
            .build();
    }
    
    @Override
    public ComplianceCheckResult performFullComplianceCheck(ComplianceCheckRequest request) {
        log.info("Performing full compliance check: objectId={}, objectType={}", 
                request.getObjectId(), request.getObjectType());
        
        // 执行所有单项检查
        ComplianceCheckResult capResult = checkCapitalAdequacyRatio(request);
        ComplianceCheckResult loanResult = checkLoanConcentration(request);
        ComplianceCheckResult liqResult = checkLiquidityRatio(request);
        ComplianceCheckResult nplResult = checkNonPerformingLoanRatio(request);
        ComplianceCheckResult pcrResult = checkProvisionCoverageRatio(request);
        ComplianceCheckResult levResult = checkLeverageRatio(request);
        ComplianceCheckResult gccResult = checkGroupCreditConcentration(request);
        
        // 汇总结果
        boolean allPassed = capResult.isPassed() && loanResult.isPassed() && 
                           liqResult.isPassed() && nplResult.isPassed() && 
                           pcrResult.isPassed() && levResult.isPassed() && 
                           gccResult.isPassed();
        
        String message = allPassed ? 
            "所有监管指标均符合要求" : 
            "存在不符合监管要求的指标，请查看详细信息";
        
        // 收集所有违规信息
        List<ComplianceCheckResult.ViolationDetail> allViolations = new ArrayList<>();
        addViolationsIfPresent(allViolations, capResult.getViolations());
        addViolationsIfPresent(allViolations, loanResult.getViolations());
        addViolationsIfPresent(allViolations, liqResult.getViolations());
        addViolationsIfPresent(allViolations, nplResult.getViolations());
        addViolationsIfPresent(allViolations, pcrResult.getViolations());
        addViolationsIfPresent(allViolations, levResult.getViolations());
        addViolationsIfPresent(allViolations, gccResult.getViolations());
        
        log.info("Full compliance check completed: allPassed={}", allPassed);
        
        return ComplianceCheckResult.builder()
            .passed(allPassed)
            .resultCode(allPassed ? "FULL_COMPLIANT" : "PARTIAL_NON_COMPLIANT")
            .message(message)
            .checkType("FULL_COMPLIANCE_CHECK")
            .checkTime(LocalDateTime.now())
            .details("综合监管指标合规检查")
            .violations(allViolations.isEmpty() ? null : allViolations)
            .build();
    }
    
    @Override
    public Object getRegulatoryThresholds() {
        log.info("Getting regulatory thresholds");
        
        Map<String, BigDecimal> thresholds = new HashMap<>();
        thresholds.put("capitalAdequacyRatio", CAPITAL_ADEQUACY_RATIO_THRESHOLD);
        thresholds.put("loanConcentration", LOAN_CONCENTRATION_THRESHOLD);
        thresholds.put("groupCreditConcentration", GROUP_CREDIT_CONCENTRATION_THRESHOLD);
        thresholds.put("nonPerformingLoanRatio", NON_PERFORMING_LOAN_RATIO_THRESHOLD);
        thresholds.put("provisionCoverageRatio", PROVISION_COVERAGE_RATIO_THRESHOLD);
        thresholds.put("liquidityRatioMin", LIQUIDITY_RATIO_MIN);
        thresholds.put("leverageRatio", LEVERAGE_RATIO_THRESHOLD);
        
        return thresholds;
    }
    
    @Override
    public boolean updateRegulatoryThresholds(Object thresholds) {
        log.warn("Updating regulatory thresholds is not supported in this implementation");
        // 在实际应用中，这将涉及更新配置表或配置文件
        // 为了演示目的，我们暂时不实现此功能
        return false;
    }
    
    /**
     * 构建失败结果
     */
    private ComplianceCheckResult buildFailedResult(String resultCode, String message, 
            String checkType, ComplianceCheckRequest request) {
        return ComplianceCheckResult.builder()
            .passed(false)
            .resultCode(resultCode)
            .message(message)
            .checkType(checkType)
            .checkTime(LocalDateTime.now())
            .build();
    }
    
    /**
     * 添加违规信息到列表（如果存在）
     */
    private void addViolationsIfPresent(List<ComplianceCheckResult.ViolationDetail> targetList, 
            List<ComplianceCheckResult.ViolationDetail> sourceList) {
        if (sourceList != null && !sourceList.isEmpty()) {
            targetList.addAll(sourceList);
        }
    }
}