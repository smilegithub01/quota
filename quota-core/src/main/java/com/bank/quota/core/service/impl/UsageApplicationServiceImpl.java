package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.Customer;
import com.bank.quota.core.domain.UsageApplication;
import com.bank.quota.core.dto.usage.*;
import com.bank.quota.core.enums.UsageStatus;
import com.bank.quota.core.repository.CustomerRepository;
import com.bank.quota.core.repository.UsageApplicationRepository;
import com.bank.quota.core.service.UsageApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用信申请服务实现类
 * 
 * <p>提供银行级信贷额度管控平台的用信申请功能实现。
 * 包括用信申请的创建、查询、审批等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsageApplicationServiceImpl implements UsageApplicationService {
    
    private final UsageApplicationRepository usageApplicationRepository;
    private final CustomerRepository customerRepository;
    
    @Override
    @Transactional
    public UsageApplicationResponse createUsageApplication(UsageApplicationRequest request) {
        log.info("Creating usage application: customerId={}, usageQuota={}", 
                request.getCustomerId(), request.getUsageQuota());
        
        // 验证客户是否存在
        Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
        if (!customerOpt.isPresent()) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                    "客户不存在: " + request.getCustomerId());
        }
        
        // 检查是否有相同的草稿申请
        List<UsageApplication> existingDrafts = usageApplicationRepository
                .findByCustomerIdAndStatus(request.getCustomerId(), UsageStatus.DRAFT);
        if (!existingDrafts.isEmpty()) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                    "客户已存在草稿状态的用信申请，请先处理现有申请");
        }
        
        // 创建用信申请实体
        UsageApplication application = UsageApplication.builder()
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .businessType(request.getBusinessType())
                .usageQuota(request.getUsageQuota())
                .currency(request.getCurrency())
                .guaranteeType(request.getGuaranteeType())
                .collateralValue(request.getCollateralValue())
                .termMonths(request.getTermMonths())
                .interestRate(request.getInterestRate())
                .purpose(request.getPurpose())
                .riskLevel(request.getRiskLevel())
                .status(UsageStatus.DRAFT)
                .contractId(request.getContractId())
                .relatedApprovalId(request.getRelatedApprovalId())
                .description(request.getDescription())
                .createBy(request.getApplicant())
                .build();
        
        UsageApplication saved = usageApplicationRepository.save(application);
        
        log.info("Usage application created successfully: applicationNo={}", saved.getApplicationNo());
        
        return buildResponse(saved);
    }
    
    @Override
    public UsageApplicationResponse getUsageApplication(Long applicationId) {
        log.debug("Getting usage application: applicationId={}", applicationId);
        
        UsageApplication application = usageApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "用信申请不存在: " + applicationId));
        
        return buildResponse(application);
    }
    
    @Override
    public List<UsageApplicationResponse> getApplicationsByCustomer(Long customerId) {
        log.debug("Getting usage applications for customer: customerId={}", customerId);
        
        List<UsageApplication> applications = usageApplicationRepository.findByCustomerId(customerId);
        
        return applications.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UsageApplicationResponse> getApplicationsByStatus(String status) {
        log.debug("Getting usage applications by status: status={}", status);
        
        UsageStatus usageStatus = UsageStatus.valueOf(status.toUpperCase());
        List<UsageApplication> applications = usageApplicationRepository.findByStatus(usageStatus);
        
        return applications.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UsageApplicationResponse approveUsageApplication(UsageApplicationApprovalRequest request) {
        log.info("Approving usage application: applicationId={}, status={}", 
                request.getApplicationId(), request.getStatus());
        
        UsageApplication application = usageApplicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "用信申请不存在: " + request.getApplicationId()));
        
        if (application.getStatus() != UsageStatus.SUBMITTED && application.getStatus() != UsageStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), 
                    "只有已提交或审核中的申请才能被审批");
        }
        
        // 更新申请信息
        if (request.getReviewComments() != null) {
            application.setReviewComments(request.getReviewComments());
        }
        
        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        }
        
        if (request.getEffectiveDate() != null) {
            application.setEffectiveDate(request.getEffectiveDate());
        }
        
        if (request.getExpiryDate() != null) {
            application.setExpiryDate(request.getExpiryDate());
        }
        
        if (request.getApproverId() != null) {
            application.setApproverId(request.getApproverId());
        }
        
        if (request.getApproverName() != null) {
            application.setApproverName(request.getApproverName());
        }
        
        application.setApproveTime(LocalDateTime.now());
        application.setUpdateBy(request.getOperator());
        
        UsageApplication updated = usageApplicationRepository.save(application);
        
        log.info("Usage application approved successfully: applicationNo={}, status={}", 
                updated.getApplicationNo(), updated.getStatus());
        
        return buildResponse(updated);
    }
    
    @Override
    @Transactional
    public UsageApplicationResponse updateUsageApplication(Long applicationId, UsageApplicationRequest request) {
        log.info("Updating usage application: applicationId={}", applicationId);
        
        UsageApplication application = usageApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "用信申请不存在: " + applicationId));
        
        if (application.getStatus() != UsageStatus.DRAFT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), 
                    "只能修改草稿状态的申请");
        }
        
        // 更新申请信息
        if (request.getCustomerName() != null) {
            application.setCustomerName(request.getCustomerName());
        }
        
        if (request.getBusinessType() != null) {
            application.setBusinessType(request.getBusinessType());
        }
        
        if (request.getUsageQuota() != null) {
            application.setUsageQuota(request.getUsageQuota());
        }
        
        if (request.getCurrency() != null) {
            application.setCurrency(request.getCurrency());
        }
        
        if (request.getGuaranteeType() != null) {
            application.setGuaranteeType(request.getGuaranteeType());
        }
        
        if (request.getCollateralValue() != null) {
            application.setCollateralValue(request.getCollateralValue());
        }
        
        if (request.getTermMonths() != null) {
            application.setTermMonths(request.getTermMonths());
        }
        
        if (request.getInterestRate() != null) {
            application.setInterestRate(request.getInterestRate());
        }
        
        if (request.getPurpose() != null) {
            application.setPurpose(request.getPurpose());
        }
        
        if (request.getRiskLevel() != null) {
            application.setRiskLevel(request.getRiskLevel());
        }
        
        if (request.getContractId() != null) {
            application.setContractId(request.getContractId());
        }
        
        if (request.getRelatedApprovalId() != null) {
            application.setRelatedApprovalId(request.getRelatedApprovalId());
        }
        
        if (request.getDescription() != null) {
            application.setDescription(request.getDescription());
        }
        
        application.setUpdateBy(request.getApplicant());
        
        UsageApplication updated = usageApplicationRepository.save(application);
        
        log.info("Usage application updated successfully: applicationNo={}", updated.getApplicationNo());
        
        return buildResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteUsageApplication(Long applicationId) {
        log.info("Deleting usage application: applicationId={}", applicationId);
        
        UsageApplication application = usageApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "用信申请不存在: " + applicationId));
        
        if (application.getStatus() != UsageStatus.DRAFT && application.getStatus() != UsageStatus.REJECTED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), 
                    "只能删除草稿或已拒绝的申请");
        }
        
        usageApplicationRepository.delete(application);
        
        log.info("Usage application deleted successfully: applicationId={}", applicationId);
    }
    
    @Override
    @Transactional
    public UsageApplicationResponse submitApplication(Long applicationId, String submitter) {
        log.info("Submitting usage application: applicationId={}, submitter={}", applicationId, submitter);
        
        UsageApplication application = usageApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "用信申请不存在: " + applicationId));
        
        if (application.getStatus() != UsageStatus.DRAFT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), 
                    "只能提交草稿状态的申请");
        }
        
        if (application.getUsageQuota() == null || 
            MonetaryUtils.isLessThanOrEqual(application.getUsageQuota(), BigDecimal.ZERO)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "用信额度不能为空或小于等于0");
        }
        
        application.setStatus(UsageStatus.SUBMITTED);
        application.setUpdateBy(submitter);
        
        UsageApplication updated = usageApplicationRepository.save(application);
        
        log.info("Usage application submitted successfully: applicationNo={}", updated.getApplicationNo());
        
        return buildResponse(updated);
    }
    
    @Override
    public List<UsageApplicationResponse> getPendingApplications() {
        log.debug("Getting pending usage applications");
        
        List<UsageApplication> applications = usageApplicationRepository.findByStatusIn(
                List.of(UsageStatus.SUBMITTED, UsageStatus.IN_REVIEW));
        
        return applications.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UsageApplicationResponse cancelApplication(Long applicationId, String reason) {
        log.info("Canceling usage application: applicationId={}, reason={}", applicationId, reason);
        
        UsageApplication application = usageApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "用信申请不存在: " + applicationId));
        
        if (application.getStatus() != UsageStatus.SUBMITTED && application.getStatus() != UsageStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), 
                    "只能撤销已提交或审核中的申请");
        }
        
        application.setStatus(UsageStatus.CANCELLED);
        application.setReviewComments("撤销原因: " + reason);
        
        UsageApplication updated = usageApplicationRepository.save(application);
        
        log.info("Usage application cancelled successfully: applicationNo={}", updated.getApplicationNo());
        
        return buildResponse(updated);
    }
    
    @Override
    @Transactional
    public UsageApplicationResponse writeOffApplication(Long applicationId, BigDecimal usageAmount, String operator) {
        log.info("Writing off usage application: applicationId={}, usageAmount={}", applicationId, usageAmount);
        
        UsageApplication application = usageApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "用信申请不存在: " + applicationId));
        
        if (application.getStatus() != UsageStatus.APPROVED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), 
                    "只能核销已批准的申请");
        }
        
        if (usageAmount == null || MonetaryUtils.isLessThanOrEqual(usageAmount, BigDecimal.ZERO)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "核销金额不能为空或小于等于0");
        }
        
        // 检查额度是否足够
        BigDecimal totalAvailable = application.getUsageQuota().subtract(application.getUsedQuota());
        if (MonetaryUtils.isLessThan(totalAvailable, usageAmount)) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(),
                    "可用额度不足，申请额度: " + application.getUsageQuota() + 
                    ", 已用额度: " + application.getUsedQuota() + 
                    ", 本次使用: " + usageAmount);
        }
        
        // 更新已用额度
        application.setUsedQuota(MonetaryUtils.add(application.getUsedQuota(), usageAmount));
        
        // 如果已用额度达到申请额度，则更新状态为已完成
        if (MonetaryUtils.isGreaterThan(application.getUsageQuota(), application.getUsedQuota())) {
            application.setStatus(UsageStatus.EXECUTED);
        }
        
        application.setUpdateBy(operator);
        
        UsageApplication updated = usageApplicationRepository.save(application);
        
        log.info("Usage application written off successfully: applicationNo={}, usedQuota={}", 
                updated.getApplicationNo(), updated.getUsedQuota());
        
        return buildResponse(updated);
    }
    
    /**
     * 构建响应对象
     */
    private UsageApplicationResponse buildResponse(UsageApplication application) {
        return UsageApplicationResponse.builder()
                .id(application.getId())
                .applicationNo(application.getApplicationNo())
                .customerId(application.getCustomerId())
                .customerName(application.getCustomerName())
                .businessType(application.getBusinessType())
                .usageQuota(application.getUsageQuota())
                .usedQuota(application.getUsedQuota())
                .currency(application.getCurrency())
                .guaranteeType(application.getGuaranteeType())
                .collateralValue(application.getCollateralValue())
                .termMonths(application.getTermMonths())
                .interestRate(application.getInterestRate())
                .purpose(application.getPurpose())
                .riskLevel(application.getRiskLevel())
                .status(application.getStatus())
                .reviewComments(application.getReviewComments())
                .approverId(application.getApproverId())
                .approverName(application.getApproverName())
                .approveTime(application.getApproveTime())
                .effectiveDate(application.getEffectiveDate())
                .expiryDate(application.getExpiryDate())
                .contractId(application.getContractId())
                .relatedApprovalId(application.getRelatedApprovalId())
                .description(application.getDescription())
                .createBy(application.getCreateBy())
                .updateBy(application.getUpdateBy())
                .createTime(application.getCreateTime())
                .updateTime(application.getUpdateTime())
                .build();
    }
}