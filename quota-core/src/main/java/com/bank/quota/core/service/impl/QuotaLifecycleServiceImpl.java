package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.*;
import com.bank.quota.core.dto.lifecycle.*;
import com.bank.quota.core.enums.*;
import com.bank.quota.core.repository.*;
import com.bank.quota.core.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuotaLifecycleServiceImpl implements QuotaLifecycleService {
    
    private final QuotaAdjustmentRepository quotaAdjustmentRepository;
    private final QuotaFreezeRepository quotaFreezeRepository;
    private final QuotaTerminationRepository quotaTerminationRepository;
    private final QuotaLifecycleEventRepository quotaLifecycleEventRepository;
    private final GroupQuotaRepository groupQuotaRepository;
    private final CustomerQuotaRepository customerQuotaRepository;
    private final ApprovalQuotaRepository approvalQuotaRepository;
    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    public QuotaLifecycleResponse adjustQuota(QuotaAdjustRequest request) {
        log.info("额度调整申请 - 额度ID: {}, 调整类型: {}, 调整金额: {}", 
            request.getQuotaId(), request.getAdjustmentType(), request.getAdjustmentAmount());
        
        Object quota = getQuotaByIdAndType(request.getQuotaId(), request.getQuotaType());
        if (quota == null) {
            throw new BusinessException("200001", "额度不存在");
        }
        
        BigDecimal currentAmount = getQuotaAmount(quota);
        BigDecimal adjustmentAmount = request.getAdjustmentAmount();
        BigDecimal afterAmount = MonetaryUtils.add(currentAmount, adjustmentAmount);
        
        if (MonetaryUtils.isNegative(afterAmount)) {
            throw new BusinessException("200002", "调整后额度不能为负数");
        }
        
        if (MonetaryUtils.isNegative(adjustmentAmount)) {
            BigDecimal usedAmount = getUsedAmount(quota);
            if (MonetaryUtils.isLessThan(afterAmount, usedAmount)) {
                throw new BusinessException("200003", "调整后额度不能小于已用额度");
            }
        }
        
        if ("TEMPORARY".equals(request.getAdjustmentType())) {
            if (request.getExpiryTime() == null) {
                throw new BusinessException("100002", "临时调整必须指定失效时间");
            }
            if (request.getExpiryTime().isAfter(LocalDateTime.now().plusYears(1))) {
                throw new BusinessException("100002", "临时调整有效期最长为1年");
            }
        }
        
        QuotaAdjustment adjustment = QuotaAdjustment.builder()
            .adjustmentNo(generateAdjustmentNo())
            .quotaId(request.getQuotaId())
            .quotaType(QuotaType.valueOf(request.getQuotaType()))
            .adjustmentType(AdjustmentType.valueOf(request.getAdjustmentType()))
            .adjustmentAmount(adjustmentAmount)
            .beforeAmount(currentAmount)
            .afterAmount(afterAmount)
            .reason(request.getReason())
            .effectiveTime(request.getEffectiveTime())
            .expiryTime(request.getExpiryTime())
            .status(AdjustmentStatus.PENDING)
            .applicant(request.getApplicant())
            .build();
        
        quotaAdjustmentRepository.save(adjustment);
        
        recordLifecycleEvent(
            request.getQuotaId(),
            QuotaType.valueOf(request.getQuotaType()),
            EventType.ADJUST,
            adjustmentAmount,
            "PENDING",
            "PENDING",
            request.getReason(),
            request.getApplicant(),
            adjustment.getAdjustmentNo()
        );
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
            "QUOTA_ADJUST",
            request.getQuotaType(),
            request.getQuotaId().toString(),
            "额度调整申请：" + request.getReason(),
            "SUCCESS"
        );
        
        return buildLifecycleResponse(request.getQuotaId(), request.getQuotaType());
    }
    
    @Override
    public QuotaLifecycleResponse approveAdjustment(String adjustmentNo, boolean approved, 
            String approveRemark, String approver) {
        log.info("额度调整审批 - 调整编号: {}, 审批结果: {}", adjustmentNo, approved ? "通过" : "拒绝");
        
        QuotaAdjustment adjustment = quotaAdjustmentRepository.findByAdjustmentNo(adjustmentNo)
            .orElseThrow(() -> new BusinessException("200001", "调整记录不存在"));
        
        if (adjustment.getStatus() != AdjustmentStatus.PENDING) {
            throw new BusinessException("200003", "调整记录状态不正确");
        }
        
        adjustment.setApprover(approver);
        adjustment.setApproveTime(LocalDateTime.now());
        
        if (approved) {
            adjustment.setStatus(AdjustmentStatus.APPROVED);
            
            Object quota = getQuotaByIdAndType(adjustment.getQuotaId(), adjustment.getQuotaType().name());
            if (quota == null) {
                throw new BusinessException("200001", "额度不存在");
            }
            
            updateQuotaAmount(quota, adjustment.getAfterAmount());
            adjustment.setStatus(AdjustmentStatus.COMPLETED);
            
            recordLifecycleEvent(
                adjustment.getQuotaId(),
                adjustment.getQuotaType(),
                EventType.ADJUST,
                adjustment.getAdjustmentAmount(),
                "ACTIVE",
                "ACTIVE",
                adjustment.getReason(),
                approver,
                adjustment.getAdjustmentNo()
            );
        } else {
            adjustment.setStatus(AdjustmentStatus.REJECTED);
        }
        
        quotaAdjustmentRepository.save(adjustment);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
            "QUOTA_ADJUST_APPROVE",
            adjustment.getQuotaType().name(),
            adjustment.getQuotaId().toString(),
            "额度调整审批：" + (approved ? "通过" : "拒绝") + " - " + approveRemark,
            "SUCCESS"
        );
        
        return buildLifecycleResponse(adjustment.getQuotaId(), adjustment.getQuotaType().name());
    }
    
    @Override
    public QuotaLifecycleResponse freezeQuota(QuotaFreezeRequest request) {
        log.info("额度冻结申请 - 额度ID: {}, 冻结类型: {}", request.getQuotaId(), request.getFreezeType());
        
        Object quota = getQuotaByIdAndType(request.getQuotaId(), request.getQuotaType());
        if (quota == null) {
            throw new BusinessException("200001", "额度不存在");
        }
        
        BigDecimal availableAmount = getAvailableAmount(quota);
        BigDecimal freezeAmount = request.getFreezeAmount();
        
        if ("FULL".equals(request.getFreezeType())) {
            freezeAmount = availableAmount;
        } else if ("PARTIAL".equals(request.getFreezeType())) {
            if (freezeAmount == null || MonetaryUtils.isNegative(freezeAmount)) {
                throw new BusinessException("100002", "部分冻结必须指定冻结金额");
            }
            if (MonetaryUtils.isGreaterThan(freezeAmount, availableAmount)) {
                throw new BusinessException("200002", "冻结金额不能超过可用额度");
            }
        }
        
        QuotaFreeze freeze = QuotaFreeze.builder()
            .freezeNo(generateFreezeNo())
            .quotaId(request.getQuotaId())
            .quotaType(QuotaType.valueOf(request.getQuotaType()))
            .freezeType(FreezeType.valueOf(request.getFreezeType()))
            .freezeAmount(freezeAmount)
            .reason(request.getReason())
            .condition(request.getCondition())
            .status(FreezeStatus.FROZEN)
            .operator(request.getOperator())
            .freezeTime(LocalDateTime.now())
            .build();
        
        quotaFreezeRepository.save(freeze);
        
        updateQuotaFrozenAmount(quota, freezeAmount, true);
        
        recordLifecycleEvent(
            request.getQuotaId(),
            QuotaType.valueOf(request.getQuotaType()),
            EventType.FREEZE,
            freezeAmount,
            "ACTIVE",
            "FROZEN",
            request.getReason(),
            request.getOperator(),
            freeze.getFreezeNo()
        );
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
            "QUOTA_FREEZE",
            request.getQuotaType(),
            request.getQuotaId().toString(),
            "额度冻结：" + request.getReason(),
            "SUCCESS"
        );
        
        return buildLifecycleResponse(request.getQuotaId(), request.getQuotaType());
    }
    
    @Override
    public QuotaLifecycleResponse unfreezeQuota(QuotaUnfreezeRequest request) {
        log.info("额度解冻申请 - 冻结记录ID: {}", request.getFreezeId());
        
        QuotaFreeze freeze = quotaFreezeRepository.findById(request.getFreezeId())
            .orElseThrow(() -> new BusinessException("200001", "冻结记录不存在"));
        
        if (freeze.getStatus() != FreezeStatus.FROZEN) {
            throw new BusinessException("200003", "冻结记录状态不正确");
        }
        
        BigDecimal unfreezeAmount = request.getUnfreezeAmount();
        if (unfreezeAmount == null) {
            unfreezeAmount = freeze.getFreezeAmount();
        } else if (MonetaryUtils.isGreaterThan(unfreezeAmount, freeze.getFreezeAmount())) {
            throw new BusinessException("200002", "解冻金额不能超过冻结金额");
        }
        
        freeze.setUnfreezeAmount(unfreezeAmount);
        freeze.setUnfreezeReason(request.getReason());
        freeze.setUnfreezeOperator(request.getOperator());
        freeze.setUnfreezeTime(LocalDateTime.now());
        
        if (MonetaryUtils.isGreaterThanOrEqual(unfreezeAmount, freeze.getFreezeAmount())) {
            freeze.setStatus(FreezeStatus.UNFROZEN);
        }
        
        quotaFreezeRepository.save(freeze);
        
        Object quota = getQuotaByIdAndType(freeze.getQuotaId(), freeze.getQuotaType().name());
        if (quota != null) {
            updateQuotaFrozenAmount(quota, unfreezeAmount, false);
        }
        
        recordLifecycleEvent(
            freeze.getQuotaId(),
            freeze.getQuotaType(),
            EventType.UNFREEZE,
            unfreezeAmount,
            "FROZEN",
            "ACTIVE",
            request.getReason(),
            request.getOperator(),
            freeze.getFreezeNo()
        );
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
            "QUOTA_UNFREEZE",
            freeze.getQuotaType().name(),
            freeze.getQuotaId().toString(),
            "额度解冻：" + request.getReason(),
            "SUCCESS"
        );
        
        return buildLifecycleResponse(freeze.getQuotaId(), freeze.getQuotaType().name());
    }
    
    @Override
    public QuotaLifecycleResponse terminateQuota(QuotaTerminateRequest request) {
        log.info("额度终止申请 - 额度ID: {}, 终止类型: {}", request.getQuotaId(), request.getTerminateType());
        
        Object quota = getQuotaByIdAndType(request.getQuotaId(), request.getQuotaType());
        if (quota == null) {
            throw new BusinessException("200001", "额度不存在");
        }
        
        QuotaTermination termination = QuotaTermination.builder()
            .terminationNo(generateTerminationNo())
            .quotaId(request.getQuotaId())
            .quotaType(QuotaTermination.QuotaType.valueOf(request.getQuotaType()))
            .terminateType(QuotaTermination.TerminateType.valueOf(request.getTerminateType()))
            .reason(request.getReason())
            .operator(request.getOperator())
            .terminateTime(LocalDateTime.now())
            .remark(request.getRemark())
            .build();
        
        quotaTerminationRepository.save(termination);
        
        updateQuotaStatus(quota, "TERMINATED");
        
        recordLifecycleEvent(
            request.getQuotaId(),
            QuotaType.valueOf(request.getQuotaType()),
            EventType.TERMINATE,
            null,
            "ACTIVE",
            "TERMINATED",
            request.getReason(),
            request.getOperator(),
            termination.getTerminationNo()
        );
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
            "QUOTA_TERMINATE",
            request.getQuotaType(),
            request.getQuotaId().toString(),
            "额度终止：" + request.getReason(),
            "SUCCESS"
        );
        
        return buildLifecycleResponse(request.getQuotaId(), request.getQuotaType());
    }
    
    @Override
    @Transactional(readOnly = true)
    public QuotaLifecycleResponse getQuotaLifecycle(Long quotaId, String quotaType) {
        return buildLifecycleResponse(quotaId, quotaType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QuotaLifecycleEvent> getQuotaLifecycleEvents(Long quotaId) {
        return quotaLifecycleEventRepository.findByQuotaIdOrderByEventTimeDesc(quotaId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QuotaAdjustment> getQuotaAdjustmentHistory(Long quotaId) {
        return quotaAdjustmentRepository.findByQuotaIdOrderByCreateTimeDesc(quotaId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QuotaFreeze> getQuotaFreezeHistory(Long quotaId) {
        return quotaFreezeRepository.findByQuotaIdOrderByFreezeTimeDesc(quotaId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QuotaFreeze> getActiveFreezes(Long quotaId) {
        return quotaFreezeRepository.findActiveFreezesByQuotaId(quotaId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalFrozenAmount(Long quotaId) {
        return quotaFreezeRepository.sumFreezeAmountByQuotaId(quotaId)
            .orElse(BigDecimal.ZERO);
    }
    
    private Object getQuotaByIdAndType(Long quotaId, String quotaType) {
        switch (quotaType) {
            case "GROUP":
                return groupQuotaRepository.findById(quotaId).orElse(null);
            case "CUSTOMER":
                return customerQuotaRepository.findById(quotaId).orElse(null);
            case "APPROVAL":
                return approvalQuotaRepository.findById(quotaId).orElse(null);
            default:
                return null;
        }
    }
    
    private BigDecimal getQuotaAmount(Object quota) {
        if (quota instanceof GroupQuota) {
            return ((GroupQuota) quota).getTotalQuota();
        } else if (quota instanceof CustomerQuota) {
            return ((CustomerQuota) quota).getTotalQuota();
        } else if (quota instanceof ApprovalQuota) {
            return ((ApprovalQuota) quota).getApprovalQuota();
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal getUsedAmount(Object quota) {
        if (quota instanceof GroupQuota) {
            return ((GroupQuota) quota).getUsedQuota();
        } else if (quota instanceof CustomerQuota) {
            return ((CustomerQuota) quota).getUsedQuota();
        } else if (quota instanceof ApprovalQuota) {
            return ((ApprovalQuota) quota).getUsedQuota();
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal getAvailableAmount(Object quota) {
        if (quota instanceof GroupQuota) {
            return ((GroupQuota) quota).getAvailableQuota();
        } else if (quota instanceof CustomerQuota) {
            return ((CustomerQuota) quota).getAvailableQuota();
        } else if (quota instanceof ApprovalQuota) {
            return ((ApprovalQuota) quota).getAvailableQuota();
        }
        return BigDecimal.ZERO;
    }
    
    private void updateQuotaAmount(Object quota, BigDecimal newAmount) {
        if (quota instanceof GroupQuota) {
            GroupQuota groupQuota = (GroupQuota) quota;
            BigDecimal used = groupQuota.getUsedQuota();
            groupQuota.setTotalQuota(newAmount);
            groupQuota.setAvailableQuota(MonetaryUtils.subtract(newAmount, used));
            groupQuotaRepository.save(groupQuota);
        } else if (quota instanceof CustomerQuota) {
            CustomerQuota customerQuota = (CustomerQuota) quota;
            BigDecimal used = customerQuota.getUsedQuota();
            customerQuota.setTotalQuota(newAmount);
            customerQuota.setAvailableQuota(MonetaryUtils.subtract(newAmount, used));
            customerQuotaRepository.save(customerQuota);
        } else if (quota instanceof ApprovalQuota) {
            ApprovalQuota approvalQuota = (ApprovalQuota) quota;
            BigDecimal used = approvalQuota.getUsedQuota();
            approvalQuota.setApprovalQuota(newAmount);
            approvalQuota.setAvailableQuota(MonetaryUtils.subtract(newAmount, used));
            approvalQuotaRepository.save(approvalQuota);
        }
    }
    
    private void updateQuotaFrozenAmount(Object quota, BigDecimal amount, boolean isFreeze) {
        if (quota instanceof GroupQuota) {
            GroupQuota groupQuota = (GroupQuota) quota;
            BigDecimal currentFrozen = groupQuota.getLockedQuota()!= null ?
                groupQuota.getLockedQuota() : BigDecimal.ZERO;
            BigDecimal newFrozen = isFreeze ? 
                MonetaryUtils.add(currentFrozen, amount) : 
                MonetaryUtils.subtract(currentFrozen, amount);
            groupQuota.setLockedQuota(newFrozen);
            groupQuota.setAvailableQuota(MonetaryUtils.subtract(
                MonetaryUtils.subtract(groupQuota.getTotalQuota(), groupQuota.getUsedQuota()),
                newFrozen
            ));
            groupQuotaRepository.save(groupQuota);
        } else if (quota instanceof CustomerQuota) {
            CustomerQuota customerQuota = (CustomerQuota) quota;
            BigDecimal currentFrozen = customerQuota.getLockedQuota() != null ? 
                customerQuota.getLockedQuota() : BigDecimal.ZERO;
            BigDecimal newFrozen = isFreeze ? 
                MonetaryUtils.add(currentFrozen, amount) : 
                MonetaryUtils.subtract(currentFrozen, amount);
            customerQuota.setLockedQuota(newFrozen);
            customerQuota.setAvailableQuota(MonetaryUtils.subtract(
                MonetaryUtils.subtract(customerQuota.getTotalQuota(), customerQuota.getUsedQuota()),
                newFrozen
            ));
            customerQuotaRepository.save(customerQuota);
        }
    }
    
    private void updateQuotaStatus(Object quota, String status) {
        if (quota instanceof GroupQuota) {
            GroupQuota groupQuota = (GroupQuota) quota;
            groupQuota.setStatus(QuotaStatus.valueOf(status));
            groupQuotaRepository.save(groupQuota);
        } else if (quota instanceof CustomerQuota) {
            CustomerQuota customerQuota = (CustomerQuota) quota;
            customerQuota.setStatus(QuotaStatus.valueOf(status));
            customerQuotaRepository.save(customerQuota);
        }
    }
    
    private void recordLifecycleEvent(Long quotaId, QuotaType quotaType,
                                      EventType eventType, BigDecimal amount, String beforeStatus,
                                      String afterStatus, String reason, String operator, String businessRefNo) {
        QuotaLifecycleEvent event = QuotaLifecycleEvent.builder()
            .eventId(generateEventId())
            .quotaId(quotaId)
            .quotaType(quotaType)
            .eventType(eventType)
            .amount(amount)
            .beforeStatus(beforeStatus)
            .afterStatus(afterStatus)
            .reason(reason)
            .operator(operator)
            .businessRefNo(businessRefNo)
            .eventTime(LocalDateTime.now())
            .build();
        
        quotaLifecycleEventRepository.save(event);
    }
    
    private QuotaLifecycleResponse buildLifecycleResponse(Long quotaId, String quotaType) {
        Object quota = getQuotaByIdAndType(quotaId, quotaType);
        if (quota == null) {
            return null;
        }
        
        QuotaLifecycleResponse response = new QuotaLifecycleResponse();
        response.setQuotaId(quotaId);
        response.setQuotaType(quotaType);
        
        if (quota instanceof GroupQuota) {
            GroupQuota gq = (GroupQuota) quota;
            response.setTotalQuota(gq.getTotalQuota());
            response.setUsedQuota(gq.getUsedQuota());
            response.setAvailableQuota(gq.getAvailableQuota());
            response.setFrozenQuota(gq.getLockedQuota());
            response.setStatus(gq.getStatus().name());
            response.setCreateTime(gq.getCreateTime());
            response.setUpdateTime(gq.getUpdateTime());
        } else if (quota instanceof CustomerQuota) {
            CustomerQuota cq = (CustomerQuota) quota;
            response.setTotalQuota(cq.getTotalQuota());
            response.setUsedQuota(cq.getUsedQuota());
            response.setAvailableQuota(cq.getAvailableQuota());
            response.setFrozenQuota(cq.getLockedQuota());
            response.setStatus(cq.getStatus().name());
            response.setEffectiveTime(cq.getEffectiveDate());
            response.setExpiryTime(cq.getExpiryDate());
            response.setCreateTime(cq.getCreateTime());
            response.setUpdateTime(cq.getUpdateTime());
        }
        
        List<QuotaLifecycleEvent> events = getQuotaLifecycleEvents(quotaId);
        response.setEvents(events.stream()
            .map(e -> {
                QuotaLifecycleResponse.QuotaLifecycleEventDTO dto = new QuotaLifecycleResponse.QuotaLifecycleEventDTO();
                dto.setEventId(e.getEventId());
                dto.setEventType(e.getEventType().name());
                dto.setAmount(e.getAmount());
                dto.setReason(e.getReason());
                dto.setOperator(e.getOperator());
                dto.setEventTime(e.getEventTime());
                return dto;
            })
            .collect(Collectors.toList()));
        
        return response;
    }
    
    private String generateAdjustmentNo() {
        return "ADJ" + LocalDateTime.now().format(DATE_FORMATTER) + 
            String.format("%06d", System.currentTimeMillis() % 1000000);
    }
    
    private String generateFreezeNo() {
        return "FRZ" + LocalDateTime.now().format(DATE_FORMATTER) + 
            String.format("%06d", System.currentTimeMillis() % 1000000);
    }
    
    private String generateTerminationNo() {
        return "TRM" + LocalDateTime.now().format(DATE_FORMATTER) + 
            String.format("%06d", System.currentTimeMillis() % 1000000);
    }
    
    @Override
    public QuotaLifecycleResponse createQuota(QuotaCreateRequest request) {
        log.info("额度创建申请 - 客户ID: {}, 申请额度: {}", 
            request.getCustomerId(), request.getApprovalQuota());
        
        // 根据额度类型创建相应的额度记录
        switch (request.getQuotaType()) {
            case "APPROVAL":
                return createApprovalQuota(request);
            case "CUSTOMER":
                return createCustomerQuota(request);
            case "GROUP":
                return createGroupQuota(request);
            default:
                throw new BusinessException("200001", "不支持的额度类型: " + request.getQuotaType());
        }
    }
    
    private QuotaLifecycleResponse createApprovalQuota(QuotaCreateRequest request) {
        // 创建批复额度记录
        ApprovalQuota approvalQuota = ApprovalQuota.builder()
            .customerQuotaSubId(request.getCustomerId())
            .approvalNo(request.getApprovalNo())
            .approvalType(request.getProductType() != null ? request.getProductType() : "DEFAULT")
            .approvalQuota(request.getApprovalQuota())
            .usedQuota(BigDecimal.ZERO)
            .availableQuota(request.getApprovalQuota())
            .status(QuotaStatus.ENABLED)
            .createBy(request.getApplicant() != null ? request.getApplicant() : "SYSTEM")
            .updateBy(request.getApplicant() != null ? request.getApplicant() : "SYSTEM")
            .build();
        
        approvalQuota = approvalQuotaRepository.save(approvalQuota);
        
        // 记录生命周期事件
        recordLifecycleEvent(
            approvalQuota.id,
            QuotaType.APPROVAL,
            EventType.CREATE,
            request.getApprovalQuota(),
            "INACTIVE",
            "ACTIVE",
            "额度创建",
            request.getApplicant() != null ? request.getApplicant() : "SYSTEM",
            approvalQuota.approvalNo
        );
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.APPROVAL_QUOTA,
            "QUOTA_CREATE",
            request.getQuotaType(),
            approvalQuota.id.toString(),
            "额度创建：" + request.getApprovalQuota(),
            "SUCCESS"
        );
        
        return buildLifecycleResponse(approvalQuota.id, request.getQuotaType());
    }
    
    private QuotaLifecycleResponse createCustomerQuota(QuotaCreateRequest request) {
        // 检查客户是否存在
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new BusinessException("200001", "客户不存在"));
        
        // 创建客户额度记录
        CustomerQuota customerQuota = CustomerQuota.builder()
            .customerId(request.getCustomerId())
            .customerName(customer.getCustomerName())
            .customerType(com.bank.quota.core.enums.CustomerType.valueOf(customer.getCustomerType().name()))
            .category(com.bank.quota.core.enums.CustomerCategory.valueOf(customer.getCategory().name()))
            .groupId(request.getGroupId())
            .totalQuota(request.getApprovalQuota())
            .usedQuota(BigDecimal.ZERO)
            .availableQuota(request.getApprovalQuota())
            .lockedQuota(BigDecimal.ZERO)
            .riskLevel(com.bank.quota.core.enums.RiskLevel.valueOf(customer.getRiskLevel().name()))
            .productType(request.getProductType())
            .status(QuotaStatus.ENABLED)
            .createBy(request.getApplicant() != null ? request.getApplicant() : "SYSTEM")
            .updateBy(request.getApplicant() != null ? request.getApplicant() : "SYSTEM")
            .build();
        
        customerQuota = customerQuotaRepository.save(customerQuota);
        
        // 记录生命周期事件
        recordLifecycleEvent(
            customerQuota.id,
            QuotaType.CUSTOMER,
            EventType.CREATE,
            request.getApprovalQuota(),
            "INACTIVE",
            "ACTIVE",
            "额度创建",
            request.getApplicant() != null ? request.getApplicant() : "SYSTEM",
            generateEventId()
        );
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.CUSTOMER_QUOTA,
            "QUOTA_CREATE",
            request.getQuotaType(),
            customerQuota.id.toString(),
            "额度创建：" + request.getApprovalQuota(),
            "SUCCESS"
        );
        
        return buildLifecycleResponse(customerQuota.id, request.getQuotaType());
    }
    
    private QuotaLifecycleResponse createGroupQuota(QuotaCreateRequest request) {
        // 创建集团额度记录
        GroupQuota groupQuota = GroupQuota.builder()
            .groupId(request.getGroupId())
            .groupName("Group-" + request.getGroupId())
            .totalQuota(request.getApprovalQuota())
            .usedQuota(BigDecimal.ZERO)
            .availableQuota(request.getApprovalQuota())
            .lockedQuota(BigDecimal.ZERO)
            .status(QuotaStatus.ENABLED)
            .createBy(request.getApplicant() != null ? request.getApplicant() : "SYSTEM")
            .updateBy(request.getApplicant() != null ? request.getApplicant() : "SYSTEM")
            .build();
        
        groupQuota = groupQuotaRepository.save(groupQuota);
        
        // 记录生命周期事件
        recordLifecycleEvent(
            groupQuota.getId(),
            QuotaType.GROUP,
            EventType.CREATE,
            request.getApprovalQuota(),
            "INACTIVE",
            "ACTIVE",
            "额度创建",
            request.getApplicant() != null ? request.getApplicant() : "SYSTEM",
            generateEventId()
        );
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.GROUP_QUOTA,
            "QUOTA_CREATE",
            request.getQuotaType(),
            groupQuota.getId().toString(),
            "额度创建：" + request.getApprovalQuota(),
            "SUCCESS"
        );
        
        return buildLifecycleResponse(groupQuota.getId(), request.getQuotaType());
    }
    
    private String generateApprovalQuotaId() {
        return "AQ" + LocalDateTime.now().format(DATE_FORMATTER) + 
            String.format("%06d", System.currentTimeMillis() % 1000000);
    }
    
    private String generateEventId() {
        return "EVT" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
