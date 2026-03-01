package com.bank.quota.core.service;

import java.util.List;

/**
 * 集中度风险预警服务
 * 
 * <p>提供银行级信贷额度管控平台的集中度风险预警功能。
 * 用于检测和预警客户、行业、地区等维度的集中度风险。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public interface ConcentrationRiskService {
    
    /**
     * 检查客户集中度风险
     * 
     * <p>检查单一客户或关联客户群体的集中度风险。</p>
     * 
     * @param customerId 客户ID
     * @return 风险检查结果
     */
    boolean checkCustomerConcentrationRisk(Long customerId);
    
    /**
     * 检查行业集中度风险
     * 
     * <p>检查特定行业的集中度风险。</p>
     * 
     * @param industryCode 行业代码
     * @return 风险检查结果
     */
    boolean checkIndustryConcentrationRisk(String industryCode);
    
    /**
     * 检查地区集中度风险
     * 
     * <p>检查特定地区的集中度风险。</p>
     * 
     * @param regionCode 地区代码
     * @return 风险检查结果
     */
    boolean checkRegionConcentrationRisk(String regionCode);
    
    /**
     * 检查产品集中度风险
     * 
     * <p>检查特定产品的集中度风险。</p>
     * 
     * @param productType 产品类型
     * @return 风险检查结果
     */
    boolean checkProductConcentrationRisk(String productType);
    
    /**
     * 获取集中度风险报告
     * 
     * <p>生成集中度风险分析报告。</p>
     * 
     * @param reportType 报告类型
     * @return 风险报告数据
     */
    List<Object> getConcentrationRiskReport(String reportType);
    
    /**
     * 检查总体集中度风险
     * 
     * <p>检查整个额度系统的集中度风险状况。</p>
     * 
     * @return 风险检查结果
     */
    boolean checkOverallConcentrationRisk();
}