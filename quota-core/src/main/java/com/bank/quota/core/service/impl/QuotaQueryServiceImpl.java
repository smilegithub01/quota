package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.core.domain.*;
import com.bank.quota.core.dto.quotaquery.QuotaQueryRequest;
import com.bank.quota.core.dto.quotaquery.QuotaQueryResponse;
import com.bank.quota.core.enums.QuotaType;
import com.bank.quota.core.repository.*;
import com.bank.quota.core.service.QuotaQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 额度查询服务实现
 * 
 * <p>提供统一的额度查询功能，支持集团额度、客户额度、批复额度等多层级查询。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuotaQueryServiceImpl implements QuotaQueryService {
    
    private final GroupQuotaRepository groupQuotaRepository;
    private final CustomerQuotaRepository customerQuotaRepository;
    private final ApprovalQuotaRepository approvalQuotaRepository;
    private final CustomerRepository customerRepository;
    private final GroupQuotaSubRepository groupQuotaSubRepository;
    private final CustomerQuotaSubRepository customerQuotaSubRepository;
    private final ContractOccupancyRepository contractOccupancyRepository;
    private final QuotaUsageDetailRepository quotaUsageDetailRepository;
    
    @Override
    public QuotaQueryResponse queryGroupQuota(Long groupId) {
        log.info("查询集团额度 - 集团ID: {}", groupId);
        
        GroupQuota groupQuota = groupQuotaRepository.findById(groupId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "集团额度不存在"));
        
        QuotaQueryResponse response = convertToQuotaQueryResponse(groupQuota, "GROUP");
        
        // 查询子额度
        List<GroupQuotaSub> subQuotas = groupQuotaSubRepository.findByGroupQuotaId(groupId);
        if (!subQuotas.isEmpty()) {
            response.setSubQuotas(subQuotas.stream()
                .map(sub -> convertToQuotaQueryResponse(sub, "GROUP_SUB"))
                .collect(Collectors.toList()));
        }
        
        response.calculateUsageRate();
        return response;
    }
    
    @Override
    public QuotaQueryResponse queryCustomerQuota(Long customerId) {
        log.info("查询客户额度 - 客户ID: {}", customerId);
        
        CustomerQuota customerQuota = customerQuotaRepository.findById(customerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "客户额度不存在"));
        
        QuotaQueryResponse response = convertToQuotaQueryResponse(customerQuota, "CUSTOMER");
        
        // 查询子额度
        List<CustomerQuotaSub> subQuotas = customerQuotaSubRepository.findByCustomerQuotaId(customerId);
        if (!subQuotas.isEmpty()) {
            response.setSubQuotas(subQuotas.stream()
                .map(sub -> convertToQuotaQueryResponse(sub, "CUSTOMER_SUB"))
                .collect(Collectors.toList()));
        }
        
        response.calculateUsageRate();
        return response;
    }
    
    @Override
    public QuotaQueryResponse queryApprovalQuota(Long approvalId) {
        log.info("查询批复额度 - 批复ID: {}", approvalId);
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findById(approvalId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "批复额度不存在"));
        
        QuotaQueryResponse response = convertToQuotaQueryResponse(approvalQuota, "APPROVAL");
        
        response.calculateUsageRate();
        return response;
    }
    
    @Override
    public List<QuotaQueryResponse> queryItemQuotas(Long parentId, String parentType) {
        log.info("查询细项额度 - 父级ID: {}, 类型: {}", parentId, parentType);
        
        List<? extends Object> itemQuotas = new ArrayList<>();
        
        switch (parentType.toUpperCase()) {
            case "GROUP":
                itemQuotas = groupQuotaSubRepository.findByGroupQuotaId(parentId);
                break;
            case "CUSTOMER":
                itemQuotas = customerQuotaSubRepository.findByCustomerQuotaId(parentId);
                break;
            default:
                throw new BusinessException("200001", "不支持的父级类型: " + parentType);
        }
        
        return itemQuotas.stream()
            .map(item -> convertToQuotaQueryResponse(item, "ITEM"))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<QuotaQueryResponse> queryContractOccupancy(Long quotaId) {
        log.info("查询合同占用 - 额度ID: {}", quotaId);
        
        List<ContractOccupancy> occupancies = contractOccupancyRepository.findByApprovalQuotaSubId(quotaId);
        
        return occupancies.stream()
            .map(this::convertToQuotaQueryResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public QuotaQueryResponse queryQuota(QuotaQueryRequest request) {
        log.info("综合额度查询 - 请求: {}", request);
        
        if (request.getObjectType() != null && request.getObjectId() != null) {
            switch (request.getObjectType().toUpperCase()) {
                case "GROUP":
                    return queryGroupQuota(request.getObjectId());
                case "CUSTOMER":
                    return queryCustomerQuota(request.getObjectId());
                case "APPROVAL":
                    return queryApprovalQuota(request.getObjectId());
                default:
                    throw new BusinessException("200001", "不支持的对象类型: " + request.getObjectType());
            }
        } else if (request.getGroupId() != null) {
            return queryGroupQuota(request.getGroupId());
        } else if (request.getCustomerId() != null) {
            return queryCustomerQuota(request.getCustomerId());
        } else if (request.getApprovalId() != null) {
            return queryApprovalQuota(request.getApprovalId());
        } else {
            throw new BusinessException("200002", "必须指定查询对象");
        }
    }
    
    @Override
    public List<QuotaQueryResponse> batchQueryQuotas(List<Long> quotaIds, String quotaType) {
        log.info("批量查询额度 - 数量: {}, 类型: {}", quotaIds.size(), quotaType);
        
        List<QuotaQueryResponse> responses = new ArrayList<>();
        
        switch (quotaType.toUpperCase()) {
            case "GROUP":
                List<GroupQuota> groupQuotas = groupQuotaRepository.findAllById(quotaIds);
                responses.addAll(groupQuotas.stream()
                    .map(gq -> convertToQuotaQueryResponse(gq, "GROUP"))
                    .collect(Collectors.toList()));
                break;
            case "CUSTOMER":
                List<CustomerQuota> customerQuotas = customerQuotaRepository.findAllById(quotaIds);
                responses.addAll(customerQuotas.stream()
                    .map(cq -> convertToQuotaQueryResponse(cq, "CUSTOMER"))
                    .collect(Collectors.toList()));
                break;
            case "APPROVAL":
                List<ApprovalQuota> approvalQuotas = approvalQuotaRepository.findAllById(quotaIds);
                responses.addAll(approvalQuotas.stream()
                    .map(aq -> convertToQuotaQueryResponse(aq, "APPROVAL"))
                    .collect(Collectors.toList()));
                break;
            default:
                throw new BusinessException("200001", "不支持的额度类型: " + quotaType);
        }
        
        responses.forEach(QuotaQueryResponse::calculateUsageRate);
        return responses;
    }
    
    @Override
    public List<QuotaQueryResponse> getUsageRateStats(QuotaQueryRequest request) {
        log.info("额度使用率统计 - 开始日期: {}, 结束日期: {}", request.getStartDate(), request.getEndDate());
        
        // 这里实现使用率统计逻辑
        // 由于是示例，我们只返回模拟数据
        List<QuotaQueryResponse> stats = new ArrayList<>();
        
        // 模拟统计数据
        QuotaQueryResponse stat = new QuotaQueryResponse();
        stat.setQuotaType("STATISTICS");
        stat.setTotalQuota(new BigDecimal("10000000.00")); // 1000万
        stat.setUsedQuota(new BigDecimal("6500000.00"));    // 650万
        stat.setAvailableQuota(new BigDecimal("3500000.00")); // 350万
        stat.calculateUsageRate(); // 65%
        stat.setCustomerName("统计汇总");
        
        stats.add(stat);
        return stats;
    }
    
    @Override
    public List<QuotaQueryResponse> getDistributionStats(QuotaQueryRequest request) {
        log.info("额度分布统计 - 开始日期: {}, 结束日期: {}", request.getStartDate(), request.getEndDate());
        
        // 这里实现分布统计逻辑
        List<QuotaQueryResponse> distribution = new ArrayList<>();
        
        // 模拟不同产品类型的分布统计
        QuotaQueryResponse loanStat = new QuotaQueryResponse();
        loanStat.setProductType("LOAN");
        loanStat.setTotalQuota(new BigDecimal("5000000.00"));
        loanStat.setUsedQuota(new BigDecimal("3000000.00"));
        loanStat.setAvailableQuota(new BigDecimal("2000000.00"));
        loanStat.calculateUsageRate(); // 60%
        loanStat.setCustomerName("贷款产品");
        distribution.add(loanStat);
        
        QuotaQueryResponse billStat = new QuotaQueryResponse();
        billStat.setProductType("BILL");
        billStat.setTotalQuota(new BigDecimal("3000000.00"));
        billStat.setUsedQuota(new BigDecimal("2500000.00"));
        billStat.setAvailableQuota(new BigDecimal("500000.00"));
        billStat.calculateUsageRate(); // 83.33%
        billStat.setCustomerName("票据产品");
        distribution.add(billStat);
        
        QuotaQueryResponse cardStat = new QuotaQueryResponse();
        cardStat.setProductType("CARD");
        cardStat.setTotalQuota(new BigDecimal("2000000.00"));
        cardStat.setUsedQuota(new BigDecimal("1000000.00"));
        cardStat.setAvailableQuota(new BigDecimal("1000000.00"));
        cardStat.calculateUsageRate(); // 50%
        cardStat.setCustomerName("信用卡产品");
        distribution.add(cardStat);
        
        return distribution;
    }
    
    @Override
    public List<QuotaQueryResponse> getTrendStats(QuotaQueryRequest request) {
        log.info("额度趋势统计 - 开始日期: {}, 结束日期: {}", request.getStartDate(), request.getEndDate());
        
        // 这里实现趋势统计逻辑
        List<QuotaQueryResponse> trend = new ArrayList<>();
        
        // 模拟按月份的趋势统计
        for (int i = 1; i <= 12; i++) {
            QuotaQueryResponse monthStat = new QuotaQueryResponse();
            monthStat.setQuotaType("MONTHLY_TREND");
            monthStat.setTotalQuota(new BigDecimal("1000000").add(new BigDecimal(i * 100000)));
            monthStat.setUsedQuota(new BigDecimal("600000").add(new BigDecimal(i * 60000)));
            monthStat.setAvailableQuota(monthStat.getTotalQuota().subtract(monthStat.getUsedQuota()));
            monthStat.calculateUsageRate();
            monthStat.setCustomerName("2026-" + String.format("%02d", i));
            trend.add(monthStat);
        }
        
        return trend;
    }
    
    private QuotaQueryResponse convertToQuotaQueryResponse(Object quota, String quotaType) {
        QuotaQueryResponse response = new QuotaQueryResponse();
        
        if (quota instanceof GroupQuota) {
            GroupQuota gq = (GroupQuota) quota;
            response.setQuotaId(gq.getId());
            response.setQuotaType(quotaType);
            response.setGroupId(gq.getGroupId());
            response.setTotalQuota(gq.getTotalQuota());
            response.setUsedQuota(gq.getUsedQuota());
            response.setAvailableQuota(gq.getAvailableQuota());
            response.setLockedQuota(gq.getLockedQuota());
            response.setStatus(gq.getStatus() != null ? gq.getStatus().name() : "UNKNOWN");
            response.setCreateTime(gq.getCreateTime() != null ? gq.getCreateTime().toString() : null);
            response.setUpdateTime(gq.getUpdateTime() != null ? gq.getUpdateTime().toString() : null);
        } else if (quota instanceof CustomerQuota) {
            CustomerQuota cq = (CustomerQuota) quota;
            response.setQuotaId(cq.id);
            response.setQuotaType(quotaType);
            response.setCustomerId(cq.customerId);
            response.setGroupId(cq.groupId);
            response.setCustomerName(cq.customerName);
            response.setTotalQuota(cq.totalQuota);
            response.setUsedQuota(cq.usedQuota);
            response.setAvailableQuota(cq.availableQuota);
            response.setLockedQuota(cq.lockedQuota);
            response.setProductType(cq.productType);
            response.setStatus(cq.status != null ? cq.status.name() : "UNKNOWN");
            response.setCreateTime(cq.createTime != null ? cq.createTime.toString() : null);
            response.setUpdateTime(cq.updateTime != null ? cq.updateTime.toString() : null);
        } else if (quota instanceof ApprovalQuota) {
            ApprovalQuota aq = (ApprovalQuota) quota;
            response.setQuotaId(aq.id);
            response.setQuotaType(quotaType);
            response.setTotalQuota(aq.approvalQuota);
            response.setUsedQuota(aq.usedQuota);
            response.setAvailableQuota(aq.availableQuota);
            response.setStatus(aq.status != null ? aq.status.name() : "UNKNOWN");
            response.setApprovalNo(aq.approvalNo);
            response.setCreateTime(aq.createTime != null ? aq.createTime.toString() : null);
            response.setUpdateTime(aq.updateTime != null ? aq.updateTime.toString() : null);
        } else if (quota instanceof GroupQuotaSub) {
            GroupQuotaSub gqs = (GroupQuotaSub) quota;
            response.setQuotaId(gqs.getId());
            response.setQuotaType(quotaType);
            response.setGroupId(gqs.getGroupQuotaId());
            response.setTotalQuota(gqs.getSubQuota());
            response.setUsedQuota(gqs.getUsedQuota());
            response.setAvailableQuota(gqs.getAvailableQuota());
            response.setStatus(gqs.getStatus() != null ? gqs.getStatus().name() : "UNKNOWN");
        } else if (quota instanceof CustomerQuotaSub) {
            CustomerQuotaSub cqs = (CustomerQuotaSub) quota;
            response.setQuotaId(cqs.getId());
            response.setQuotaType(quotaType);
            response.setCustomerId(cqs.getCustomerQuotaId());
            response.setTotalQuota(cqs.getSubQuota());
            response.setUsedQuota(cqs.getUsedQuota());
            response.setAvailableQuota(cqs.getAvailableQuota());
            response.setStatus(cqs.getStatus() != null ? cqs.getStatus().name() : "UNKNOWN");
        }
        
        return response;
    }
    
    private QuotaQueryResponse convertToQuotaQueryResponse(ContractOccupancy occupancy) {
        QuotaQueryResponse response = new QuotaQueryResponse();
        response.setQuotaId(occupancy.getId());
        response.setQuotaType("CONTRACT_OCCUPANCY");
        response.setCustomerId(occupancy.getCustomerId());
        response.setTotalQuota(occupancy.getOccupancyAmount());
        response.setUsedQuota(occupancy.getOccupancyAmount());
        response.setAvailableQuota(BigDecimal.ZERO);
        response.setCurrency(occupancy.getCurrency());
        response.setApprovalNo(occupancy.getContractNo());
        response.setStatus(occupancy.getStatus() != null ? occupancy.getStatus().name() : "UNKNOWN");
        
        return response;
    }
}