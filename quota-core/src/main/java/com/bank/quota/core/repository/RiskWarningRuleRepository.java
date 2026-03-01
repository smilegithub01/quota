package com.bank.quota.core.repository;

import com.bank.quota.core.domain.RiskWarningRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskWarningRuleRepository extends JpaRepository<RiskWarningRule, Long> {
    
    Optional<RiskWarningRule> findByRuleCode(String ruleCode);
    
    List<RiskWarningRule> findByRuleType(RiskWarningRule.RuleType ruleType);
    
    List<RiskWarningRule> findByWarningLevel(RiskWarningRule.WarningLevel warningLevel);
    
    List<RiskWarningRule> findByObjectType(RiskWarningRule.QuotaObjectType objectType);
    
    @Query("SELECT r FROM RiskWarningRule r WHERE r.status = 'ENABLED'")
    List<RiskWarningRule> findAllEnabled();
    
    @Query("SELECT r FROM RiskWarningRule r WHERE r.status = 'ENABLED' AND r.objectType = :objectType")
    List<RiskWarningRule> findEnabledByObjectType(@Param("objectType") RiskWarningRule.QuotaObjectType objectType);
    
    boolean existsByRuleCode(String ruleCode);
}
