package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.CreditApplication;
import com.bank.quota.core.domain.Customer;
import com.bank.quota.core.dto.credit.*;
import com.bank.quota.core.enums.ApprovalStatus;
import com.bank.quota.core.repository.CreditApplicationRepository;
import com.bank.quota.core.repository.CustomerRepository;
import com.bank.quota.core.service.CreditApplicationService;
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
 * 授信申请服务实现类
 * 
 * <p>提供银行级信贷额度管控平台的授信申请功能实现。
 * 包括授信申请的创建、查询、审批等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditApplicationServiceImpl implements CreditApplicationService {
    
    private final CreditApplicationRepository creditApplicationRepository;
    private final CustomerRepository customerRepository;
    
    @Override
    @Transactional
    public CreditApplicationResponse createCreditApplication(CreditApplicationRequest request) {
        log.info("Creating credit application: customerId={}, appliedQuota={}", 
                request.getCustomerId(), request.getAppliedQuota());
        
        // 验证客户是否存在
        Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
        if (!customerOpt.isPresent()) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                    "客户不存在: " + request.getCustomerId());
        }
        
        // 检查是否有相同的草稿申请
        List<CreditApplication> existingDrafts = creditApplicationRepository
                .findByCustomerIdAndStatus(request.getCustomerId(), ApprovalStatus.DRAFT);
        if (!existingDrafts.isEmpty()) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                    "客户已存在草稿状态的授信申请，请先处理现有申请");
        }
        
        // 创建授信申请实体
        CreditApplication application = CreditApplication.builder()
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .businessType(request.getBusinessType())
                .appliedQuota(request.getAppliedQuota())
                .currency(request.getCurrency())
                .guaranteeType(request.getGuaranteeType())
                .collateralValue(request.getCollateralValue())
                .termMonths(request.getTermMonths())
                .interestRate(request.getInterestRate())
                .purpose(request.getPurpose())
                .riskLevel(request.getRiskLevel())
                .status(ApprovalStatus.DRAFT)
                .description(request.getDescription())
                .createBy(request.getApplicant())
                .build();
        
        CreditApplication saved = creditApplicationRepository.save(application);
        
        log.info("Credit application created successfully: applicationNo={}", saved.getApplicationNo());
        
        return buildResponse(saved);
    }
    
    @Override
    public CreditApplicationResponse getCreditApplication(Long applicationId) {
        log.debug("Getting credit application: applicationId={}", applicationId);
        
        CreditApplication application = creditApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "授信申请不存在: " + applicationId));
        
        return buildResponse(application);
    }
    
    @Override
    public List<CreditApplicationResponse> getApplicationsByCustomer(Long customerId) {
        log.debug("Getting credit applications for customer: customerId={}", customerId);
        
        List<CreditApplication> applications = creditApplicationRepository.findByCustomerId(customerId);
        
        return applications.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CreditApplicationResponse> getApplicationsByStatus(String status) {
        log.debug("Getting credit applications by status: status={}", status);
        
        ApprovalStatus approvalStatus = ApprovalStatus.valueOf(status.toUpperCase());
        List<CreditApplication> applications = creditApplicationRepository.findByStatus(approvalStatus);
        
        return applications.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CreditApplicationResponse approveCreditApplication(CreditApplicationApprovalRequest request) {
        log.info("Approving credit application: applicationId={}, status={}", 
                request.getApplicationId(), request.getStatus());
        
        CreditApplication application = creditApplicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "授信申请不存在: " + request.getApplicationId()));
        
        if (application.getStatus() != ApprovalStatus.SUBMITTED && application.getStatus() != ApprovalStatus.UNDER_REVIEW) {
            throw new BusinessException(ErrorCode.AUTH_ERROR.getCode(), 
                    "只有已提交或审核中的申请才能被审批");
        }
        
        // 更新申请信息
        if (request.getApprovedQuota() != null) {
            application.setApprovedQuota(request.getApprovedQuota());
        }
        
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
        
        CreditApplication updated = creditApplicationRepository.save(application);
        
        log.info("Credit application approved successfully: applicationNo={}, status={}", 
                updated.getApplicationNo(), updated.getStatus());
        
        return buildResponse(updated);
    }
    
    @Override
    @Transactional
    public CreditApplicationResponse updateCreditApplication(Long applicationId, CreditApplicationRequest request) {
        log.info("Updating credit application: applicationId={}", applicationId);
        
        CreditApplication application = creditApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "授信申请不存在: " + applicationId));
        
        if (application.getStatus() != ApprovalStatus.DRAFT) {
            throw new BusinessException(ErrorCode.AUTH_ERROR.getCode(), 
                    "只能修改草稿状态的申请");
        }
        
        // 更新申请信息
        if (request.getCustomerName() != null) {
            application.setCustomerName(request.getCustomerName());
        }
        
        if (request.getBusinessType() != null) {
            application.setBusinessType(request.getBusinessType());
        }
        
        if (request.getAppliedQuota() != null) {
            application.setAppliedQuota(request.getAppliedQuota());
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
        
        if (request.getDescription() != null) {
            application.setDescription(request.getDescription());
        }
        
        application.setUpdateBy(request.getApplicant());
        
        CreditApplication updated = creditApplicationRepository.save(application);
        
        log.info("Credit application updated successfully: applicationNo={}", updated.getApplicationNo());
        
        return buildResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteCreditApplication(Long applicationId) {
        log.info("Deleting credit application: applicationId={}", applicationId);
        
        CreditApplication application = creditApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "授信申请不存在: " + applicationId));
        
        if (application.getStatus() != ApprovalStatus.DRAFT && application.getStatus() != ApprovalStatus.REJECTED) {
            throw new BusinessException(ErrorCode.AUTH_ERROR.getCode(), 
                    "只能删除草稿或已拒绝的申请");
        }
        
        creditApplicationRepository.delete(application);
        
        log.info("Credit application deleted successfully: applicationId={}", applicationId);
    }
    
    @Override
    @Transactional
    public CreditApplicationResponse submitApplication(Long applicationId, String submitter) {
        log.info("Submitting credit application: applicationId={}, submitter={}", applicationId, submitter);
        
        CreditApplication application = creditApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "授信申请不存在: " + applicationId));
        
        if (application.getStatus() != ApprovalStatus.DRAFT) {
            throw new BusinessException(ErrorCode.AUTH_ERROR.getCode(), 
                    "只能提交草稿状态的申请");
        }
        
        if (application.getAppliedQuota() == null || 
            MonetaryUtils.isLessThanOrEqual(application.getAppliedQuota(), BigDecimal.ZERO)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "申请额度不能为空或小于等于0");
        }
        
        application.setStatus(ApprovalStatus.SUBMITTED);
        application.setUpdateBy(submitter);
        
        CreditApplication updated = creditApplicationRepository.save(application);
        
        log.info("Credit application submitted successfully: applicationNo={}", updated.getApplicationNo());
        
        return buildResponse(updated);
    }
    
    @Override
    public List<CreditApplicationResponse> getPendingApplications() {
        log.debug("Getting pending credit applications");
        
        List<CreditApplication> applications = creditApplicationRepository.findByStatusIn(
                java.util.Arrays.asList(ApprovalStatus.SUBMITTED, ApprovalStatus.UNDER_REVIEW));
        
        return applications.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CreditApplicationResponse cancelApplication(Long applicationId, String reason) {
        log.info("Canceling credit application: applicationId={}, reason={}", applicationId, reason);
        
        CreditApplication application = creditApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "授信申请不存在: " + applicationId));
        
        if (application.getStatus() != ApprovalStatus.SUBMITTED && application.getStatus() != ApprovalStatus.UNDER_REVIEW) {
            throw new BusinessException(ErrorCode.AUTH_ERROR.getCode(), 
                    "只能撤销已提交或审核中的申请");
        }
        
        application.setStatus(ApprovalStatus.CANCELLED);
        application.setReviewComments("撤销原因: " + reason);
        
        CreditApplication updated = creditApplicationRepository.save(application);
        
        log.info("Credit application cancelled successfully: applicationNo={}", updated.getApplicationNo());
        
        return buildResponse(updated);
    }
    
    /**
     * 构建响应对象
     */
    private CreditApplicationResponse buildResponse(CreditApplication application) {
        return CreditApplicationResponse.builder()
                .id(application.getId())
                .applicationNo(application.getApplicationNo())
                .customerId(application.getCustomerId())
                .customerName(application.getCustomerName())
                .businessType(application.getBusinessType())
                .appliedQuota(application.getAppliedQuota())
                .approvedQuota(application.getApprovedQuota())
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
                .description(application.getDescription())
                .createBy(application.getCreateBy())
                .updateBy(application.getUpdateBy())
                .createTime(application.getCreateTime())
                .updateTime(application.getUpdateTime())
                .build();
    }
}