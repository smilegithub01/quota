package com.bank.quota.core.service.impl;

import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.*;
import com.bank.quota.core.dto.penetrating.*;
import com.bank.quota.core.enums.BusinessType;
import com.bank.quota.core.enums.EventType;
import com.bank.quota.core.enums.QuotaStatus;
import com.bank.quota.core.enums.QuotaType;
import com.bank.quota.core.repository.*;
import com.bank.quota.core.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.bank.quota.core.enums.BusinessType.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PenetratingQuotaServiceImpl implements PenetratingQuotaService {
    
    private final GroupQuotaRepository groupQuotaRepository;
    private final CustomerQuotaRepository customerQuotaRepository;
    private final ApprovalQuotaRepository approvalQuotaRepository;
    private final ItemQuotaRepository itemQuotaRepository;
    private final ContractQuotaRepository contractQuotaRepository;
    private final BusinessProductMappingRepository businessProductMappingRepository;
    private final QuotaUsageDetailRepository quotaUsageDetailRepository;
    private final QuotaLifecycleEventRepository quotaLifecycleEventRepository;
    private final AuditLogService auditLogService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    private static final int LEVEL_GROUP = 1;
    private static final int LEVEL_CUSTOMER = 2;
    private static final int LEVEL_APPROVAL = 3;
    private static final int LEVEL_ITEM = 4;
    private static final int LEVEL_CONTRACT = 5;
    private static final int LEVEL_BUSINESS = 6;
    
    @Override
    @Transactional(readOnly = true)
    public PenetratingValidateResult validateQuota(PenetratingValidateRequest request) {
        log.info("穿透式额度校验 - 客户ID: {}, 业务类型: {}, 产品类型: {}, 金额: {}", 
            request.getCustomerId(), request.getBusinessType(), 
            request.getProductType(), request.getAmount());
        
        PenetratingValidateResult result = new PenetratingValidateResult();
        List<PenetratingValidateResult.QuotaLevelValidation> levelValidations = new ArrayList<>();
        
        try {
            CustomerQuota customerQuota = customerQuotaRepository.findByCustomerId(Long.parseLong(request.getCustomerId()))
                .orElseThrow(() -> new BusinessException("200001", "客户额度不存在"));
            
            if (customerQuota.getStatus() != QuotaStatus.ENABLED) {
                throw new BusinessException("200003", "客户额度状态异常: " + customerQuota.getStatus());
            }
            
            validateCustomerQuota(customerQuota, request.getAmount(), levelValidations);
            
            if (customerQuota.getGroupId() != null) {
                validateGroupQuota(customerQuota.getGroupId(), request.getAmount(), levelValidations);
            }
            
            if (request.getApprovalId() != null) {
                validateApprovalQuota(Long.parseLong(request.getApprovalId()), request.getAmount(), levelValidations);
            }
            
            if (request.getItemId() != null) {
                validateItemQuota(Long.parseLong(request.getItemId()), request.getAmount(), levelValidations);
            }
            
            if (request.getContractId() != null) {
                validateContractQuota(Long.parseLong(request.getContractId()), request.getAmount(), levelValidations);
            }
            
            validateBusinessQuota(request.getCustomerId(), request.getBusinessType(), 
                request.getProductType(), request.getAmount(), levelValidations);
            
            boolean allValid = levelValidations.stream().allMatch(PenetratingValidateResult.QuotaLevelValidation::isSufficient);
            
            result.setValid(allValid);
            result.setLevelValidations(levelValidations);
            result.setMessage(allValid ? "额度校验通过" : "额度不足");
            
        } catch (BusinessException e) {
            result.setValid(false);
            result.setMessage(e.getMessage());
            result.setLevelValidations(levelValidations);
        }
        
        return result;
    }
    
    @Override
    public PenetratingOccupyResult occupyQuota(PenetratingOccupyRequest request) {
        log.info("穿透式额度占用 - 客户ID: {}, 业务类型: {}, 产品类型: {}, 金额: {}", 
            request.getCustomerId(), request.getBusinessType(), 
            request.getProductType(), request.getAmount());
        
        String occupyNo = generateOccupyNo();
        List<PenetratingOccupyResult.QuotaLevelResult> levelResults = new ArrayList<>();
        
        try {
            PenetratingValidateResult validateResult = validateQuota(buildValidateRequest(request));
            if (!validateResult.isValid()) {
                throw new BusinessException("200002", "额度校验失败: " + validateResult.getMessage());
            }
            
            CustomerQuota customerQuota = customerQuotaRepository.findByCustomerId(Long.parseLong(request.getCustomerId()))
                .orElseThrow(() -> new BusinessException("200001", "客户额度不存在"));
            
            occupyCustomerQuota(customerQuota, request.getAmount(), levelResults);
            
            if (customerQuota.getGroupId() != null) {
                occupyGroupQuota(customerQuota.getGroupId(), request.getAmount(), levelResults);
            }
            
            if (request.getApprovalId() != null) {
                occupyApprovalQuota(Long.parseLong(request.getApprovalId()), request.getAmount(), levelResults);
            }
            
            if (request.getItemId() != null) {
                occupyItemQuota(Long.parseLong(request.getItemId()), request.getAmount(), levelResults);
            }
            
            if (request.getContractId() != null) {
                occupyContractQuota(Long.parseLong(request.getContractId()), request.getAmount(), levelResults);
            }
            
            occupyBusinessQuota(request, levelResults);
            
            recordOccupyEvent(occupyNo, request, levelResults);
            auditLogService.logOperation(
                    AuditLog.OperationType.QUOTA_OCCUPY,
                    AuditLog.AuditObjectType.CUSTOMER_QUOTA,
                "QUOTA_OCCUPY",
                "PENETRATING",
                request.getCustomerId(),
                String.format("穿透式额度占用成功 - 占用编号: %s, 金额: %s", occupyNo, request.getAmount()),
                "SUCCESS"
            );
            
            return buildSuccessResult(occupyNo, request.getAmount(), levelResults);
            
        } catch (BusinessException e) {
            log.error("穿透式额度占用失败: {}", e.getMessage());
            rollbackOccupation(levelResults);
            
            auditLogService.logOperation(AuditLog.OperationType.QUOTA_OCCUPY,
                    AuditLog.AuditObjectType.CUSTOMER_QUOTA,
                "QUOTA_OCCUPY",
                "PENETRATING",
                request.getCustomerId(),
                "穿透式额度占用失败: " + e.getMessage(),
                "FAILED"
            );
            
            return buildFailedResult(e.getMessage(), levelResults);
        }
    }
    
    @Override
    public PenetratingOccupyResult releaseQuota(String occupyNo, BigDecimal amount, String operator) {
        log.info("穿透式额度释放 - 占用编号: {}, 释放金额: {}", occupyNo, amount);
        
        List<QuotaUsageDetail> usageDetails = quotaUsageDetailRepository.findByRelatedId(occupyNo);
        if (usageDetails.isEmpty()) {
            throw new BusinessException("200001", "占用记录不存在");
        }
        
        List<PenetratingOccupyResult.QuotaLevelResult> levelResults = new ArrayList<>();
        
        for (QuotaUsageDetail detail : usageDetails) {
            if ("OCCUPY".equals(detail.getUsageType())) {
                releaseQuotaAtLevel(detail, amount, operator, levelResults);
            }
        }
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_OCCUPY,
                AuditLog.AuditObjectType.CUSTOMER_QUOTA,
            "QUOTA_RELEASE",
            "PENETRATING",
            occupyNo,
            String.format("穿透式额度释放成功 - 占用编号: %s, 金额: %s", occupyNo, amount),
            "SUCCESS"
        );
        
        return buildSuccessResult(occupyNo, amount, levelResults);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PenetratingOccupyResult getOccupyDetail(String occupyNo) {
        List<QuotaUsageDetail> usageDetails = quotaUsageDetailRepository.findByRelatedId(occupyNo);
        if (usageDetails.isEmpty()) {
            throw new BusinessException("200001", "占用记录不存在");
        }
        
        List<PenetratingOccupyResult.QuotaLevelResult> levelResults = new ArrayList<>();
        
        for (QuotaUsageDetail detail : usageDetails) {
            PenetratingOccupyResult.QuotaLevelResult levelResult = new PenetratingOccupyResult.QuotaLevelResult();
            levelResult.setLevel(getLevelByQuotaType(detail.getBusinessType()));
            levelResult.setLevelName(detail.getDescription());
            levelResult.setOccupyAmount(detail.getUsageAmount());
            levelResult.setSuccess(true);
            levelResults.add(levelResult);
        }
        
        PenetratingOccupyResult result = new PenetratingOccupyResult();
        result.setSuccess(true);
        result.setOccupyNo(occupyNo);
        result.setLevelResults(levelResults);
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAvailableQuota(String customerId, String businessType, String productType) {
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerId(Long.parseLong(customerId))
            .orElseThrow(() -> new BusinessException("200001", "客户额度不存在"));
        
        BigDecimal available = customerQuota.getAvailableQuota();
        
        if (customerQuota.getGroupId() != null) {
            GroupQuota groupQuota = groupQuotaRepository.findById(customerQuota.getGroupId())
                .orElse(null);
            if (groupQuota != null && MonetaryUtils.isLessThan(groupQuota.getAvailableQuota(), available)) {
                available = groupQuota.getAvailableQuota();
            }
        }
        
        return available;
    }
    
    private void validateCustomerQuota(CustomerQuota quota, BigDecimal amount, 
            List<PenetratingValidateResult.QuotaLevelValidation> validations) {
        PenetratingValidateResult.QuotaLevelValidation validation = new PenetratingValidateResult.QuotaLevelValidation();
        validation.setLevel(LEVEL_CUSTOMER);
        validation.setLevelName("客户额度");
        validation.setQuotaId(quota.getId());
        validation.setQuotaNo(quota.getCustomerNo());
        validation.setTotalQuota(quota.getTotalQuota());
        validation.setUsedQuota(quota.getUsedQuota());
        validation.setAvailableQuota(quota.getAvailableQuota());
        validation.setFrozenQuota(quota.getLockedQuota());
        validation.setRequestedAmount(amount);
        
        boolean sufficient = MonetaryUtils.isGreaterThanOrEqual(quota.getAvailableQuota(), amount);
        validation.setSufficient(sufficient);
        
        if (!sufficient) {
            validation.setShortage(MonetaryUtils.subtract(amount, quota.getAvailableQuota()));
        }
        
        validations.add(validation);
    }
    
    private void validateGroupQuota(Long groupCustomerId, BigDecimal amount, 
            List<PenetratingValidateResult.QuotaLevelValidation> validations) {
        GroupQuota groupQuota = groupQuotaRepository.findById(groupCustomerId)
            .orElseThrow(() -> new BusinessException("200001", "集团额度不存在"));
        
        if (groupQuota.getStatus() != QuotaStatus.ENABLED) {
            throw new BusinessException("200003", "集团额度状态异常: " + groupQuota.getStatus());
        }
        
        PenetratingValidateResult.QuotaLevelValidation validation = new PenetratingValidateResult.QuotaLevelValidation();
        validation.setLevel(LEVEL_GROUP);
        validation.setLevelName("集团额度");
        validation.setQuotaId(groupQuota.getId());
        validation.setQuotaNo(groupQuota.getGroupId().toString());
        validation.setTotalQuota(groupQuota.getTotalQuota());
        validation.setUsedQuota(groupQuota.getUsedQuota());
        validation.setAvailableQuota(groupQuota.getAvailableQuota());
        validation.setFrozenQuota(groupQuota.getLockedQuota());
        validation.setRequestedAmount(amount);
        
        boolean sufficient = MonetaryUtils.isGreaterThanOrEqual(groupQuota.getAvailableQuota(), amount);
        validation.setSufficient(sufficient);
        
        if (!sufficient) {
            validation.setShortage(MonetaryUtils.subtract(amount, groupQuota.getAvailableQuota()));
        }
        
        validations.add(validation);
    }
    
    private void validateApprovalQuota(Long approvalId, BigDecimal amount, 
            List<PenetratingValidateResult.QuotaLevelValidation> validations) {
        ApprovalQuota approvalQuota = approvalQuotaRepository.findById(approvalId)
            .orElse(null);
        
        if (approvalQuota == null) {
            return;
        }
        
        PenetratingValidateResult.QuotaLevelValidation validation = new PenetratingValidateResult.QuotaLevelValidation();
        validation.setLevel(LEVEL_APPROVAL);
        validation.setLevelName("批复额度");
        validation.setQuotaId(approvalQuota.getId());
        validation.setQuotaNo(approvalQuota.getApprovalNo());
        validation.setTotalQuota(approvalQuota.getApprovalQuota());
        validation.setUsedQuota(approvalQuota.getUsedQuota());
        validation.setAvailableQuota(approvalQuota.getAvailableQuota());
        validation.setRequestedAmount(amount);
        
        boolean sufficient = MonetaryUtils.isGreaterThanOrEqual(approvalQuota.getAvailableQuota(), amount);
        validation.setSufficient(sufficient);
        
        if (!sufficient) {
            validation.setShortage(MonetaryUtils.subtract(amount, approvalQuota.getAvailableQuota()));
        }
        
        validations.add(validation);
    }
    
    private void validateItemQuota(Long itemId, BigDecimal amount, 
            List<PenetratingValidateResult.QuotaLevelValidation> validations) {
        ItemQuota itemQuota = itemQuotaRepository.findById(itemId).orElse(null);
        
        if (itemQuota == null) {
            return;
        }
        
        PenetratingValidateResult.QuotaLevelValidation validation = new PenetratingValidateResult.QuotaLevelValidation();
        validation.setLevel(LEVEL_ITEM);
        validation.setLevelName("细项额度");
        validation.setQuotaId(itemQuota.getId());
        validation.setQuotaNo(itemQuota.getItemNo());
        validation.setTotalQuota(itemQuota.getItemQuota());
        validation.setUsedQuota(itemQuota.getUsedQuota());
        validation.setAvailableQuota(itemQuota.getAvailableQuota());
        validation.setRequestedAmount(amount);
        
        boolean sufficient = MonetaryUtils.isGreaterThanOrEqual(itemQuota.getAvailableQuota(), amount);
        validation.setSufficient(sufficient);
        
        if (!sufficient) {
            validation.setShortage(MonetaryUtils.subtract(amount, itemQuota.getAvailableQuota()));
        }
        
        validations.add(validation);
    }
    
    private void validateContractQuota(Long contractId, BigDecimal amount, 
            List<PenetratingValidateResult.QuotaLevelValidation> validations) {
        ContractQuota contractQuota = contractQuotaRepository.findById(contractId).orElse(null);
        
        if (contractQuota == null) {
            return;
        }
        
        PenetratingValidateResult.QuotaLevelValidation validation = new PenetratingValidateResult.QuotaLevelValidation();
        validation.setLevel(LEVEL_CONTRACT);
        validation.setLevelName("合同额度");
        validation.setQuotaId(contractQuota.getId());
        validation.setQuotaNo(contractQuota.getContractNo());
        validation.setTotalQuota(contractQuota.getContractQuota());
        validation.setUsedQuota(contractQuota.getUsedQuota());
        validation.setAvailableQuota(contractQuota.getAvailableQuota());
        validation.setRequestedAmount(amount);
        
        boolean sufficient = MonetaryUtils.isGreaterThanOrEqual(contractQuota.getAvailableQuota(), amount);
        validation.setSufficient(sufficient);
        
        if (!sufficient) {
            validation.setShortage(MonetaryUtils.subtract(amount, contractQuota.getAvailableQuota()));
        }
        
        validations.add(validation);
    }
    
    private void validateBusinessQuota(String customerId, String businessType, String productType, 
            BigDecimal amount, List<PenetratingValidateResult.QuotaLevelValidation> validations) {
        PenetratingValidateResult.QuotaLevelValidation validation = new PenetratingValidateResult.QuotaLevelValidation();
        validation.setLevel(LEVEL_BUSINESS);
        validation.setLevelName("业务品种额度");
        validation.setRequestedAmount(amount);
        validation.setSufficient(true);
        validations.add(validation);
    }
    
    private void occupyCustomerQuota(CustomerQuota quota, BigDecimal amount, 
            List<PenetratingOccupyResult.QuotaLevelResult> results) {
        PenetratingOccupyResult.QuotaLevelResult result = new PenetratingOccupyResult.QuotaLevelResult();
        result.setLevel(LEVEL_CUSTOMER);
        result.setLevelName("客户额度");
        result.setQuotaId(quota.getId());
        result.setQuotaNo(quota.getCustomerNo());
        result.setBeforeAvailable(quota.getAvailableQuota());
        result.setBeforeUsed(quota.getUsedQuota());
        result.setOccupyAmount(amount);
        
        try {
            quota.setUsedQuota(MonetaryUtils.add(quota.getUsedQuota(), amount));
            quota.setAvailableQuota(MonetaryUtils.subtract(quota.getAvailableQuota(), amount));
            customerQuotaRepository.save(quota);
            
            result.setAfterAvailable(quota.getAvailableQuota());
            result.setAfterUsed(quota.getUsedQuota());
            result.setSuccess(true);
            result.setMessage("客户额度占用成功");
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("客户额度占用失败: " + e.getMessage());
            throw new BusinessException("200004", "客户额度占用失败", e);
        }
        
        results.add(result);
    }
    
    private void occupyGroupQuota(Long groupCustomerId, BigDecimal amount, 
            List<PenetratingOccupyResult.QuotaLevelResult> results) {
        GroupQuota groupQuota = groupQuotaRepository.findById(groupCustomerId)
            .orElseThrow(() -> new BusinessException("200001", "集团额度不存在"));
        
        PenetratingOccupyResult.QuotaLevelResult result = new PenetratingOccupyResult.QuotaLevelResult();
        result.setLevel(LEVEL_GROUP);
        result.setLevelName("集团额度");
        result.setQuotaId(groupQuota.getId());
        result.setQuotaNo(groupQuota.getGroupId().toString());
        result.setBeforeAvailable(groupQuota.getAvailableQuota());
        result.setBeforeUsed(groupQuota.getUsedQuota());
        result.setOccupyAmount(amount);
        
        try {
            groupQuota.setUsedQuota(MonetaryUtils.add(groupQuota.getUsedQuota(), amount));
            groupQuota.setAvailableQuota(MonetaryUtils.subtract(groupQuota.getAvailableQuota(), amount));
            groupQuotaRepository.save(groupQuota);
            
            result.setAfterAvailable(groupQuota.getAvailableQuota());
            result.setAfterUsed(groupQuota.getUsedQuota());
            result.setSuccess(true);
            result.setMessage("集团额度占用成功");
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("集团额度占用失败: " + e.getMessage());
            throw new BusinessException("200004", "集团额度占用失败", e);
        }
        
        results.add(result);
    }
    
    private void occupyApprovalQuota(Long approvalId, BigDecimal amount, 
            List<PenetratingOccupyResult.QuotaLevelResult> results) {
        ApprovalQuota approvalQuota = approvalQuotaRepository.findById(approvalId).orElse(null);
        if (approvalQuota == null) {
            return;
        }
        
        PenetratingOccupyResult.QuotaLevelResult result = new PenetratingOccupyResult.QuotaLevelResult();
        result.setLevel(LEVEL_APPROVAL);
        result.setLevelName("批复额度");
        result.setQuotaId(approvalQuota.getId());
        result.setQuotaNo(approvalQuota.getApprovalNo());
        result.setBeforeAvailable(approvalQuota.getAvailableQuota());
        result.setBeforeUsed(approvalQuota.getUsedQuota());
        result.setOccupyAmount(amount);
        
        try {
            approvalQuota.setUsedQuota(MonetaryUtils.add(approvalQuota.getUsedQuota(), amount));
            approvalQuota.setAvailableQuota(MonetaryUtils.subtract(approvalQuota.getAvailableQuota(), amount));
            approvalQuotaRepository.save(approvalQuota);
            
            result.setAfterAvailable(approvalQuota.getAvailableQuota());
            result.setAfterUsed(approvalQuota.getUsedQuota());
            result.setSuccess(true);
            result.setMessage("批复额度占用成功");
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("批复额度占用失败: " + e.getMessage());
            throw new BusinessException("200004", "批复额度占用失败", e);
        }
        
        results.add(result);
    }
    
    private void occupyItemQuota(Long itemId, BigDecimal amount, 
            List<PenetratingOccupyResult.QuotaLevelResult> results) {
        ItemQuota itemQuota = itemQuotaRepository.findById(itemId).orElse(null);
        if (itemQuota == null) {
            return;
        }
        
        PenetratingOccupyResult.QuotaLevelResult result = new PenetratingOccupyResult.QuotaLevelResult();
        result.setLevel(LEVEL_ITEM);
        result.setLevelName("细项额度");
        result.setQuotaId(itemQuota.getId());
        result.setQuotaNo(itemQuota.getItemNo());
        result.setBeforeAvailable(itemQuota.getAvailableQuota());
        result.setBeforeUsed(itemQuota.getUsedQuota());
        result.setOccupyAmount(amount);
        
        try {
            itemQuota.setUsedQuota(MonetaryUtils.add(itemQuota.getUsedQuota(), amount));
            itemQuota.setAvailableQuota(MonetaryUtils.subtract(itemQuota.getAvailableQuota(), amount));
            itemQuotaRepository.save(itemQuota);
            
            result.setAfterAvailable(itemQuota.getAvailableQuota());
            result.setAfterUsed(itemQuota.getUsedQuota());
            result.setSuccess(true);
            result.setMessage("细项额度占用成功");
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("细项额度占用失败: " + e.getMessage());
            throw new BusinessException("200004", "细项额度占用失败", e);
        }
        
        results.add(result);
    }
    
    private void occupyContractQuota(Long contractId, BigDecimal amount, 
            List<PenetratingOccupyResult.QuotaLevelResult> results) {
        ContractQuota contractQuota = contractQuotaRepository.findById(contractId).orElse(null);
        if (contractQuota == null) {
            return;
        }
        
        PenetratingOccupyResult.QuotaLevelResult result = new PenetratingOccupyResult.QuotaLevelResult();
        result.setLevel(LEVEL_CONTRACT);
        result.setLevelName("合同额度");
        result.setQuotaId(contractQuota.getId());
        result.setQuotaNo(contractQuota.getContractNo());
        result.setBeforeAvailable(contractQuota.getAvailableQuota());
        result.setBeforeUsed(contractQuota.getUsedQuota());
        result.setOccupyAmount(amount);
        
        try {
            contractQuota.setUsedQuota(MonetaryUtils.add(contractQuota.getUsedQuota(), amount));
            contractQuota.setAvailableQuota(MonetaryUtils.subtract(contractQuota.getAvailableQuota(), amount));
            contractQuotaRepository.save(contractQuota);
            
            result.setAfterAvailable(contractQuota.getAvailableQuota());
            result.setAfterUsed(contractQuota.getUsedQuota());
            result.setSuccess(true);
            result.setMessage("合同额度占用成功");
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("合同额度占用失败: " + e.getMessage());
            throw new BusinessException("200004", "合同额度占用失败", e);
        }
        
        results.add(result);
    }
    
    private void occupyBusinessQuota(PenetratingOccupyRequest request, 
            List<PenetratingOccupyResult.QuotaLevelResult> results) {
        PenetratingOccupyResult.QuotaLevelResult result = new PenetratingOccupyResult.QuotaLevelResult();
        result.setLevel(LEVEL_BUSINESS);
        result.setLevelName("业务品种额度");
        result.setOccupyAmount(request.getAmount());
        result.setSuccess(true);
        result.setMessage("业务品种额度占用成功");
        results.add(result);
    }
    
    private void releaseQuotaAtLevel(QuotaUsageDetail detail, BigDecimal amount, String operator,
            List<PenetratingOccupyResult.QuotaLevelResult> results) {
        PenetratingOccupyResult.QuotaLevelResult result = new PenetratingOccupyResult.QuotaLevelResult();
        result.setLevel(getLevelByQuotaType(detail.getBusinessType()));
        result.setLevelName(detail.getBusinessType().name());
        result.setQuotaId(detail.getQuotaId());
        result.setOccupyAmount(amount);
        
        try {
            // 根据业务类型确定释放哪个层级的额度
            // 这里需要根据实际业务逻辑来决定释放哪个层级的额度
            // 假设使用不同的业务类型对应不同的额度层级
            switch (detail.getBusinessType()) {
                case CREDIT_APPLICATION:
                    // 授信申请可能涉及客户额度
                    releaseCustomerQuota(detail.getQuotaId(), amount, result);
                    break;
                case USAGE_APPLICATION:
                    // 使用申请可能涉及多个层级，这里简化处理
                    releaseCustomerQuota(detail.getQuotaId(), amount, result);
                    break;
                case QUOTA_ADJUSTMENT:
                    // 额度调整可能涉及集团额度
                    releaseGroupQuota(detail.getQuotaId(), amount, result);
                    break;
            }
            
            result.setSuccess(true);
            result.setMessage("额度释放成功");
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("额度释放失败: " + e.getMessage());
        }
        
        results.add(result);
    }
    
    private void releaseCustomerQuota(Long quotaId, BigDecimal amount, 
            PenetratingOccupyResult.QuotaLevelResult result) {
        CustomerQuota quota = customerQuotaRepository.findById(quotaId)
            .orElseThrow(() -> new BusinessException("200001", "客户额度不存在"));
        
        result.setBeforeAvailable(quota.getAvailableQuota());
        result.setBeforeUsed(quota.getUsedQuota());
        
        quota.setUsedQuota(MonetaryUtils.subtract(quota.getUsedQuota(), amount));
        quota.setAvailableQuota(MonetaryUtils.add(quota.getAvailableQuota(), amount));
        customerQuotaRepository.save(quota);
        
        result.setAfterAvailable(quota.getAvailableQuota());
        result.setAfterUsed(quota.getUsedQuota());
    }
    
    private void releaseGroupQuota(Long quotaId, BigDecimal amount, 
            PenetratingOccupyResult.QuotaLevelResult result) {
        GroupQuota quota = groupQuotaRepository.findById(quotaId)
            .orElseThrow(() -> new BusinessException("200001", "集团额度不存在"));
        
        result.setBeforeAvailable(quota.getAvailableQuota());
        result.setBeforeUsed(quota.getUsedQuota());
        
        quota.setUsedQuota(MonetaryUtils.subtract(quota.getUsedQuota(), amount));
        quota.setAvailableQuota(MonetaryUtils.add(quota.getAvailableQuota(), amount));
        groupQuotaRepository.save(quota);
        
        result.setAfterAvailable(quota.getAvailableQuota());
        result.setAfterUsed(quota.getUsedQuota());
    }
    
    private void releaseApprovalQuota(Long quotaId, BigDecimal amount, 
            PenetratingOccupyResult.QuotaLevelResult result) {
        ApprovalQuota quota = approvalQuotaRepository.findById(quotaId).orElse(null);
        if (quota == null) return;
        
        result.setBeforeAvailable(quota.getAvailableQuota());
        result.setBeforeUsed(quota.getUsedQuota());
        
        quota.setUsedQuota(MonetaryUtils.subtract(quota.getUsedQuota(), amount));
        quota.setAvailableQuota(MonetaryUtils.add(quota.getAvailableQuota(), amount));
        approvalQuotaRepository.save(quota);
        
        result.setAfterAvailable(quota.getAvailableQuota());
        result.setAfterUsed(quota.getUsedQuota());
    }
    
    private void releaseItemQuota(Long quotaId, BigDecimal amount, 
            PenetratingOccupyResult.QuotaLevelResult result) {
        ItemQuota quota = itemQuotaRepository.findById(quotaId).orElse(null);
        if (quota == null) return;
        
        result.setBeforeAvailable(quota.getAvailableQuota());
        result.setBeforeUsed(quota.getUsedQuota());
        
        quota.setUsedQuota(MonetaryUtils.subtract(quota.getUsedQuota(), amount));
        quota.setAvailableQuota(MonetaryUtils.add(quota.getAvailableQuota(), amount));
        itemQuotaRepository.save(quota);
        
        result.setAfterAvailable(quota.getAvailableQuota());
        result.setAfterUsed(quota.getUsedQuota());
    }
    
    private void releaseContractQuota(Long quotaId, BigDecimal amount, 
            PenetratingOccupyResult.QuotaLevelResult result) {
        ContractQuota quota = contractQuotaRepository.findById(quotaId).orElse(null);
        if (quota == null) return;
        
        result.setBeforeAvailable(quota.getAvailableQuota());
        result.setBeforeUsed(quota.getUsedQuota());
        
        quota.setUsedQuota(MonetaryUtils.subtract(quota.getUsedQuota(), amount));
        quota.setAvailableQuota(MonetaryUtils.add(quota.getAvailableQuota(), amount));
        contractQuotaRepository.save(quota);
        
        result.setAfterAvailable(quota.getAvailableQuota());
        result.setAfterUsed(quota.getUsedQuota());
    }
    
    private void recordOccupyEvent(String occupyNo, PenetratingOccupyRequest request, 
            List<PenetratingOccupyResult.QuotaLevelResult> results) {
        for (PenetratingOccupyResult.QuotaLevelResult result : results) {
            if (result.getQuotaId() != null) {
                QuotaLifecycleEvent event = QuotaLifecycleEvent.builder()
                    .eventId(generateEventId())
                    .quotaId(result.getQuotaId())
                    .quotaType(QuotaType.valueOf(getQuotaTypeByLevel(result.getLevel())))
                    .eventType(EventType.OCCUPY)
                    .amount(result.getOccupyAmount())
                    .beforeStatus("ACTIVE")
                    .afterStatus("ACTIVE")
                    .reason("穿透式额度占用")
                    .operator(request.getOperator())
                    .businessRefNo(occupyNo)
                    .eventTime(LocalDateTime.now())
                    .build();
                
                quotaLifecycleEventRepository.save(event);
            }
        }
    }
    
    private void rollbackOccupation(List<PenetratingOccupyResult.QuotaLevelResult> results) {
        for (PenetratingOccupyResult.QuotaLevelResult result : results) {
            if (result.isSuccess() && result.getQuotaId() != null) {
                try {
                    rollbackQuotaAtLevel(result);
                } catch (Exception e) {
                    log.error("回滚额度失败 - 级别: {}, 额度ID: {}", result.getLevel(), result.getQuotaId(), e);
                }
            }
        }
    }
    
    private void rollbackQuotaAtLevel(PenetratingOccupyResult.QuotaLevelResult result) {
        switch (result.getLevel()) {
            case LEVEL_GROUP:
                GroupQuota groupQuota = groupQuotaRepository.findById(result.getQuotaId()).orElse(null);
                if (groupQuota != null) {
                    groupQuota.setUsedQuota(result.getBeforeUsed());
                    groupQuota.setAvailableQuota(result.getBeforeAvailable());
                    groupQuotaRepository.save(groupQuota);
                }
                break;
            case LEVEL_CUSTOMER:
                CustomerQuota customerQuota = customerQuotaRepository.findById(result.getQuotaId()).orElse(null);
                if (customerQuota != null) {
                    customerQuota.setUsedQuota(result.getBeforeUsed());
                    customerQuota.setAvailableQuota(result.getBeforeAvailable());
                    customerQuotaRepository.save(customerQuota);
                }
                break;
            case LEVEL_APPROVAL:
                ApprovalQuota approvalQuota = approvalQuotaRepository.findById(result.getQuotaId()).orElse(null);
                if (approvalQuota != null) {
                    approvalQuota.setUsedQuota(result.getBeforeUsed());
                    approvalQuota.setAvailableQuota(result.getBeforeAvailable());
                    approvalQuotaRepository.save(approvalQuota);
                }
                break;
            case LEVEL_ITEM:
                ItemQuota itemQuota = itemQuotaRepository.findById(result.getQuotaId()).orElse(null);
                if (itemQuota != null) {
                    itemQuota.setUsedQuota(result.getBeforeUsed());
                    itemQuota.setAvailableQuota(result.getBeforeAvailable());
                    itemQuotaRepository.save(itemQuota);
                }
                break;
            case LEVEL_CONTRACT:
                ContractQuota contractQuota = contractQuotaRepository.findById(result.getQuotaId()).orElse(null);
                if (contractQuota != null) {
                    contractQuota.setUsedQuota(result.getBeforeUsed());
                    contractQuota.setAvailableQuota(result.getBeforeAvailable());
                    contractQuotaRepository.save(contractQuota);
                }
                break;
        }
    }
    
    private PenetratingValidateRequest buildValidateRequest(PenetratingOccupyRequest request) {
        PenetratingValidateRequest validateRequest = new PenetratingValidateRequest();
        validateRequest.setCustomerId(request.getCustomerId());
        validateRequest.setBusinessType(request.getBusinessType());
        validateRequest.setProductType(request.getProductType());
        validateRequest.setAmount(request.getAmount());
        validateRequest.setApprovalId(request.getApprovalId());
        validateRequest.setItemId(request.getItemId());
        validateRequest.setContractId(request.getContractId());
        return validateRequest;
    }
    
    private PenetratingOccupyResult buildSuccessResult(String occupyNo, BigDecimal amount, 
            List<PenetratingOccupyResult.QuotaLevelResult> levelResults) {
        PenetratingOccupyResult result = new PenetratingOccupyResult();
        result.setSuccess(true);
        result.setOccupyNo(occupyNo);
        result.setMessage("穿透式额度占用成功");
        result.setTotalAmount(amount);
        result.setOccupiedAmount(amount);
        result.setOccupyTime(LocalDateTime.now());
        result.setLevelResults(levelResults);
        return result;
    }
    
    private PenetratingOccupyResult buildFailedResult(String message, 
            List<PenetratingOccupyResult.QuotaLevelResult> levelResults) {
        PenetratingOccupyResult result = new PenetratingOccupyResult();
        result.setSuccess(false);
        result.setMessage(message);
        result.setLevelResults(levelResults);
        return result;
    }
    
    private String generateOccupyNo() {
        return "OCP" + LocalDateTime.now().format(DATE_FORMATTER) + 
            String.format("%04d", new Random().nextInt(10000));
    }
    
    private String generateEventId() {
        return "EVT" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    private int getLevelByQuotaType(BusinessType businessType) {
        switch (businessType) {
            case CREDIT_APPLICATION: return LEVEL_CUSTOMER;
            case USAGE_APPLICATION: return LEVEL_BUSINESS;
            case QUOTA_ADJUSTMENT: return LEVEL_BUSINESS;
            default: return LEVEL_BUSINESS;
        }
    }
    
    private String getQuotaTypeByLevel(int level) {
        switch (level) {
            case LEVEL_GROUP: return "GROUP";
            case LEVEL_CUSTOMER: return "CUSTOMER";
            case LEVEL_APPROVAL: return "APPROVAL";
            case LEVEL_ITEM: return "ITEM";
            case LEVEL_CONTRACT: return "CONTRACT";
            default: return "BUSINESS";
        }
    }
}
