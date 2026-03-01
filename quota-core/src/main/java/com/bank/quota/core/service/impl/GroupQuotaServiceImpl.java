package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.AuditLog;
import com.bank.quota.core.domain.GroupQuota;
import com.bank.quota.core.domain.GroupQuotaSub;
import com.bank.quota.core.dto.groupquota.*;
import com.bank.quota.core.enums.QuotaStatus;
import com.bank.quota.core.repository.GroupQuotaRepository;
import com.bank.quota.core.repository.GroupQuotaSubRepository;
import com.bank.quota.core.service.AuditLogService;
import com.bank.quota.core.service.GroupQuotaService;
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
public class GroupQuotaServiceImpl implements GroupQuotaService {
    
    private final GroupQuotaRepository groupQuotaRepository;
    private final GroupQuotaSubRepository groupQuotaSubRepository;
    private final AuditLogService auditLogService;
    
    @Override
    @Transactional
    public GroupQuotaResponse createGroupQuota(CreateGroupQuotaRequest request) {
        log.info("Creating group quota: groupId={}, groupName={}, totalQuota={}", 
                request.getGroupId(), request.getGroupName(), request.getTotalQuota());
        
        if (groupQuotaRepository.findByGroupId(request.getGroupId()).isPresent()) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                    "集团限额已存在: groupId=" + request.getGroupId());
        }
        
        GroupQuota groupQuota = GroupQuota.builder()
                .groupId(request.getGroupId())
                .groupName(request.getGroupName())
                .totalQuota(request.getTotalQuota())
                .usedQuota(BigDecimal.ZERO)
                .availableQuota(request.getTotalQuota())
                .status(QuotaStatus.ENABLED)
                .createBy(request.getCreateBy())
                .build();
        
        GroupQuota saved = groupQuotaRepository.save(groupQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_CREATE,
                AuditLog.AuditObjectType.GROUP_QUOTA,
                saved.getGroupId().toString(),
                "创建集团限额: " + saved.getGroupName(),
                request.getCreateBy(),
                "SUCCESS");
        
        log.info("Group quota created successfully: groupId={}", saved.getGroupId());
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public GroupQuotaResponse updateGroupQuota(Long groupId, UpdateGroupQuotaRequest request) {
        log.info("Updating group quota: groupId={}", groupId);
        
        GroupQuota groupQuota = groupQuotaRepository.findByGroupIdWithLock(groupId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团限额不存在: groupId=" + groupId));
        
        if (request.getTotalQuota() != null) {
            BigDecimal oldTotal = groupQuota.getTotalQuota();
            BigDecimal diff = MonetaryUtils.subtract(request.getTotalQuota(), oldTotal);
            
            groupQuota.setTotalQuota(request.getTotalQuota());
            groupQuota.setAvailableQuota(MonetaryUtils.add(groupQuota.getAvailableQuota(), diff));
        }
        
        if (request.getDescription() != null) {
            log.debug("Description update requested but GroupQuota entity does not have description field");
        }
        
        groupQuota.setUpdateBy(request.getUpdateBy());
        GroupQuota saved = groupQuotaRepository.save(groupQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.GROUP_QUOTA,
                groupId.toString(),
                "更新集团限额",
                request.getUpdateBy(),
                "SUCCESS");
        
        log.info("Group quota updated successfully: groupId={}", groupId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public GroupQuotaResponse freezeGroupQuota(Long groupId, String reason, String operator) {
        log.info("Freezing group quota: groupId={}, reason={}", groupId, reason);
        
        GroupQuota groupQuota = groupQuotaRepository.findByGroupIdWithLock(groupId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团限额不存在: groupId=" + groupId));
        
        if (groupQuota.getStatus() == QuotaStatus.FROZEN) {
            throw new BusinessException(ErrorCode.QUOTA_FROZEN.getCode(), 
                    "集团限额已冻结");
        }
        
        groupQuota.setStatus(QuotaStatus.FROZEN);
        GroupQuota saved = groupQuotaRepository.save(groupQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.GROUP_QUOTA,
                groupId.toString(),
                "冻结集团限额: " + reason,
                operator,
                "SUCCESS");
        
        log.info("Group quota frozen successfully: groupId={}", groupId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public GroupQuotaResponse unfreezeGroupQuota(Long groupId, String operator) {
        log.info("Unfreezing group quota: groupId={}", groupId);
        
        GroupQuota groupQuota = groupQuotaRepository.findByGroupIdWithLock(groupId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团限额不存在: groupId=" + groupId));
        
        if (groupQuota.getStatus() != QuotaStatus.FROZEN) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "集团限额未冻结");
        }
        
        groupQuota.setStatus(QuotaStatus.ENABLED);
        GroupQuota saved = groupQuotaRepository.save(groupQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.GROUP_QUOTA,
                groupId.toString(),
                "解冻集团限额",
                operator,
                "SUCCESS");
        
        log.info("Group quota unfrozen successfully: groupId={}", groupId);
        
        return buildResponse(saved);
    }
    
    @Override
    @Transactional
    public GroupQuotaResponse disableGroupQuota(Long groupId, String reason, String operator) {
        log.info("Disabling group quota: groupId={}, reason={}", groupId, reason);
        
        GroupQuota groupQuota = groupQuotaRepository.findByGroupIdWithLock(groupId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团限额不存在: groupId=" + groupId));
        
        if (groupQuota.getStatus() == QuotaStatus.DISABLED) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "集团限额已停用");
        }
        
        groupQuota.setStatus(QuotaStatus.DISABLED);
        GroupQuota saved = groupQuotaRepository.save(groupQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.GROUP_QUOTA,
                groupId.toString(),
                "停用集团限额: " + reason,
                operator,
                "SUCCESS");
        
        log.info("Group quota disabled successfully: groupId={}", groupId);
        
        return buildResponse(saved);
    }
    
    @Override
    public GroupQuotaResponse getGroupQuota(Long groupId) {
        GroupQuota groupQuota = groupQuotaRepository.findByGroupId(groupId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团限额不存在: groupId=" + groupId));
        
        return buildResponse(groupQuota);
    }
    
    @Override
    public List<GroupQuotaResponse> getAllGroupQuotas() {
        return groupQuotaRepository.findAll().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GroupQuotaResponse> getEnabledGroupQuotas() {
        return groupQuotaRepository.findAllEnabled().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public GroupQuotaQueryResponse queryGroupQuotas(GroupQuotaQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPageNum() != null ? request.getPageNum() : 0,
                request.getPageSize() != null ? request.getPageSize() : 10,
                Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<GroupQuota> page = groupQuotaRepository.findAll(pageable);
        
        GroupQuotaQueryResponse response = new GroupQuotaQueryResponse();
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
    @Transactional
    public GroupQuotaSubResponse createGroupQuotaSub(Long groupQuotaId, CreateGroupQuotaSubRequest request) {
        log.info("Creating group quota sub: groupQuotaId={}, subType={}", 
                groupQuotaId, request.getSubType());
        
        GroupQuota groupQuota = groupQuotaRepository.findByGroupIdWithLock(groupQuotaId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团限额不存在: groupQuotaId=" + groupQuotaId));
        
        if (groupQuotaSubRepository.findByGroupQuotaIdAndSubType(groupQuotaId, request.getSubType())
                .isPresent()) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                    "子限额类型已存在: subType=" + request.getSubType());
        }
        
        GroupQuotaSub subQuota = GroupQuotaSub.builder()
                .groupQuotaId(groupQuotaId)
                .subType(request.getSubType())
                .subTypeName(request.getSubTypeName())
                .subQuota(request.getSubQuota())
                .usedQuota(BigDecimal.ZERO)
                .availableQuota(request.getSubQuota())
                .status(GroupQuotaSub.QuotaStatus.ENABLED)
                .createBy(request.getCreateBy())
                .build();
        
        GroupQuotaSub saved = groupQuotaSubRepository.save(subQuota);
        
        log.info("Group quota sub created successfully: id={}", saved.getId());
        
        return buildSubResponse(saved);
    }
    
    @Override
    @Transactional
    public GroupQuotaSubResponse updateGroupQuotaSub(Long subId, UpdateGroupQuotaSubRequest request) {
        log.info("Updating group quota sub: subId={}", subId);
        
        GroupQuotaSub subQuota = groupQuotaSubRepository.findByIdWithLock(subId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.QUOTA_NOT_FOUND.getCode(), 
                        "子限额不存在: subId=" + subId));
        
        if (request.getSubQuota() != null) {
            BigDecimal oldQuota = subQuota.getSubQuota();
            BigDecimal diff = MonetaryUtils.subtract(request.getSubQuota(), oldQuota);
            
            subQuota.setSubQuota(request.getSubQuota());
            subQuota.setAvailableQuota(MonetaryUtils.add(subQuota.getAvailableQuota(), diff));
        }
        
        if (request.getSubTypeName() != null) {
            subQuota.setSubTypeName(request.getSubTypeName());
        }
        
        subQuota.setUpdateBy(request.getUpdateBy());
        GroupQuotaSub saved = groupQuotaSubRepository.save(subQuota);
        
        log.info("Group quota sub updated successfully: subId={}", subId);
        
        return buildSubResponse(saved);
    }
    
    @Override
    public List<GroupQuotaSubResponse> getGroupQuotaSubs(Long groupQuotaId) {
        return groupQuotaSubRepository.findByGroupQuotaId(groupQuotaId).stream()
                .map(this::buildSubResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public GroupQuotaUsageResponse getGroupQuotaUsage(Long groupId) {
        GroupQuota groupQuota = groupQuotaRepository.findByGroupId(groupId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团限额不存在: groupId=" + groupId));
        
        GroupQuotaUsageResponse response = new GroupQuotaUsageResponse();
        response.setGroupId(groupQuota.getGroupId());
        response.setGroupName(groupQuota.getGroupName());
        response.setTotalQuota(groupQuota.getTotalQuota());
        response.setUsedQuota(groupQuota.getUsedQuota());
        response.setAvailableQuota(groupQuota.getAvailableQuota());
        response.setUsageRate(MonetaryUtils.usageRate(groupQuota.getUsedQuota(), groupQuota.getTotalQuota()));
        
        List<GroupQuotaSub> subQuotas = groupQuotaSubRepository.findByGroupQuotaId(groupQuota.getId());
        List<GroupQuotaUsageResponse.SubQuotaUsage> subUsages = subQuotas.stream()
                .map(sub -> {
                    GroupQuotaUsageResponse.SubQuotaUsage subUsage = new GroupQuotaUsageResponse.SubQuotaUsage();
                    subUsage.setSubType(sub.getSubType());
                    subUsage.setSubTypeName(sub.getSubTypeName());
                    subUsage.setSubQuota(sub.getSubQuota());
                    subUsage.setUsedQuota(sub.getUsedQuota());
                    subUsage.setAvailableQuota(sub.getAvailableQuota());
                    subUsage.setUsageRate(MonetaryUtils.usageRate(sub.getUsedQuota(), sub.getSubQuota()));
                    return subUsage;
                })
                .collect(Collectors.toList());
        
        response.setSubQuotaUsages(subUsages);
        
        return response;
    }
    
    @Override
    @Transactional
    public void adjustGroupQuota(Long groupId, BigDecimal adjustmentAmount, String reason, String operator) {
        log.info("Adjusting group quota: groupId={}, adjustmentAmount={}, reason={}", 
                groupId, adjustmentAmount, reason);
        
        GroupQuota groupQuota = groupQuotaRepository.findByGroupIdWithLock(groupId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团限额不存在: groupId=" + groupId));
        
        if (MonetaryUtils.isNegative(adjustmentAmount)) {
            if (MonetaryUtils.isLessThan(groupQuota.getAvailableQuota(), 
                    MonetaryUtils.abs(adjustmentAmount))) {
                throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(), 
                        "调整金额超出可用额度");
            }
        }
        
        groupQuota.setTotalQuota(MonetaryUtils.add(groupQuota.getTotalQuota(), adjustmentAmount));
        groupQuota.setAvailableQuota(MonetaryUtils.add(groupQuota.getAvailableQuota(), adjustmentAmount));
        groupQuota.setUpdateTime(LocalDateTime.now());
        groupQuota.setUpdateBy(operator);
        
        groupQuotaRepository.save(groupQuota);
        
        auditLogService.logOperation(
                AuditLog.OperationType.QUOTA_UPDATE,
                AuditLog.AuditObjectType.GROUP_QUOTA,
                groupId.toString(),
                "调整集团限额: " + reason + ", 调整金额: " + adjustmentAmount,
                operator,
                "SUCCESS");
        
        log.info("Group quota adjusted successfully: groupId={}, newTotalQuota={}", 
                groupId, groupQuota.getTotalQuota());
    }
    
    private GroupQuotaResponse buildResponse(GroupQuota groupQuota) {
        GroupQuotaResponse response = new GroupQuotaResponse();
        response.setId(groupQuota.getId());
        response.setGroupId(groupQuota.getGroupId());
        response.setGroupName(groupQuota.getGroupName());
        response.setTotalQuota(groupQuota.getTotalQuota());
        response.setUsedQuota(groupQuota.getUsedQuota());
        response.setAvailableQuota(groupQuota.getAvailableQuota());
        response.setStatus(groupQuota.getStatus() != null ? groupQuota.getStatus().name() : null);
        response.setCreateBy(groupQuota.getCreateBy());
        response.setUpdateBy(groupQuota.getUpdateBy());
        response.setCreateTime(groupQuota.getCreateTime());
        response.setUpdateTime(groupQuota.getUpdateTime());
        response.setUsageRate(MonetaryUtils.usageRate(groupQuota.getUsedQuota(), groupQuota.getTotalQuota()));
        return response;
    }
    
    private GroupQuotaSubResponse buildSubResponse(GroupQuotaSub subQuota) {
        GroupQuotaSubResponse response = new GroupQuotaSubResponse();
        response.setId(subQuota.getId());
        response.setGroupQuotaId(subQuota.getGroupQuotaId());
        response.setSubType(subQuota.getSubType());
        response.setSubTypeName(subQuota.getSubTypeName());
        response.setSubQuota(subQuota.getSubQuota());
        response.setUsedQuota(subQuota.getUsedQuota());
        response.setAvailableQuota(subQuota.getAvailableQuota());
        response.setStatus(subQuota.getStatus() != null ? subQuota.getStatus().name() : null);
        response.setCreateBy(subQuota.getCreateBy());
        response.setUpdateBy(subQuota.getUpdateBy());
        response.setCreateTime(subQuota.getCreateTime());
        response.setUpdateTime(subQuota.getUpdateTime());
        response.setUsageRate(MonetaryUtils.usageRate(subQuota.getUsedQuota(), subQuota.getSubQuota()));
        return response;
    }
}
