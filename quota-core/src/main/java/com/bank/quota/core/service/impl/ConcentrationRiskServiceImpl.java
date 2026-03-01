package com.bank.quota.core.service.impl;

import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.Customer;
import com.bank.quota.core.domain.CustomerQuota;
import com.bank.quota.core.domain.GroupQuota;
import com.bank.quota.core.repository.*;
import com.bank.quota.core.service.ConcentrationRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 集中度风险预警服务实现类
 * 
 * <p>提供银行级信贷额度管控平台的集中度风险预警功能实现。
 * 用于检测和预警客户、行业、地区等维度的集中度风险。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConcentrationRiskServiceImpl implements ConcentrationRiskService {
    
    private final CustomerRepository customerRepository;
    private final CustomerQuotaRepository customerQuotaRepository;
    private final GroupQuotaRepository groupQuotaRepository;
    private final ApprovalQuotaRepository approvalQuotaRepository;
    
    // 集中度风险阈值常量
    private static final BigDecimal CUSTOMER_CONCENTRATION_THRESHOLD = new BigDecimal("0.15"); // 15%
    private static final BigDecimal INDUSTRY_CONCENTRATION_THRESHOLD = new BigDecimal("0.20"); // 20%
    private static final BigDecimal REGION_CONCENTRATION_THRESHOLD = new BigDecimal("0.25"); // 25%
    private static final BigDecimal PRODUCT_CONCENTRATION_THRESHOLD = new BigDecimal("0.30"); // 30%
    
    @Override
    public boolean checkCustomerConcentrationRisk(Long customerId) {
        log.info("Checking customer concentration risk: customerId={}", customerId);
        
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerIdWithLock(customerId)
                .orElse(null);
        
        if (customerQuota == null) {
            log.warn("Customer quota not found: customerId={}", customerId);
            return false;
        }
        
        // 获取集团总额度
        GroupQuota groupQuota = groupQuotaRepository.findByGroupId(customerQuota.getGroupId())
                .orElse(null);
        
        if (groupQuota == null) {
            log.warn("Group quota not found for customer: customerId={}", customerId);
            return false;
        }
        
        // 计算客户额度占集团总额度的比例
        BigDecimal groupTotalQuota = groupQuota.getTotalQuota();
        if (groupTotalQuota.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid group total quota: {}", groupTotalQuota);
            return false;
        }
        
        BigDecimal customerTotalQuota = customerQuota.getTotalQuota();
        BigDecimal concentrationRatio = customerTotalQuota.divide(groupTotalQuota, 4, RoundingMode.HALF_UP);
        
        log.info("Customer concentration ratio calculated: customerId={}, ratio={}", 
                customerId, concentrationRatio);
        
        boolean isRisk = concentrationRatio.compareTo(CUSTOMER_CONCENTRATION_THRESHOLD) > 0;
        
        if (isRisk) {
            log.warn("Customer concentration risk detected: customerId={}, ratio={}, threshold={}", 
                    customerId, concentrationRatio, CUSTOMER_CONCENTRATION_THRESHOLD);
        }
        
        return isRisk;
    }
    
    @Override
    public boolean checkIndustryConcentrationRisk(String industryCode) {
        log.info("Checking industry concentration risk: industryCode={}", industryCode);
        
        // 查询该行业的所有客户额度总和
        List<CustomerQuota> industryCustomers = customerQuotaRepository.findByIndustryCode(industryCode);
        BigDecimal industryTotalQuota = industryCustomers.stream()
                .map(CustomerQuota::getTotalQuota)
                .reduce(BigDecimal.ZERO, MonetaryUtils::add);
        
        // 查询所有客户额度总和
        List<CustomerQuota> allCustomers = customerQuotaRepository.findAll();
        BigDecimal allTotalQuota = allCustomers.stream()
                .map(CustomerQuota::getTotalQuota)
                .reduce(BigDecimal.ZERO, MonetaryUtils::add);
        
        if (allTotalQuota.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid total quota for all customers: {}", allTotalQuota);
            return false;
        }
        
        BigDecimal concentrationRatio = industryTotalQuota.divide(allTotalQuota, 4, RoundingMode.HALF_UP);
        
        log.info("Industry concentration ratio calculated: industry={}, ratio={}", 
                industryCode, concentrationRatio);
        
        boolean isRisk = concentrationRatio.compareTo(INDUSTRY_CONCENTRATION_THRESHOLD) > 0;
        
        if (isRisk) {
            log.warn("Industry concentration risk detected: industry={}, ratio={}, threshold={}", 
                    industryCode, concentrationRatio, INDUSTRY_CONCENTRATION_THRESHOLD);
        }
        
        return isRisk;
    }
    
    @Override
    public boolean checkRegionConcentrationRisk(String regionCode) {
        log.info("Checking region concentration risk: regionCode={}", regionCode);
        
        // 查询该地区的所有客户额度总和
        List<CustomerQuota> regionCustomers = customerQuotaRepository.findByRegionCode(regionCode);
        BigDecimal regionTotalQuota = regionCustomers.stream()
                .map(CustomerQuota::getTotalQuota)
                .reduce(BigDecimal.ZERO, MonetaryUtils::add);
        
        // 查询所有客户额度总和
        List<CustomerQuota> allCustomers = customerQuotaRepository.findAll();
        BigDecimal allTotalQuota = allCustomers.stream()
                .map(CustomerQuota::getTotalQuota)
                .reduce(BigDecimal.ZERO, MonetaryUtils::add);
        
        if (allTotalQuota.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid total quota for all customers: {}", allTotalQuota);
            return false;
        }
        
        BigDecimal concentrationRatio = regionTotalQuota.divide(allTotalQuota, 4, RoundingMode.HALF_UP);
        
        log.info("Region concentration ratio calculated: region={}, ratio={}", 
                regionCode, concentrationRatio);
        
        boolean isRisk = concentrationRatio.compareTo(REGION_CONCENTRATION_THRESHOLD) > 0;
        
        if (isRisk) {
            log.warn("Region concentration risk detected: region={}, ratio={}, threshold={}", 
                    regionCode, concentrationRatio, REGION_CONCENTRATION_THRESHOLD);
        }
        
        return isRisk;
    }
    
    @Override
    public boolean checkProductConcentrationRisk(String productType) {
        log.info("Checking product concentration risk: productType={}", productType);
        
        // 查询该产品类型的所有批复额度总和
        // 注意：这里需要根据实际业务逻辑调整查询方式
        List<CustomerQuota> productCustomers = customerQuotaRepository.findByProductType(productType);
        BigDecimal productTotalQuota = productCustomers.stream()
                .map(CustomerQuota::getTotalQuota)
                .reduce(BigDecimal.ZERO, MonetaryUtils::add);
        
        // 查询所有客户额度总和
        List<CustomerQuota> allCustomers = customerQuotaRepository.findAll();
        BigDecimal allTotalQuota = allCustomers.stream()
                .map(CustomerQuota::getTotalQuota)
                .reduce(BigDecimal.ZERO, MonetaryUtils::add);
        
        if (allTotalQuota.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid total quota for all customers: {}", allTotalQuota);
            return false;
        }
        
        BigDecimal concentrationRatio = productTotalQuota.divide(allTotalQuota, 4, RoundingMode.HALF_UP);
        
        log.info("Product concentration ratio calculated: product={}, ratio={}", 
                productType, concentrationRatio);
        
        boolean isRisk = concentrationRatio.compareTo(PRODUCT_CONCENTRATION_THRESHOLD) > 0;
        
        if (isRisk) {
            log.warn("Product concentration risk detected: product={}, ratio={}, threshold={}", 
                    productType, concentrationRatio, PRODUCT_CONCENTRATION_THRESHOLD);
        }
        
        return isRisk;
    }
    
    @Override
    public List<Object> getConcentrationRiskReport(String reportType) {
        log.info("Generating concentration risk report: type={}", reportType);
        
        // 这里将根据报告类型生成相应的集中度风险报告
        // 由于这是一个示例实现，我们返回空列表
        // 在实际应用中，这里应该查询数据库并返回具体的报告数据
        return java.util.Collections.emptyList();
    }
    
    @Override
    public boolean checkOverallConcentrationRisk() {
        log.info("Checking overall concentration risk");
        
        // 检查各种集中度风险
        boolean customerRisk = checkAnyCustomerConcentrationRisk();
        boolean industryRisk = checkAnyIndustryConcentrationRisk();
        boolean regionRisk = checkAnyRegionConcentrationRisk();
        boolean productRisk = checkAnyProductConcentrationRisk();
        
        boolean overallRisk = customerRisk || industryRisk || regionRisk || productRisk;
        
        if (overallRisk) {
            log.warn("Overall concentration risk detected");
        } else {
            log.info("No overall concentration risk detected");
        }
        
        return overallRisk;
    }
    
    /**
     * 检查是否存在任何客户集中度风险
     */
    private boolean checkAnyCustomerConcentrationRisk() {
        List<CustomerQuota> allCustomers = customerQuotaRepository.findAll();
        for (CustomerQuota customerQuota : allCustomers) {
            if (checkCustomerConcentrationRisk(customerQuota.getCustomerId())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否存在任何行业集中度风险
     */
    private boolean checkAnyIndustryConcentrationRisk() {
        // 这里应该查询所有不同的行业代码并检查风险
        // 由于没有直接的行业代码查询方法，我们假设一些常见的行业代码
        List<String> industries = java.util.Arrays.asList("BANKING", "INSURANCE", "SECURITIES", "REAL_ESTATE", "MANUFACTURING");
        for (String industry : industries) {
            if (checkIndustryConcentrationRisk(industry)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否存在任何地区集中度风险
     */
    private boolean checkAnyRegionConcentrationRisk() {
        // 这里应该查询所有不同的地区代码并检查风险
        // 由于没有直接的地区代码查询方法，我们假设一些常见的地区代码
        List<String> regions = java.util.Arrays.asList("BEIJING", "SHANGHAI", "GUANGDONG", "JIANGSU", "ZHEJIANG");
        for (String region : regions) {
            if (checkRegionConcentrationRisk(region)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否存在任何产品集中度风险
     */
    private boolean checkAnyProductConcentrationRisk() {
        // 这里应该查询所有不同的产品类型并检查风险
        // 由于没有直接的产品类型查询方法，我们假设一些常见的产品类型
        List<String> products = java.util.Arrays.asList("LOAN", "CREDIT_CARD", "MORTGAGE", "OVERDRAFT", "GUARANTEE");
        for (String product : products) {
            if (checkProductConcentrationRisk(product)) {
                return true;
            }
        }
        return false;
    }
}