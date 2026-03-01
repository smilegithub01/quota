package com.bank.quota.core.service;

import com.bank.quota.core.dto.compliance.ComplianceCheckRequest;
import com.bank.quota.core.dto.compliance.ComplianceCheckResult;

/**
 * 监管指标校验服务
 * 
 * <p>提供银行级信贷额度管控平台的监管指标校验功能。
 * 用于校验各项业务指标是否符合监管要求。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public interface RegulatoryComplianceService {
    
    /**
     * 校验资本充足率指标
     * 
     * <p>校验银行资本充足率是否符合监管要求（通常不低于8%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    ComplianceCheckResult checkCapitalAdequacyRatio(ComplianceCheckRequest request);
    
    /**
     * 校验贷款集中度指标
     * 
     * <p>校验单一客户贷款集中度是否符合监管要求（通常不超过10%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    ComplianceCheckResult checkLoanConcentration(ComplianceCheckRequest request);
    
    /**
     * 校验流动性比率指标
     * 
     * <p>校验银行流动性比率是否符合监管要求。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    ComplianceCheckResult checkLiquidityRatio(ComplianceCheckRequest request);
    
    /**
     * 校验不良贷款率指标
     * 
     * <p>校验银行不良贷款率是否符合监管要求（通常不超过5%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    ComplianceCheckResult checkNonPerformingLoanRatio(ComplianceCheckRequest request);
    
    /**
     * 校验拨备覆盖率指标
     * 
     * <p>校验银行拨备覆盖率是否符合监管要求（通常不低于150%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    ComplianceCheckResult checkProvisionCoverageRatio(ComplianceCheckRequest request);
    
    /**
     * 校验杠杆率指标
     * 
     * <p>校验银行杠杆率是否符合监管要求。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    ComplianceCheckResult checkLeverageRatio(ComplianceCheckRequest request);
    
    /**
     * 校验单一集团客户授信集中度
     * 
     * <p>校验对单一集团客户的授信集中度是否符合监管要求（通常不超过15%）。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    ComplianceCheckResult checkGroupCreditConcentration(ComplianceCheckRequest request);
    
    /**
     * 执行全面监管合规检查
     * 
     * <p>执行所有监管指标的全面合规检查。</p>
     * 
     * @param request 校验请求
     * @return 综合校验结果
     */
    ComplianceCheckResult performFullComplianceCheck(ComplianceCheckRequest request);
    
    /**
     * 获取监管指标阈值配置
     * 
     * <p>获取当前监管指标的阈值配置。</p>
     * 
     * @return 阈值配置信息
     */
    Object getRegulatoryThresholds();
    
    /**
     * 更新监管指标阈值配置
     * 
     * <p>更新监管指标的阈值配置。</p>
     * 
     * @param thresholds 新的阈值配置
     * @return 操作结果
     */
    boolean updateRegulatoryThresholds(Object thresholds);
}