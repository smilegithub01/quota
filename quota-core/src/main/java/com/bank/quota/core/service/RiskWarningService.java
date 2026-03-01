package com.bank.quota.core.service;

import com.bank.quota.core.domain.RiskWarningRule;
import com.bank.quota.core.dto.riskwarning.*;

import java.util.List;

public interface RiskWarningService {
    
    RiskWarningRuleResponse createWarningRule(CreateWarningRuleRequest request);
    
    RiskWarningRuleResponse updateWarningRule(Long ruleId, UpdateWarningRuleRequest request);
    
    RiskWarningRuleResponse enableWarningRule(Long ruleId, String operator);
    
    RiskWarningRuleResponse disableWarningRule(Long ruleId, String operator);
    
    RiskWarningRuleResponse getWarningRule(Long ruleId);
    
    List<RiskWarningRuleResponse> getWarningRulesByType(String ruleType);
    
    List<RiskWarningRuleResponse> getWarningRulesByLevel(String warningLevel);
    
    List<RiskWarningRuleResponse> getEnabledWarningRules();
    
    List<RiskWarningRuleResponse> getEnabledWarningRulesByObjectType(String objectType);
    
    void checkAndTriggerWarnings(String objectType, Long objectId);
    
    List<RiskWarningEventResponse> getWarningEvents(String objectType, Long objectId);
    
    /**
     * 检查集中度风险
     * 
     * <p>检查客户、行业、地区等维度的集中度风险。</p>
     * 
     * @param riskType 风险类型
     * @param riskDimension 风险维度标识
     * @return 风险检查结果
     */
    boolean checkConcentrationRisk(String riskType, String riskDimension);
    
    /**
     * 定期检查所有集中度风险
     * 
     * <p>检查整个系统中的所有集中度风险。</p>
     * 
     * @return 风险检查结果
     */
    boolean checkAllConcentrationRisks();
    
    void processWarningEvent(Long eventId, String action, String operator);
}
