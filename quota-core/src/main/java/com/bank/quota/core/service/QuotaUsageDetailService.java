package com.bank.quota.core.service;

import com.bank.quota.core.dto.quota.*;

import java.util.List;

/**
 * 额度使用明细服务
 * 
 * <p>提供银行级信贷额度管控平台的额度使用明细功能。
 * 包括额度使用明细的查询、统计等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public interface QuotaUsageDetailService {
    
    /**
     * 查询额度使用明细
     * 
     * <p>根据查询条件查询额度使用明细列表。</p>
     * 
     * @param request 额度使用明细查询请求
     * @return 额度使用明细响应列表
     */
    List<QuotaUsageDetailResponse> queryUsageDetails(QuotaUsageDetailQueryRequest request);
    
    /**
     * 查询额度使用明细（带分页）
     * 
     * <p>根据查询条件查询额度使用明细列表，支持分页。</p>
     * 
     * @param request 额度使用明细查询请求
     * @return 额度使用明细响应列表
     */
    List<QuotaUsageDetailResponse> queryUsageDetailsWithPaging(QuotaUsageDetailQueryRequest request);
    
    /**
     * 获取额度使用明细总数
     * 
     * <p>根据查询条件获取符合条件的额度使用明细总数。</p>
     * 
     * @param request 额度使用明细查询请求
     * @return 符合条件的明细总数
     */
    Long getTotalUsageCount(QuotaUsageDetailQueryRequest request);
    
    /**
     * 查询额度使用统计
     * 
     * <p>根据查询条件查询额度使用统计数据。</p>
     * 
     * @param request 额度使用明细查询请求
     * @return 额度使用统计响应
     */
    QuotaUsageStatisticsResponse getUsageStatistics(QuotaUsageDetailQueryRequest request);
    
    /**
     * 根据客户ID查询额度使用明细
     * 
     * <p>根据客户ID查询其额度使用明细。</p>
     * 
     * @param customerId 客户ID
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 额度使用明细响应列表
     */
    List<QuotaUsageDetailResponse> getUsageDetailsByCustomer(Long customerId, Integer pageNum, Integer pageSize);
    
    /**
     * 根据集团ID查询额度使用明细
     * 
     * <p>根据集团ID查询其额度使用明细。</p>
     * 
     * @param groupId 集团ID
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 额度使用明细响应列表
     */
    List<QuotaUsageDetailResponse> getUsageDetailsByGroup(Long groupId, Integer pageNum, Integer pageSize);
    
    /**
     * 根据关联ID查询额度使用明细
     * 
     * <p>根据关联ID查询其对应的额度使用明细。</p>
     * 
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @return 额度使用明细响应列表
     */
    List<QuotaUsageDetailResponse> getUsageDetailsByRelatedId(String relatedId, String relatedType);
    
    /**
     * 创建额度使用明细
     * 
     * <p>创建额度使用明细记录。</p>
     * 
     * @param detail 额度使用明细
     * @return 额度使用明细响应
     */
    QuotaUsageDetailResponse createUsageDetail(com.bank.quota.core.domain.QuotaUsageDetail detail);
}