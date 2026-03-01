package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.ApprovalQuota;
import com.bank.quota.core.domain.AuditLog;
import com.bank.quota.core.domain.ContractOccupancy;
import com.bank.quota.core.dto.approvalquota.*;
import com.bank.quota.core.enums.QuotaStatus;
import com.bank.quota.core.repository.ApprovalQuotaRepository;
import com.bank.quota.core.repository.ContractOccupancyRepository;
import com.bank.quota.core.repository.CustomerQuotaSubRepository;
import com.bank.quota.core.service.AuditLogService;
import com.bank.quota.core.service.ApprovalQuotaService;
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
public class ApprovalQuotaServiceImpl implements ApprovalQuotaService {
    
    private final ApprovalQuotaRepository approvalQuotaRepository;
    private final CustomerQuotaSubRepository customerQuotaSubRepository;
    private final ContractOccupancyRepository contractOccupancyRepository;
    private final AuditLogService auditLogService;
    
    @Override
    @Transactional
    public ApprovalQuotaResponse createApprovalQuota(CreateApprovalQuotaRequest request) {
        log.info("Creating approval quota: approvalNo={}, approvalType={}, approvalQuota={}", 
                request.getApprovalNo(), request.getApprovalType(), request.getApprovalQuota());
        
        if (customerQuotaSubRepository.findById(request.getCustomerQuotaSubId()).isPresent()) {
            throw new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), 
                    "客户细项额度不存在: customerQuotaSubId=" + request.getCustomerQuotaSubId());
        }
        
        if (approvalQuotaRepository.findByApprovalNo(request.getApprovalNo()).isPresent()) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                    "批复额度已存在: approvalNo=" + request.getApprovalNo());
        }
        
        ApprovalQuota approvalQuota = ApprovalQuota.builder()
                .customerQuotaSubId(request.getCustomerQuotaSubId())
                .approvalNo(request.getApprovalNo())
                .approvalType(request.getApprovalType())
                .approvalQuota(request.getApprovalQuota())
                .usedQuota(BigDecimal.ZERO)
                .availableQuota(request.getApprovalQuota())

                .createBy(request.getCreateBy())
                .build();
        
        ApprovalQuota saved = approvalQuotaRepository.save(approvalQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
                saved.getApprovalNo(),
                "创建批复额度: " + saved.getApprovalNo() + ", 额度: " + saved.getApprovalQuota(),
                request.getCreateBy(),
                "SUCCESS");
        
        log.info("Approval quota created successfully: approvalNo={}", saved.getApprovalNo());
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public ApprovalQuotaResponse updateApprovalQuota(Long approvalId, UpdateApprovalQuotaRequest request) {
        log.info("Updating approval quota: approvalId={}", approvalId);
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(approvalId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: approvalId=" + approvalId));
        
        if (request.getApprovalQuota() != null) {
            BigDecimal oldQuota = approvalQuota.getApprovalQuota();
            BigDecimal diff = MonetaryUtils.subtract(request.getApprovalQuota(), oldQuota);
            
            approvalQuota.setApprovalQuota(request.getApprovalQuota());
            approvalQuota.setAvailableQuota(MonetaryUtils.add(approvalQuota.getAvailableQuota(), diff));
        }
        
        if (request.getDescription() != null) {
//            approvalQuota.setDescription(request.getDescription());
        }
        
        approvalQuota.setUpdateBy(request.getUpdateBy());
        ApprovalQuota saved = approvalQuotaRepository.save(approvalQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
                saved.getApprovalNo(),
                "更新批复额度",
                request.getUpdateBy(),
                "SUCCESS");
        
        log.info("Approval quota updated successfully: approvalId={}", approvalId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public ApprovalQuotaResponse freezeApprovalQuota(Long approvalId, String reason, String operator) {
        log.info("Freezing approval quota: approvalId={}, reason={}", approvalId, reason);
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(approvalId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: approvalId=" + approvalId));
        
        if (approvalQuota.getStatus() == QuotaStatus.FROZEN) {
            throw new BusinessException(ErrorCode.QUOTA_FROZEN.getCode(), 
                    "批复额度已冻结");
        }
        
        approvalQuota.setStatus(QuotaStatus.FROZEN);
        ApprovalQuota saved = approvalQuotaRepository.save(approvalQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
                saved.getApprovalNo(),
                "冻结批复额度: " + reason,
                operator,
                "SUCCESS");
        
        log.info("Approval quota frozen successfully: approvalId={}", approvalId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public ApprovalQuotaResponse unfreezeApprovalQuota(Long approvalId, String operator) {
        log.info("Unfreezing approval quota: approvalId={}", approvalId);
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(approvalId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: approvalId=" + approvalId));
        
        if (approvalQuota.getStatus() != QuotaStatus.FROZEN) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "批复额度未冻结");
        }
        
        approvalQuota.setStatus(QuotaStatus.ENABLED);
        ApprovalQuota saved = approvalQuotaRepository.save(approvalQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
                saved.getApprovalNo(),
                "解冻批复额度",
                operator,
                "SUCCESS");
        
        log.info("Approval quota unfrozen successfully: approvalId={}", approvalId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public ApprovalQuotaResponse disableApprovalQuota(Long approvalId, String reason, String operator) {
        log.info("Disabling approval quota: approvalId={}, reason={}", approvalId, reason);
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(approvalId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: approvalId=" + approvalId));
        
        if (approvalQuota.getStatus() == QuotaStatus.DISABLED) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "批复额度已停用");
        }
        
        approvalQuota.setStatus(QuotaStatus.DISABLED);
        ApprovalQuota saved = approvalQuotaRepository.save(approvalQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
                saved.getApprovalNo(),
                "停用批复额度: " + reason,
                operator,
                "SUCCESS");
        
        log.info("Approval quota disabled successfully: approvalId={}", approvalId);
        
        return buildResponse(saved);
    }
    
    @Override
    public ApprovalQuotaResponse getApprovalQuota(Long approvalId) {
        ApprovalQuota approvalQuota = approvalQuotaRepository.findById(approvalId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: approvalId=" + approvalId));
        
        return buildResponse(approvalQuota);
    }
    
    @Override
    public ApprovalQuotaResponse getApprovalQuotaByNo(String approvalNo) {
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByApprovalNo(approvalNo)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: approvalNo=" + approvalNo));
        
        return buildResponse(approvalQuota);
    }
    
    @Override
    public List<ApprovalQuotaResponse> getApprovalQuotasByCustomerQuotaSubId(Long customerQuotaSubId) {
        return approvalQuotaRepository.findByCustomerQuotaSubId(customerQuotaSubId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ApprovalQuotaResponse> getEnabledApprovalQuotas() {
        return approvalQuotaRepository.findAllEnabled().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ApprovalQuotaResponse> getApprovalQuotasByType(String approvalType) {
        return approvalQuotaRepository.findByApprovalType(approvalType).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public ApprovalQuotaUsageResponse getApprovalQuotaUsage(Long approvalId) {
        ApprovalQuota approvalQuota = approvalQuotaRepository.findById(approvalId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: approvalId=" + approvalId));
        
        ApprovalQuotaUsageResponse response = new ApprovalQuotaUsageResponse();
        response.setId(approvalQuota.getId());
        response.setApprovalNo(approvalQuota.getApprovalNo());
        response.setApprovalType(approvalQuota.getApprovalType());
        response.setCustomerQuotaSubId(approvalQuota.getCustomerQuotaSubId());
        response.setApprovalQuota(approvalQuota.getApprovalQuota());
        response.setUsedQuota(approvalQuota.getUsedQuota());
        response.setAvailableQuota(approvalQuota.getAvailableQuota());
        response.setUsageRate(MonetaryUtils.usageRate(approvalQuota.getUsedQuota(), 
                approvalQuota.getApprovalQuota()));
        
        List<ContractOccupancy> occupancies = contractOccupancyRepository
                .findOccupiedByApprovalQuotaSubId(approvalId);
        
        List<ApprovalQuotaUsageResponse.ContractOccupancyInfo> occupancyInfos = occupancies.stream()
                .map(occ -> {
                    ApprovalQuotaUsageResponse.ContractOccupancyInfo info = 
                            new ApprovalQuotaUsageResponse.ContractOccupancyInfo();
                    info.setContractNo(occ.getContractNo());
                    info.setOccupancyAmount(occ.getOccupancyAmount());
                    info.setStatus(occ.getStatus().name());
                    info.setCreateTime(occ.getCreateTime());
                    return info;
                })
                .collect(Collectors.toList());
        
        response.setContractOccupancies(occupancyInfos);
        
        return response;
    }
    
    @Override
    @Transactional
    public void adjustApprovalQuota(Long approvalId, BigDecimal adjustmentAmount, String reason, String operator) {
        log.info("Adjusting approval quota: approvalId={}, adjustmentAmount={}, reason={}", 
                approvalId, adjustmentAmount, reason);
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(approvalId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: approvalId=" + approvalId));
        
        if (MonetaryUtils.isNegative(adjustmentAmount)) {
            if (MonetaryUtils.isLessThan(approvalQuota.getAvailableQuota(), 
                    MonetaryUtils.abs(adjustmentAmount))) {
                throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(), 
                        "调整金额超出可用额度");
            }
        }
        
        approvalQuota.setApprovalQuota(MonetaryUtils.add(approvalQuota.getApprovalQuota(), adjustmentAmount));
        approvalQuota.setAvailableQuota(MonetaryUtils.add(approvalQuota.getAvailableQuota(), adjustmentAmount));
        approvalQuota.setUpdateTime(LocalDateTime.now());
        approvalQuota.setUpdateBy(operator);
        
        approvalQuotaRepository.save(approvalQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
                approvalQuota.getApprovalNo(),
                "调整批复额度: " + reason + ", 调整金额: " + adjustmentAmount,
                operator,
                "SUCCESS");
        
        log.info("Approval quota adjusted successfully: approvalId={}, newApprovalQuota={}", 
                approvalId, approvalQuota.getApprovalQuota());
    }
    
    @Override
    @Transactional
    public void withdrawApprovalQuota(Long approvalId, BigDecimal amount, String reason, String operator) {
        log.info("Withdrawing approval quota: approvalId={}, amount={}, reason={}", 
                approvalId, amount, reason);
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(approvalId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: approvalId=" + approvalId));
        
        if (MonetaryUtils.isGreaterThan(amount, approvalQuota.getUsedQuota())) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(), 
                    "撤回金额不能超过已用额度");
        }
        
        approvalQuota.setApprovalQuota(MonetaryUtils.subtract(approvalQuota.getApprovalQuota(), amount));
        approvalQuota.setUsedQuota(MonetaryUtils.subtract(approvalQuota.getUsedQuota(), amount));
        approvalQuota.setAvailableQuota(MonetaryUtils.subtract(approvalQuota.getAvailableQuota(), amount));
        approvalQuota.setUpdateTime(LocalDateTime.now());
        approvalQuota.setUpdateBy(operator);
        
        approvalQuotaRepository.save(approvalQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
                approvalQuota.getApprovalNo(),
                "撤回批复额度: " + reason + ", 撤回金额: " + amount,
                operator,
                "SUCCESS");
        
        log.info("Approval quota withdrawn successfully: approvalId={}, withdrawnAmount={}", 
                approvalId, amount);
    }
    
    private ApprovalQuotaResponse buildResponse(ApprovalQuota approvalQuota) {
        ApprovalQuotaResponse response = new ApprovalQuotaResponse();
        response.setId(approvalQuota.getId());
        response.setCustomerQuotaSubId(approvalQuota.getCustomerQuotaSubId());
        response.setApprovalNo(approvalQuota.getApprovalNo());
        response.setApprovalType(approvalQuota.getApprovalType());
        response.setApprovalQuota(approvalQuota.getApprovalQuota());
        response.setUsedQuota(approvalQuota.getUsedQuota());
        response.setAvailableQuota(approvalQuota.getAvailableQuota());
        response.setStatus(approvalQuota.getStatus() != null ? approvalQuota.getStatus().name() : null);
//        response.setDescription(approvalQuota.getDescription());
        response.setCreateBy(approvalQuota.getCreateBy());
        response.setUpdateBy(approvalQuota.getUpdateBy());
        response.setCreateTime(approvalQuota.getCreateTime());
        response.setUpdateTime(approvalQuota.getUpdateTime());
        response.setUsageRate(MonetaryUtils.usageRate(approvalQuota.getUsedQuota(), 
                approvalQuota.getApprovalQuota()));
        return response;
    }
}
