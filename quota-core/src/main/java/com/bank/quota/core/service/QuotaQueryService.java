package com.bank.quota.core.service;

import com.bank.quota.core.dto.quotaquery.QuotaQueryRequest;
import com.bank.quota.core.dto.quotaquery.QuotaQueryResponse;

import java.util.List;

/**
 * 额度查询服务
 * 
 * <p>提供统一的额度查询功能，支持集团额度、客户额度、批复额度等多层级查询。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public interface QuotaQueryService {
    
    /**
     * 查询集团额度
     */
    QuotaQueryResponse queryGroupQuota(Long groupId);
    
    /**
     * 查询客户额度
     */
    QuotaQueryResponse queryCustomerQuota(Long customerId);
    
    /**
     * 查询批复额度
     */
    QuotaQueryResponse queryApprovalQuota(Long approvalId);
    
    /**
     * 查询细项额度
     */
    List<QuotaQueryResponse> queryItemQuotas(Long parentId, String parentType);
    
    /**
     * 查询合同占用
     */
    List<QuotaQueryResponse> queryContractOccupancy(Long quotaId);
    
    /**
     * 综合额度查询
     */
    QuotaQueryResponse queryQuota(QuotaQueryRequest request);
    
    /**
     * 批量查询额度
     */
    List<QuotaQueryResponse> batchQueryQuotas(List<Long> quotaIds, String quotaType);
    
    /**
     * 额度使用率统计
     */
    List<QuotaQueryResponse> getUsageRateStats(QuotaQueryRequest request);
    
    /**
     * 额度分布统计
     */
    List<QuotaQueryResponse> getDistributionStats(QuotaQueryRequest request);
    
    /**
     * 额度趋势统计
     */
    List<QuotaQueryResponse> getTrendStats(QuotaQueryRequest request);
}