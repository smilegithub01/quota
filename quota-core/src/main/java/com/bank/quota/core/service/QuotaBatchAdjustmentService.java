package com.bank.quota.core.service;

import com.bank.quota.core.dto.quota.*;

import java.util.List;

/**
 * 额度批量调整服务
 * 
 * <p>提供银行级信贷额度管控平台的额度批量调整功能。
 * 支持批量额度调整操作，适用于定期批量处理场景。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public interface QuotaBatchAdjustmentService {
    
    /**
     * 发起批量调整
     * 
     * <p>根据请求参数发起批量额度调整操作。</p>
     * 
     * @param request 批量调整请求
     * @return 批量调整响应
     */
    QuotaBatchAdjustmentResponse initiateBatchAdjustment(QuotaBatchAdjustmentRequest request);
    
    /**
     * 执行批量调整
     * 
     * <p>执行指定的批量调整任务。</p>
     * 
     * @param batchId 批量ID
     * @return 执行结果
     */
    boolean executeBatchAdjustment(Long batchId);
    
    /**
     * 查询批量调整
     * 
     * <p>根据ID查询批量调整信息。</p>
     * 
     * @param batchId 批量ID
     * @return 批量调整响应
     */
    QuotaBatchAdjustmentResponse getBatchAdjustment(Long batchId);
    
    /**
     * 查询批量调整明细
     * 
     * <p>根据批量ID查询其明细信息。</p>
     * 
     * @param batchId 批量ID
     * @return 批量调整明细响应列表
     */
    List<QuotaBatchAdjustmentDetailResponse> getBatchAdjustmentDetails(Long batchId);
    
    /**
     * 查询批量调整列表
     * 
     * <p>根据条件查询批量调整列表。</p>
     * 
     * @param status 状态
     * @param executorId 执行者ID
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 批量调整响应列表
     */
    List<QuotaBatchAdjustmentResponse> getBatchAdjustments(String status, String executorId, Integer pageNum, Integer pageSize);
    
    /**
     * 取消批量调整
     * 
     * <p>取消指定的批量调整任务（仅限待处理状态）。</p>
     * 
     * @param batchId 批量ID
     * @return 是否取消成功
     */
    boolean cancelBatchAdjustment(Long batchId);
    
    /**
     * 重试失败的批量调整
     * 
     * <p>重试指定批次中失败的调整项。</p>
     * 
     * @param batchId 批量ID
     * @return 是否重试成功
     */
    boolean retryFailedAdjustments(Long batchId);
}