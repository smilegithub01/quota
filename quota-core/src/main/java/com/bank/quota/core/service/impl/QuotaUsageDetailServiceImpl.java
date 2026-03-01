package com.bank.quota.core.service.impl;

import com.bank.quota.core.domain.QuotaUsageDetail;
import com.bank.quota.core.dto.quota.*;
import com.bank.quota.core.enums.QuotaUsageType;
import com.bank.quota.core.repository.QuotaUsageDetailRepository;
import com.bank.quota.core.service.QuotaUsageDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 额度使用明细服务实现类
 * 
 * <p>提供银行级信贷额度管控平台的额度使用明细功能实现。
 * 包括额度使用明细的查询、统计等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaUsageDetailServiceImpl implements QuotaUsageDetailService {
    
    private final QuotaUsageDetailRepository quotaUsageDetailRepository;
    
    @Override
    public List<QuotaUsageDetailResponse> queryUsageDetails(QuotaUsageDetailQueryRequest request) {
        log.debug("Querying quota usage details: customerId={}, businessType={}", 
                request.getCustomerId(), request.getBusinessType());
        
        // 构建查询条件
        List<QuotaUsageDetail> details = quotaUsageDetailRepository.findByConditions(
                request.getCustomerId(),
                request.getGroupId(),
                request.getBusinessType(),
                request.getUsageType(),
                request.getRelatedId(),
                request.getRelatedType(),
                request.getOperatorId(),
                request.getStartDate(),
                request.getEndDate()
        );
        
        return details.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QuotaUsageDetailResponse> queryUsageDetailsWithPaging(QuotaUsageDetailQueryRequest request) {
        log.debug("Querying quota usage details with paging: customerId={}, pageNum={}, pageSize={}", 
                request.getCustomerId(), request.getPageNum(), request.getPageSize());
        
        // 计算偏移量
        int offset = (request.getPageNum() - 1) * request.getPageSize();
        
        // 构建查询条件
        List<QuotaUsageDetail> details = quotaUsageDetailRepository.findByConditionsWithPaging(
                request.getCustomerId(),
                request.getGroupId(),
                request.getBusinessType(),
                request.getUsageType(),
                request.getRelatedId(),
                request.getRelatedType(),
                request.getOperatorId(),
                request.getStartDate(),
                request.getEndDate(),
                offset,
                request.getPageSize()
        );
        
        return details.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Long getTotalUsageCount(QuotaUsageDetailQueryRequest request) {
        log.debug("Getting total quota usage count: customerId={}", request.getCustomerId());
        
        return quotaUsageDetailRepository.countByConditions(
                request.getCustomerId(),
                request.getGroupId(),
                request.getBusinessType(),
                request.getUsageType(),
                request.getRelatedId(),
                request.getRelatedType(),
                request.getOperatorId(),
                request.getStartDate(),
                request.getEndDate()
        );
    }
    
    @Override
    public QuotaUsageStatisticsResponse getUsageStatistics(QuotaUsageDetailQueryRequest request) {
        log.debug("Getting quota usage statistics: startDate={}, endDate={}", 
                request.getStartDate(), request.getEndDate());
        
        // 获取使用类型统计
        List<QuotaUsageStatisticsResponse.UsageTypeSummary> usageTypeSummaries = 
                quotaUsageDetailRepository.getUsageTypeStatistics(
                        request.getCustomerId(),
                        request.getGroupId(),
                        request.getBusinessType(),
                        request.getStartDate(),
                        request.getEndDate()
                ).stream()
                .map(data -> QuotaUsageStatisticsResponse.UsageTypeSummary.builder()
                        .usageType((String) data[0])
                        .count(((Number) data[1]).longValue())
                        .amount((BigDecimal) data[2])
                        .build())
                .collect(Collectors.toList());
        
        // 获取客户统计
        List<QuotaUsageStatisticsResponse.CustomerUsageSummary> customerSummaries = 
                quotaUsageDetailRepository.getCustomerStatistics(
                        request.getCustomerId(),
                        request.getGroupId(),
                        request.getBusinessType(),
                        request.getStartDate(),
                        request.getEndDate()
                ).stream()
                .map(data -> QuotaUsageStatisticsResponse.CustomerUsageSummary.builder()
                        .customerId(((Number) data[0]).longValue())
                        .customerName((String) data[1])
                        .count(((Number) data[2]).longValue())
                        .amount((BigDecimal) data[3])
                        .build())
                .collect(Collectors.toList());
        
        // 获取基础统计数据
        Long totalCount = getTotalUsageCount(request);
        BigDecimal totalUsageAmount = getTotalUsageAmount(request);
        BigDecimal totalOccupyAmount = getTotalByUsageType(request, QuotaUsageType.OCCUPY);
        BigDecimal totalReleaseAmount = getTotalByUsageType(request, QuotaUsageType.RELEASE);
        BigDecimal totalAdjustAmount = getTotalByUsageType(request, QuotaUsageType.ADJUST);
        
        return QuotaUsageStatisticsResponse.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalUsageCount(totalCount)
                .totalUsageAmount(totalUsageAmount)
                .totalOccupyAmount(totalOccupyAmount)
                .totalReleaseAmount(totalReleaseAmount)
                .totalAdjustAmount(totalAdjustAmount)
                .usageTypeSummaries(usageTypeSummaries)
                .customerSummaries(customerSummaries)
                .build();
    }
    
    /**
     * 获取总使用金额
     */
    private BigDecimal getTotalUsageAmount(QuotaUsageDetailQueryRequest request) {
        BigDecimal result = quotaUsageDetailRepository.sumUsageAmountByConditions(
                request.getCustomerId(),
                request.getGroupId(),
                request.getBusinessType(),
                null,
                request.getRelatedId(),
                request.getRelatedType(),
                request.getOperatorId(),
                request.getStartDate(),
                request.getEndDate()
        );
        return result != null ? result : BigDecimal.ZERO;
    }
    
    /**
     * 根据使用类型获取总金额
     */
    private BigDecimal getTotalByUsageType(QuotaUsageDetailQueryRequest request, QuotaUsageType usageType) {
        BigDecimal result = quotaUsageDetailRepository.sumUsageAmountByConditions(
                request.getCustomerId(),
                request.getGroupId(),
                request.getBusinessType(),
                usageType,
                request.getRelatedId(),
                request.getRelatedType(),
                request.getOperatorId(),
                request.getStartDate(),
                request.getEndDate()
        );
        return result != null ? result : BigDecimal.ZERO;
    }
    
    @Override
    public List<QuotaUsageDetailResponse> getUsageDetailsByCustomer(Long customerId, Integer pageNum, Integer pageSize) {
        log.debug("Getting quota usage details by customer: customerId={}, pageNum={}, pageSize={}", 
                customerId, pageNum, pageSize);
        
        int offset = (pageNum - 1) * pageSize;
        
        List<QuotaUsageDetail> details = quotaUsageDetailRepository.findByCustomerIdWithPaging(customerId, offset, pageSize);
        
        return details.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QuotaUsageDetailResponse> getUsageDetailsByGroup(Long groupId, Integer pageNum, Integer pageSize) {
        log.debug("Getting quota usage details by group: groupId={}, pageNum={}, pageSize={}", 
                groupId, pageNum, pageSize);
        
        int offset = (pageNum - 1) * pageSize;
        
        List<QuotaUsageDetail> details = quotaUsageDetailRepository.findByGroupIdWithPaging(groupId, offset, pageSize);
        
        return details.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QuotaUsageDetailResponse> getUsageDetailsByRelatedId(String relatedId, String relatedType) {
        log.debug("Getting quota usage details by related id: relatedId={}, relatedType={}", 
                relatedId, relatedType);
        
        List<QuotaUsageDetail> details = quotaUsageDetailRepository.findByRelatedIdAndType(relatedId, relatedType);
        
        return details.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public QuotaUsageDetailResponse createUsageDetail(QuotaUsageDetail detail) {
        log.info("Creating quota usage detail: usageType={}, usageAmount={}", 
                detail.getUsageType(), detail.getUsageAmount());
        
        QuotaUsageDetail saved = quotaUsageDetailRepository.save(detail);
        
        log.info("Quota usage detail created successfully: usageNo={}", saved.getUsageNo());
        
        return buildResponse(saved);
    }
    
    /**
     * 构建响应对象
     */
    private QuotaUsageDetailResponse buildResponse(QuotaUsageDetail detail) {
        return QuotaUsageDetailResponse.builder()
                .id(detail.getId())
                .usageNo(detail.getUsageNo())
                .customerId(detail.getCustomerId())
                .customerName(detail.getCustomerName())
                .groupId(detail.getGroupId())
                .groupName(detail.getGroupName())
                .businessType(detail.getBusinessType())
                .usageType(detail.getUsageType())
                .usageAmount(detail.getUsageAmount())
                .currency(detail.getCurrency())
                .originalBalance(detail.getOriginalBalance())
                .currentBalance(detail.getCurrentBalance())
                .balanceAfter(detail.getBalanceAfter())
                .relatedId(detail.getRelatedId())
                .relatedType(detail.getRelatedType())
                .usageDate(detail.getUsageDate())
                .description(detail.getDescription())
                .operatorId(detail.getOperatorId())
                .operatorName(detail.getOperatorName())
                .createTime(detail.getCreateTime())
                .updateTime(detail.getUpdateTime())
                .build();
    }
}