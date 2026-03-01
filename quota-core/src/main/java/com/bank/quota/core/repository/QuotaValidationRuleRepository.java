package com.bank.quota.core.repository;

import com.bank.quota.core.domain.QuotaValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaValidationRuleRepository extends JpaRepository<QuotaValidationRule, Long> {

    Optional<QuotaValidationRule> findByRuleCode(String ruleCode);

    List<QuotaValidationRule> findByStatus(QuotaValidationRule.RuleStatus status);

    List<QuotaValidationRule> findByValidationLevelOrderByPriorityAsc(Integer validationLevel);

    @Query("SELECT r FROM QuotaValidationRule r WHERE r.status = 'ENABLED' " +
           "AND (r.effectiveDate IS NULL OR r.effectiveDate <= CURRENT_TIMESTAMP) " +
           "AND (r.expireDate IS NULL OR r.expireDate > CURRENT_TIMESTAMP) " +
           "ORDER BY r.validationLevel ASC, r.priority ASC")
    List<QuotaValidationRule> findAllActiveRules();

    @Query("SELECT r FROM QuotaValidationRule r WHERE r.status = 'ENABLED' " +
           "AND r.validationLevel = :level " +
           "AND (r.effectiveDate IS NULL OR r.effectiveDate <= CURRENT_TIMESTAMP) " +
           "AND (r.expireDate IS NULL OR r.expireDate > CURRENT_TIMESTAMP) " +
           "ORDER BY r.priority ASC")
    List<QuotaValidationRule> findActiveRulesByLevel(@Param("level") Integer level);

    @Query("SELECT r FROM QuotaValidationRule r WHERE r.status = 'ENABLED' " +
           "AND r.validationObject = :object " +
           "AND (r.effectiveDate IS NULL OR r.effectiveDate <= CURRENT_TIMESTAMP) " +
           "AND (r.expireDate IS NULL OR r.expireDate > CURRENT_TIMESTAMP) " +
           "ORDER BY r.validationLevel ASC, r.priority ASC")
    List<QuotaValidationRule> findActiveRulesByObject(@Param("object") QuotaValidationRule.ValidationObject object);

    @Query("SELECT r FROM QuotaValidationRule r WHERE r.status = 'ENABLED' " +
           "AND r.ruleType = :type " +
           "ORDER BY r.validationLevel ASC, r.priority ASC")
    List<QuotaValidationRule> findActiveRulesByType(@Param("type") QuotaValidationRule.RuleType type);

    @Query("SELECT MAX(r.version) FROM QuotaValidationRule r WHERE r.ruleCode = :ruleCode")
    Integer findMaxVersionByRuleCode(@Param("ruleCode") String ruleCode);

    boolean existsByRuleCode(String ruleCode);
}
