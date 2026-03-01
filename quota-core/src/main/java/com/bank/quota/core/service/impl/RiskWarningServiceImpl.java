package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.*;
import com.bank.quota.core.dto.riskwarning.*;
import com.bank.quota.core.repository.*;
import com.bank.quota.core.service.RiskWarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskWarningServiceImpl implements RiskWarningService {
    
    private final RiskWarningRuleRepository warningRuleRepository;
    private final GroupQuotaRepository groupQuotaRepository;
    private final CustomerQuotaRepository customerQuotaRepository;
    private final ApprovalQuotaRepository approvalQuotaRepository;
    private final com.bank.quota.core.service.ConcentrationRiskService concentrationRiskService;
    
    @Override
    @Transactional
    public RiskWarningRuleResponse createWarningRule(CreateWarningRuleRequest request) {
        log.info("Creating warning rule: ruleCode={}, ruleName={}", 
                request.getRuleCode(), request.getRuleName());
        
        if (warningRuleRepository.existsByRuleCode(request.getRuleCode())) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                    "预警规则编码已存在: " + request.getRuleCode());
        }
        
        RiskWarningRule rule = RiskWarningRule.builder()
                .ruleCode(request.getRuleCode())
                .ruleName(request.getRuleName())
                .ruleType(RiskWarningRule.RuleType.valueOf(request.getRuleType()))
                .warningLevel(RiskWarningRule.WarningLevel.valueOf(request.getWarningLevel()))
                .thresholdType(RiskWarningRule.ThresholdType.valueOf(request.getThresholdType()))
                .thresholdValue(request.getThresholdValue())
                .objectType(RiskWarningRule.QuotaObjectType.valueOf(request.getObjectType()))
                .status(RiskWarningRule.RuleStatus.ENABLED)
                .notifyChannels(request.getNotifyChannels())
                .notifyRecipients(request.getNotifyRecipients())
                .description(request.getDescription())
                .createBy(request.getCreateBy())
                .build();
        
        RiskWarningRule saved = warningRuleRepository.save(rule);
        
        log.info("Warning rule created successfully: ruleCode={}", saved.getRuleCode());
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public RiskWarningRuleResponse updateWarningRule(Long ruleId, UpdateWarningRuleRequest request) {
        log.info("Updating warning rule: ruleId={}", ruleId);
        
        RiskWarningRule rule = warningRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "预警规则不存在: ruleId=" + ruleId));
        
        if (request.getRuleName() != null) {
            rule.setRuleName(request.getRuleName());
        }
        
        if (request.getWarningLevel() != null) {
            rule.setWarningLevel(RiskWarningRule.WarningLevel.valueOf(request.getWarningLevel()));
        }
        
        if (request.getThresholdValue() != null) {
            rule.setThresholdValue(request.getThresholdValue());
        }
        
        if (request.getNotifyChannels() != null) {
            rule.setNotifyChannels(request.getNotifyChannels());
        }
        
        if (request.getNotifyRecipients() != null) {
            rule.setNotifyRecipients(request.getNotifyRecipients());
        }
        
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        
        rule.setUpdateBy(request.getUpdateBy());
        RiskWarningRule saved = warningRuleRepository.save(rule);
        
        log.info("Warning rule updated successfully: ruleId={}", ruleId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public RiskWarningRuleResponse enableWarningRule(Long ruleId, String operator) {
        log.info("Enabling warning rule: ruleId={}", ruleId);
        
        RiskWarningRule rule = warningRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "预警规则不存在: ruleId=" + ruleId));
        
        rule.setStatus(RiskWarningRule.RuleStatus.ENABLED);
        rule.setUpdateBy(operator);
        
        RiskWarningRule saved = warningRuleRepository.save(rule);
        
        log.info("Warning rule enabled successfully: ruleId={}", ruleId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public RiskWarningRuleResponse disableWarningRule(Long ruleId, String operator) {
        log.info("Disabling warning rule: ruleId={}", ruleId);
        
        RiskWarningRule rule = warningRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "预警规则不存在: ruleId=" + ruleId));
        
        rule.setStatus(RiskWarningRule.RuleStatus.DISABLED);
        rule.setUpdateBy(operator);
        
        RiskWarningRule saved = warningRuleRepository.save(rule);
        
        log.info("Warning rule disabled successfully: ruleId={}", ruleId);
        
        return buildResponse(saved);
    }
    
    @Override
    public RiskWarningRuleResponse getWarningRule(Long ruleId) {
        RiskWarningRule rule = warningRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "预警规则不存在: ruleId=" + ruleId));
        
        return buildResponse(rule);
    }
    
    @Override
    public List<RiskWarningRuleResponse> getWarningRulesByType(String ruleType) {
        return warningRuleRepository.findByRuleType(RiskWarningRule.RuleType.valueOf(ruleType))
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RiskWarningRuleResponse> getWarningRulesByLevel(String warningLevel) {
        return warningRuleRepository.findByWarningLevel(RiskWarningRule.WarningLevel.valueOf(warningLevel))
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RiskWarningRuleResponse> getEnabledWarningRules() {
        return warningRuleRepository.findAllEnabled()
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RiskWarningRuleResponse> getEnabledWarningRulesByObjectType(String objectType) {
        return warningRuleRepository.findEnabledByObjectType(
                RiskWarningRule.QuotaObjectType.valueOf(objectType))
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void checkAndTriggerWarnings(String objectType, Long objectId) {
        log.info("Checking warnings for: objectType={}, objectId={}", objectType, objectId);
        
        List<RiskWarningRule> rules = warningRuleRepository.findEnabledByObjectType(
                RiskWarningRule.QuotaObjectType.valueOf(objectType));
        
        for (RiskWarningRule rule : rules) {
            checkSingleRule(rule, objectType, objectId);
        }
    }
    
    private void checkSingleRule(RiskWarningRule rule, String objectType, Long objectId) {
        BigDecimal currentValue = getCurrentValue(objectType, objectId);
        if (currentValue == null) {
            return;
        }
        
        boolean shouldTrigger = evaluateThreshold(rule, currentValue);
        
        if (shouldTrigger) {
            log.warn("Risk warning triggered: ruleCode={}, objectType={}, objectId={}, " +
                    "currentValue={}, threshold={}", 
                    rule.getRuleCode(), objectType, objectId, currentValue, rule.getThresholdValue());
            
            triggerWarning(rule, objectType, objectId, currentValue);
        }
    }
    
    private BigDecimal getCurrentValue(String objectType, Long objectId) {
        switch (objectType) {
            case "GROUP":
                return groupQuotaRepository.findByGroupId(objectId)
                        .map(GroupQuota::getUsageRate)
                        .orElse(null);
            case "CUSTOMER":
                return customerQuotaRepository.findByCustomerId(objectId)
                        .map(CustomerQuota::getUsageRate)
                        .orElse(null);
            case "APPROVAL":
                return approvalQuotaRepository.findById(objectId)
                        .map(ApprovalQuota::getUsedQuota)
                        .orElse(null);
            default:
                return null;
        }
    }
    
    private boolean evaluateThreshold(RiskWarningRule rule, BigDecimal currentValue) {
        switch (rule.getThresholdType()) {
            case GREATER_THAN:
                return MonetaryUtils.isGreaterThan(currentValue, rule.getThresholdValue());
            case LESS_THAN:
                return MonetaryUtils.isLessThan(currentValue, rule.getThresholdValue());
            case EQUAL:
                return currentValue.compareTo(rule.getThresholdValue()) == 0;
            case BETWEEN:
                return false;
            default:
                return false;
        }
    }
    
    private void triggerWarning(RiskWarningRule rule, String objectType, Long objectId, BigDecimal currentValue) {
        log.warn("Risk warning event triggered - Rule: {}, Object: {}-{}, Value: {}, Threshold: {}",
                rule.getRuleCode(), objectType, objectId, currentValue, rule.getThresholdValue());
    }
    
    @Override
    public List<RiskWarningEventResponse> getWarningEvents(String objectType, Long objectId) {
        return null;
    }
    
    @Override
    public boolean checkConcentrationRisk(String riskType, String riskDimension) {
        log.info("Checking concentration risk: type={}, dimension={}", riskType, riskDimension);
        
        switch (riskType.toUpperCase()) {
            case "CUSTOMER":
                // 检查特定客户的集中度风险
                try {
                    Long customerId = Long.parseLong(riskDimension);
                    return checkCustomerConcentrationRisk(customerId);
                } catch (NumberFormatException e) {
                    log.error("Invalid customer ID: {}", riskDimension);
                    return false;
                }
            case "INDUSTRY":
                // 检查特定行业的集中度风险
                return checkIndustryConcentrationRisk(riskDimension);
            case "REGION":
                // 检查特定地区的集中度风险
                return checkRegionConcentrationRisk(riskDimension);
            case "PRODUCT":
                // 检查特定产品的集中度风险
                return checkProductConcentrationRisk(riskDimension);
            default:
                log.warn("Unsupported risk type: {}", riskType);
                return false;
        }
    }
    
    @Override
    public boolean checkAllConcentrationRisks() {
        log.info("Checking all concentration risks");
        
        // 使用注入的集中度风险服务进行检查
        return concentrationRiskService.checkOverallConcentrationRisk();
    }
    
    // 从集中度风险服务委托的方法
    
    private boolean checkCustomerConcentrationRisk(Long customerId) {
        return concentrationRiskService.checkCustomerConcentrationRisk(customerId);
    }
    
    private boolean checkIndustryConcentrationRisk(String industryCode) {
        return concentrationRiskService.checkIndustryConcentrationRisk(industryCode);
    }
    
    private boolean checkRegionConcentrationRisk(String regionCode) {
        return concentrationRiskService.checkRegionConcentrationRisk(regionCode);
    }
    
    private boolean checkProductConcentrationRisk(String productType) {
        return concentrationRiskService.checkProductConcentrationRisk(productType);
    }
    
    @Override
    public void processWarningEvent(Long eventId, String action, String operator) {
        log.info("Processing warning event: eventId={}, action={}, operator={}", 
                eventId, action, operator);
    }
    
    private RiskWarningRuleResponse buildResponse(RiskWarningRule rule) {
        RiskWarningRuleResponse response = new RiskWarningRuleResponse();
        response.setId(rule.getId());
        response.setRuleCode(rule.getRuleCode());
        response.setRuleName(rule.getRuleName());
        response.setRuleType(rule.getRuleType() != null ? rule.getRuleType().name() : null);
        response.setWarningLevel(rule.getWarningLevel() != null ? rule.getWarningLevel().name() : null);
        response.setThresholdType(rule.getThresholdType() != null ? rule.getThresholdType().name() : null);
        response.setThresholdValue(rule.getThresholdValue());
        response.setObjectType(rule.getObjectType() != null ? rule.getObjectType().name() : null);
        response.setStatus(rule.getStatus() != null ? rule.getStatus().name() : null);
        response.setNotifyChannels(rule.getNotifyChannels());
        response.setNotifyRecipients(rule.getNotifyRecipients());
        response.setDescription(rule.getDescription());
        response.setCreateBy(rule.getCreateBy());
        response.setUpdateBy(rule.getUpdateBy());
        response.setCreateTime(rule.getCreateTime());
        response.setUpdateTime(rule.getUpdateTime());
        return response;
    }
}
