package com.bank.quota.core.service.impl;

import com.bank.quota.core.domain.*;
import com.bank.quota.core.dto.quotacontrol.QuotaValidateRequest;
import com.bank.quota.core.dto.quotacontrol.QuotaValidateResult;
import com.bank.quota.core.repository.*;
import com.bank.quota.core.service.QuotaValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuotaValidationServiceImpl implements QuotaValidationService {

    private final QuotaValidationRuleRepository ruleRepository;
    private final GroupQuotaRepository groupQuotaRepository;
    private final GroupQuotaSubRepository groupQuotaSubRepository;
    private final CustomerQuotaRepository customerQuotaRepository;
    private final CustomerQuotaSubRepository customerQuotaSubRepository;
    private final ApprovalQuotaRepository approvalQuotaRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    private static final String RULE_CACHE_NAME = "quotaValidationRules";

    @Override
    public QuotaValidateResult validate(QuotaValidateRequest request) {
        log.info("Starting quota validation: customerId={}, amount={}, businessType={}", 
                request.getCustomerId(), request.getAmount(), request.getBusinessType());

        List<QuotaValidationRule> rules = getActiveRules();
        
        Map<Integer, List<QuotaValidationRule>> rulesByLevel = rules.stream()
                .collect(Collectors.groupingBy(QuotaValidationRule::getValidationLevel, TreeMap::new, Collectors.toList()));

        ValidationContext context = buildValidationContext(request);

        for (Map.Entry<Integer, List<QuotaValidationRule>> entry : rulesByLevel.entrySet()) {
            int level = entry.getKey();
            List<QuotaValidationRule> levelRules = entry.getValue();

            log.debug("Validating level {} with {} rules", level, levelRules.size());

            for (QuotaValidationRule rule : levelRules) {
                if (!isRuleApplicable(rule, request, context)) {
                    continue;
                }

                QuotaValidateResult result = evaluateRule(rule, context);
                if (!result.isPassed()) {
                    log.warn("Validation failed at level {} rule {}: {}", 
                            level, rule.getRuleCode(), result.getMessage());
                    return result;
                }
            }
        }

        log.info("Quota validation passed for customer: {}", request.getCustomerId());
        return QuotaValidateResult.builder()
                .passed(true)
                .message("额度校验通过")
                .validationTime(LocalDateTime.now())
                .build();
    }

    @Override
    public QuotaValidateResult validateByLevel(QuotaValidateRequest request, int level) {
        log.info("Validating quota at level {} for customer: {}", level, request.getCustomerId());

        List<QuotaValidationRule> rules = ruleRepository.findActiveRulesByLevel(level);
        ValidationContext context = buildValidationContext(request);

        for (QuotaValidationRule rule : rules) {
            if (!isRuleApplicable(rule, request, context)) {
                continue;
            }

            QuotaValidateResult result = evaluateRule(rule, context);
            if (!result.isPassed()) {
                return result;
            }
        }

        return QuotaValidateResult.builder()
                .passed(true)
                .message("层级" + level + "校验通过")
                .validationLevel(level)
                .validationTime(LocalDateTime.now())
                .build();
    }

    @Override
    public List<QuotaValidateResult> validateAllLevels(QuotaValidateRequest request) {
        List<QuotaValidateResult> results = new ArrayList<>();
        List<QuotaValidationRule> rules = getActiveRules();

        Map<Integer, List<QuotaValidationRule>> rulesByLevel = rules.stream()
                .collect(Collectors.groupingBy(QuotaValidationRule::getValidationLevel, TreeMap::new, Collectors.toList()));

        ValidationContext context = buildValidationContext(request);

        for (Map.Entry<Integer, List<QuotaValidationRule>> entry : rulesByLevel.entrySet()) {
            int level = entry.getKey();
            List<QuotaValidationRule> levelRules = entry.getValue();

            boolean levelPassed = true;
            StringBuilder levelMessages = new StringBuilder();

            for (QuotaValidationRule rule : levelRules) {
                if (!isRuleApplicable(rule, request, context)) {
                    continue;
                }

                QuotaValidateResult result = evaluateRule(rule, context);
                if (!result.isPassed()) {
                    levelPassed = false;
                    levelMessages.append(result.getMessage()).append("; ");
                }
            }

            results.add(QuotaValidateResult.builder()
                    .passed(levelPassed)
                    .validationLevel(level)
                    .message(levelPassed ? "层级" + level + "校验通过" : levelMessages.toString())
                    .validationTime(LocalDateTime.now())
                    .build());

            if (!levelPassed) {
                break;
            }
        }

        return results;
    }

    @Override
    @CacheEvict(value = RULE_CACHE_NAME, allEntries = true)
    public void reloadRules() {
        log.info("Reloading quota validation rules cache");
    }

    @Override
    @CacheEvict(value = RULE_CACHE_NAME, allEntries = true)
    public void clearRuleCache() {
        log.info("Cleared quota validation rules cache");
    }

    @Cacheable(value = RULE_CACHE_NAME, key = "'allActiveRules'")
    public List<QuotaValidationRule> getActiveRules() {
        return ruleRepository.findAllActiveRules();
    }

    private ValidationContext buildValidationContext(QuotaValidateRequest request) {
        ValidationContext context = new ValidationContext();
        context.setAmount(request.getAmount());
        context.setBusinessType(request.getBusinessType());
        context.setNeedExtraApproval(request.isNeedExtraApproval());

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElse(null);
            context.setCustomer(customer);

            CustomerQuota customerQuota = customerQuotaRepository.findByCustomerId(request.getCustomerId())
                    .orElse(null);
            context.setCustomerQuota(customerQuota);

            if (customerQuota != null) {
                List<CustomerQuotaSub> subQuotas = customerQuotaSubRepository.findByCustomerQuotaId(customerQuota.getId());
                context.setCustomerQuotaSubs(subQuotas);
            }
        }

        if (request.getGroupId() != null) {
            GroupQuota groupQuota = groupQuotaRepository.findByGroupId(request.getGroupId())
                    .orElse(null);
            context.setGroupQuota(groupQuota);

            if (groupQuota != null) {
                List<GroupQuotaSub> groupQuotaSubs = groupQuotaSubRepository.findByGroupQuotaId(groupQuota.getId());
                context.setGroupQuotaSubs(groupQuotaSubs);
            }
        }

        if (request.getApprovalId() != null) {
            ApprovalQuota approvalQuota = approvalQuotaRepository.findById(request.getApprovalId())
                    .orElse(null);
            context.setApprovalQuota(approvalQuota);
        }

        return context;
    }

    private boolean isRuleApplicable(QuotaValidationRule rule, QuotaValidateRequest request, ValidationContext context) {
        if (rule.getBusinessTypes() != null && !rule.getBusinessTypes().isEmpty()) {
            List<String> applicableTypes = Arrays.asList(rule.getBusinessTypes().split(","));
            if (request.getBusinessType() != null && !applicableTypes.contains(request.getBusinessType())) {
                return false;
            }
        }

        if (rule.getRiskLevels() != null && !rule.getRiskLevels().isEmpty()) {
            List<String> applicableLevels = Arrays.asList(rule.getRiskLevels().split(","));
            if (context.getCustomerQuota() != null && context.getCustomerQuota().getRiskLevel() != null) {
                String customerRiskLevel = context.getCustomerQuota().getRiskLevel().name();
                if (!applicableLevels.contains(customerRiskLevel)) {
                    return false;
                }
            }
        }

        return true;
    }

    private QuotaValidateResult evaluateRule(QuotaValidationRule rule, ValidationContext context) {
        try {
            EvaluationContext evalContext = new StandardEvaluationContext(context);
            Boolean result = expressionParser.parseExpression(rule.getRuleExpression())
                    .getValue(evalContext, Boolean.class);

            if (result == null || !result) {
                String errorMessage = formatErrorMessage(rule, context);
                return QuotaValidateResult.builder()
                        .passed(false)
                        .ruleCode(rule.getRuleCode())
                        .ruleName(rule.getRuleName())
                        .validationLevel(rule.getValidationLevel())
                        .errorCode(rule.getErrorCode())
                        .message(errorMessage)
                        .validationTime(LocalDateTime.now())
                        .build();
            }

            return QuotaValidateResult.builder()
                    .passed(true)
                    .ruleCode(rule.getRuleCode())
                    .ruleName(rule.getRuleName())
                    .validationLevel(rule.getValidationLevel())
                    .validationTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error evaluating rule {}: {}", rule.getRuleCode(), e.getMessage(), e);
            return QuotaValidateResult.builder()
                    .passed(false)
                    .ruleCode(rule.getRuleCode())
                    .ruleName(rule.getRuleName())
                    .validationLevel(rule.getValidationLevel())
                    .errorCode("RULE_EVALUATION_ERROR")
                    .message("规则执行异常: " + e.getMessage())
                    .validationTime(LocalDateTime.now())
                    .build();
        }
    }

    private String formatErrorMessage(QuotaValidationRule rule, ValidationContext context) {
        String message = rule.getErrorMessage();

        if (context.getAmount() != null) {
            message = message.replace("{amount}", context.getAmount().toString());
        }
        if (context.getCustomerQuota() != null) {
            message = message.replace("{available}", context.getCustomerQuota().getAvailableQuota().toString());
            if (context.getCustomerQuota().getRiskLevel() != null) {
                message = message.replace("{riskLevel}", context.getCustomerQuota().getRiskLevel().name());
            }
        }
        if (context.getGroupQuota() != null) {
            message = message.replace("{groupAvailable}", context.getGroupQuota().getAvailableQuota().toString());
        }
        if (context.getApprovalQuota() != null) {
            message = message.replace("{approvalNo}", context.getApprovalQuota().getApprovalNo());
            message = message.replace("{approvalAvailable}", context.getApprovalQuota().getAvailableQuota().toString());
        }

        return message;
    }

    public static class ValidationContext {
        private BigDecimal amount;
        private String businessType;
        private boolean needExtraApproval;
        private Customer customer;
        private CustomerQuota customerQuota;
        private List<CustomerQuotaSub> customerQuotaSubs;
        private GroupQuota groupQuota;
        private List<GroupQuotaSub> groupQuotaSubs;
        private ApprovalQuota approvalQuota;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }

        public boolean isNeedExtraApproval() { return needExtraApproval; }
        public void setNeedExtraApproval(boolean needExtraApproval) { this.needExtraApproval = needExtraApproval; }

        public Customer getCustomer() { return customer; }
        public void setCustomer(Customer customer) { this.customer = customer; }

        public CustomerQuota getCustomerQuota() { return customerQuota; }
        public void setCustomerQuota(CustomerQuota customerQuota) { this.customerQuota = customerQuota; }

        public List<CustomerQuotaSub> getCustomerQuotaSubs() { return customerQuotaSubs; }
        public void setCustomerQuotaSubs(List<CustomerQuotaSub> customerQuotaSubs) { this.customerQuotaSubs = customerQuotaSubs; }

        public GroupQuota getGroupQuota() { return groupQuota; }
        public void setGroupQuota(GroupQuota groupQuota) { this.groupQuota = groupQuota; }

        public List<GroupQuotaSub> getGroupQuotaSubs() { return groupQuotaSubs; }
        public void setGroupQuotaSubs(List<GroupQuotaSub> groupQuotaSubs) { this.groupQuotaSubs = groupQuotaSubs; }

        public ApprovalQuota getApprovalQuota() { return approvalQuota; }
        public void setApprovalQuota(ApprovalQuota approvalQuota) { this.approvalQuota = approvalQuota; }

        public double calculateSingleCustomerConcentration(Long customerId) {
            return 0.05;
        }

        public double calculateGroupCustomerConcentration(Long groupId) {
            return 0.08;
        }

        public BigDecimal calculateAffiliateCreditBalance(Long customerId) {
            return BigDecimal.valueOf(1000000);
        }

        public BigDecimal getNetCapital() {
            return BigDecimal.valueOf(100000000);
        }
    }
}
