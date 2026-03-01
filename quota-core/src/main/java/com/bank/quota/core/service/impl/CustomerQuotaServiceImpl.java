package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.AuditLog;
import com.bank.quota.core.domain.CustomerQuota;
import com.bank.quota.core.dto.customerquota.*;
import com.bank.quota.core.enums.QuotaStatus;
import com.bank.quota.core.enums.CustomerType;
import com.bank.quota.core.repository.CustomerQuotaRepository;
import com.bank.quota.core.repository.CustomerQuotaSubRepository;
import com.bank.quota.core.repository.GroupQuotaRepository;
import com.bank.quota.core.service.AuditLogService;
import com.bank.quota.core.service.CustomerQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerQuotaServiceImpl implements CustomerQuotaService {
    
    private final CustomerQuotaRepository customerQuotaRepository;
    private final CustomerQuotaSubRepository customerQuotaSubRepository;
    private final GroupQuotaRepository groupQuotaRepository;
    private final AuditLogService auditLogService;
    
    @Override
    @Transactional
    public CustomerQuotaResponse createCustomerQuota(CreateCustomerQuotaRequest request) {
        log.info("Creating customer quota: customerId={}, customerName={}, totalQuota={}", 
                request.getCustomerId(), request.getCustomerName(), request.getTotalQuota());
        
        if (customerQuotaRepository.findByCustomerId(request.getCustomerId()).isPresent()) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                    "客户额度已存在: customerId=" + request.getCustomerId());
        }
        
        if (!groupQuotaRepository.findByGroupId(request.getGroupId()).isPresent()) {
            throw new BusinessException(ErrorCode.GROUP_NOT_FOUND.getCode(), 
                    "集团限额不存在: " + request.getGroupId());
        }
        
        CustomerQuota customerQuota = CustomerQuota.builder()
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .customerType(CustomerType.valueOf(request.getCustomerType()))
                .groupId(request.getGroupId())
                .totalQuota(request.getTotalQuota())
                .usedQuota(BigDecimal.ZERO)
                .availableQuota(request.getTotalQuota())
                .status(QuotaStatus.ENABLED)
                .description(request.getDescription())
                .createBy(request.getCreateBy())
                .build();
        
        CustomerQuota saved = customerQuotaRepository.save(customerQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.CUSTOMER_QUOTA,
                saved.getCustomerId().toString(),
                "创建客户额度: " + saved.getCustomerName(),
                request.getCreateBy(),
                "SUCCESS");
        
        log.info("Customer quota created successfully: customerId={}", saved.getCustomerId());
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public CustomerQuotaResponse updateCustomerQuota(Long customerId, UpdateCustomerQuotaRequest request) {
        log.info("Updating customer quota: customerId={}", customerId);
        
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerIdWithLock(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户额度不存在: customerId=" + customerId));
        
        if (request.getTotalQuota() != null) {
            BigDecimal oldTotal = customerQuota.getTotalQuota();
            BigDecimal diff = MonetaryUtils.subtract(request.getTotalQuota(), oldTotal);
            
            customerQuota.setTotalQuota(request.getTotalQuota());
            customerQuota.setAvailableQuota(MonetaryUtils.add(customerQuota.getAvailableQuota(), diff));
        }
        
        if (request.getDescription() != null) {
            customerQuota.setDescription(request.getDescription());
        }
        
        customerQuota.setUpdateBy(request.getUpdateBy());
        CustomerQuota saved = customerQuotaRepository.save(customerQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.CUSTOMER_QUOTA,
                customerId.toString(),
                "更新客户额度",
                request.getUpdateBy(),
                "SUCCESS");
        
        log.info("Customer quota updated successfully: customerId={}", customerId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public CustomerQuotaResponse freezeCustomerQuota(Long customerId, String reason, String operator) {
        log.info("Freezing customer quota: customerId={}, reason={}", customerId, reason);
        
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerIdWithLock(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户额度不存在: customerId=" + customerId));
        
        if (customerQuota.getStatus() == QuotaStatus.FROZEN) {
            throw new BusinessException(ErrorCode.QUOTA_FROZEN.getCode(), 
                    "客户额度已冻结");
        }
        
        customerQuota.setStatus(QuotaStatus.FROZEN);
        CustomerQuota saved = customerQuotaRepository.save(customerQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.CUSTOMER_QUOTA,
                customerId.toString(),
                "冻结客户额度: " + reason,
                operator,
                "SUCCESS");
        
        log.info("Customer quota frozen successfully: customerId={}", customerId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public CustomerQuotaResponse unfreezeCustomerQuota(Long customerId, String operator) {
        log.info("Unfreezing customer quota: customerId={}", customerId);
        
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerIdWithLock(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户额度不存在: customerId=" + customerId));
        
        if (customerQuota.getStatus() != QuotaStatus.FROZEN) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "客户额度未冻结");
        }
        
        customerQuota.setStatus(QuotaStatus.ENABLED);
        CustomerQuota saved = customerQuotaRepository.save(customerQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.CUSTOMER_QUOTA,
                customerId.toString(),
                "解冻客户额度",
                operator,
                "SUCCESS");
        
        log.info("Customer quota unfrozen successfully: customerId={}", customerId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public CustomerQuotaResponse disableCustomerQuota(Long customerId, String reason, String operator) {
        log.info("Disabling customer quota: customerId={}, reason={}", customerId, reason);
        
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerIdWithLock(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户额度不存在: customerId=" + customerId));
        
        if (customerQuota.getStatus() == QuotaStatus.DISABLED) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "客户额度已停用");
        }
        
        customerQuota.setStatus(QuotaStatus.DISABLED);
        CustomerQuota saved = customerQuotaRepository.save(customerQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.CUSTOMER_QUOTA,
                customerId.toString(),
                "停用客户额度: " + reason,
                operator,
                "SUCCESS");
        
        log.info("Customer quota disabled successfully: customerId={}", customerId);
        
        return buildResponse(saved);
    }
    
    @Override
    public CustomerQuotaResponse getCustomerQuota(Long customerId) {
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户额度不存在: customerId=" + customerId));
        
        return buildResponse(customerQuota);
    }
    
    @Override
    public List<CustomerQuotaResponse> getCustomerQuotasByGroupId(Long groupId) {
        return customerQuotaRepository.findByGroupId(groupId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CustomerQuotaResponse> getEnabledCustomerQuotas() {
        return customerQuotaRepository.findAllEnabled().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CustomerQuotaResponse> getCustomerQuotasByType(CustomerType customerType) {
        return customerQuotaRepository.findByCustomerType(customerType).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public CustomerQuotaQueryResponse queryCustomerQuotas(CustomerQuotaQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPageNum() != null ? request.getPageNum() : 0,
                request.getPageSize() != null ? request.getPageSize() : 10,
                Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<CustomerQuota> page = customerQuotaRepository.findAll(pageable);
        
        CustomerQuotaQueryResponse response = new CustomerQuotaQueryResponse();
        response.setTotal(page.getTotalElements());
        response.setPageNum(request.getPageNum());
        response.setPageSize(request.getPageSize());
        response.setPages(page.getTotalPages());
        response.setList(page.getContent().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList()));
        
        return response;
    }
    
    @Override
    public CustomerQuotaUsageResponse getCustomerQuotaUsage(Long customerId) {
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户额度不存在: customerId=" + customerId));
        
        CustomerQuotaUsageResponse response = new CustomerQuotaUsageResponse();
        response.setCustomerId(customerQuota.getCustomerId());
        response.setCustomerName(customerQuota.getCustomerName());
        response.setCustomerType(customerQuota.getCustomerType() != null ? 
                customerQuota.getCustomerType().name() : null);
        response.setGroupId(customerQuota.getGroupId());
        response.setTotalQuota(customerQuota.getTotalQuota());
        response.setUsedQuota(customerQuota.getUsedQuota());
        response.setAvailableQuota(customerQuota.getAvailableQuota());
        response.setUsageRate(MonetaryUtils.usageRate(customerQuota.getUsedQuota(), 
                customerQuota.getTotalQuota()));
        
        return response;
    }
    
    @Override
    @Transactional
    public void adjustCustomerQuota(Long customerId, BigDecimal adjustmentAmount, String reason, String operator) {
        log.info("Adjusting customer quota: customerId={}, adjustmentAmount={}, reason={}", 
                customerId, adjustmentAmount, reason);
        
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerIdWithLock(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户额度不存在: customerId=" + customerId));
        
        if (MonetaryUtils.isNegative(adjustmentAmount)) {
            if (MonetaryUtils.isLessThan(customerQuota.getAvailableQuota(), 
                    MonetaryUtils.abs(adjustmentAmount))) {
                throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(), 
                        "调整金额超出可用额度");
            }
        }
        
        customerQuota.setTotalQuota(MonetaryUtils.add(customerQuota.getTotalQuota(), adjustmentAmount));
        customerQuota.setAvailableQuota(MonetaryUtils.add(customerQuota.getAvailableQuota(), adjustmentAmount));
        customerQuota.setUpdateTime(LocalDateTime.now());
        customerQuota.setUpdateBy(operator);
        
        customerQuotaRepository.save(customerQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.CUSTOMER_QUOTA,
                customerId.toString(),
                "调整客户额度: " + reason + ", 调整金额: " + adjustmentAmount,
                operator,
                "SUCCESS");
        
        log.info("Customer quota adjusted successfully: customerId={}, newTotalQuota={}", 
                customerId, customerQuota.getTotalQuota());
    }
    
    @Override
    @Transactional
    public void transferQuota(Long fromCustomerId, Long toCustomerId, BigDecimal amount, 
                              String reason, String operator) {
        log.info("Transferring quota: fromCustomerId={}, toCustomerId={}, amount={}", 
                fromCustomerId, toCustomerId, amount);
        
        CustomerQuota fromQuota = customerQuotaRepository.findByCustomerIdWithLock(fromCustomerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "源客户额度不存在: fromCustomerId=" + fromCustomerId));
        
        CustomerQuota toQuota = customerQuotaRepository.findByCustomerIdWithLock(toCustomerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "目标客户额度不存在: toCustomerId=" + toCustomerId));
        
        if (MonetaryUtils.isLessThan(fromQuota.getAvailableQuota(), amount)) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(), 
                    "源客户可用额度不足");
        }
        
        fromQuota.setAvailableQuota(MonetaryUtils.subtract(fromQuota.getAvailableQuota(), amount));
        fromQuota.setUpdateTime(LocalDateTime.now());
        fromQuota.setUpdateBy(operator);
        customerQuotaRepository.save(fromQuota);
        
        toQuota.setAvailableQuota(MonetaryUtils.add(toQuota.getAvailableQuota(), amount));
        toQuota.setUpdateTime(LocalDateTime.now());
        toQuota.setUpdateBy(operator);
        customerQuotaRepository.save(toQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.CUSTOMER_QUOTA,
                fromCustomerId.toString(),
                "额度转移至客户" + toCustomerId + ": " + reason + ", 金额: " + amount,
                operator,
                "SUCCESS");
        
        log.info("Quota transferred successfully: fromCustomerId={}, toCustomerId={}, amount={}", 
                fromCustomerId, toCustomerId, amount);
    }
    
    private CustomerQuotaResponse buildResponse(CustomerQuota customerQuota) {
        CustomerQuotaResponse response = new CustomerQuotaResponse();
        response.setId(customerQuota.getId());
        response.setCustomerId(customerQuota.getCustomerId());
        response.setCustomerName(customerQuota.getCustomerName());
        response.setCustomerType(customerQuota.getCustomerType() != null ? 
                customerQuota.getCustomerType().name() : null);
        response.setGroupId(customerQuota.getGroupId());
        response.setTotalQuota(customerQuota.getTotalQuota());
        response.setUsedQuota(customerQuota.getUsedQuota());
        response.setAvailableQuota(customerQuota.getAvailableQuota());
        response.setStatus(customerQuota.getStatus() != null ? customerQuota.getStatus().name() : null);
        response.setDescription(customerQuota.getDescription());
        response.setCreateBy(customerQuota.getCreateBy());
        response.setUpdateBy(customerQuota.getUpdateBy());
        response.setCreateTime(customerQuota.getCreateTime());
        response.setUpdateTime(customerQuota.getUpdateTime());
        response.setUsageRate(MonetaryUtils.usageRate(customerQuota.getUsedQuota(), 
                customerQuota.getTotalQuota()));
        return response;
    }
}
