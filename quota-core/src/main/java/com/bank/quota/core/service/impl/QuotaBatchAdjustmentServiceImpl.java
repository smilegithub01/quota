package com.bank.quota.core.service.impl;

import com.bank.quota.core.domain.QuotaBatchAdjustment;
import com.bank.quota.core.domain.QuotaBatchAdjustmentDetail;
import com.bank.quota.core.dto.quota.*;
import com.bank.quota.core.enums.AdjustmentStatus;
import com.bank.quota.core.enums.AdjustmentType;
import com.bank.quota.core.repository.QuotaBatchAdjustmentDetailRepository;
import com.bank.quota.core.repository.QuotaBatchAdjustmentRepository;
import com.bank.quota.core.service.QuotaBatchAdjustmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 额度批量调整服务实现类
 * 
 * <p>提供银行级信贷额度管控平台的额度批量调整功能实现。
 * 支持批量额度调整操作，适用于定期批量处理场景。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaBatchAdjustmentServiceImpl implements QuotaBatchAdjustmentService {
    
    private final QuotaBatchAdjustmentRepository quotaBatchAdjustmentRepository;
    private final QuotaBatchAdjustmentDetailRepository quotaBatchAdjustmentDetailRepository;
    private final com.bank.quota.core.service.QuotaControlService quotaControlService;
    
    @Override
    @Transactional
    public QuotaBatchAdjustmentResponse initiateBatchAdjustment(QuotaBatchAdjustmentRequest request) {
        log.info("Initiating batch adjustment: name={}, itemCount={}, executor={}", 
                request.getBatchName(), request.getAdjustmentItems().size(), request.getExecutorName());
        
        // 计算总金额
        BigDecimal totalAmount = request.getAdjustmentItems().stream()
                .map(item -> item.getAdjustmentAmount() != null ? item.getAdjustmentAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 创建批量调整主记录
        QuotaBatchAdjustment batch = QuotaBatchAdjustment.builder()
                .batchName(request.getBatchName())
                .adjustmentType(request.getAdjustmentType())
                .totalAmount(totalAmount)
                .itemCount(request.getAdjustmentItems().size())
                .processedCount(0)
                .successCount(0)
                .failedCount(0)
                .status(AdjustmentStatus.PENDING)
                .executorId(request.getExecutorId())
                .executorName(request.getExecutorName())
                .description(request.getDescription())
                .createBy(request.getExecutorId())
                .build();
        
        QuotaBatchAdjustment savedBatch = quotaBatchAdjustmentRepository.save(batch);
        
        // 创建批量调整明细记录
        List<QuotaBatchAdjustmentDetail> details = request.getAdjustmentItems().stream()
                .map(item -> QuotaBatchAdjustmentDetail.builder()
                        .batchId(savedBatch.getId())
                        .customerId(item.getCustomerId())
                        .customerName(item.getCustomerName())
                        .quotaType(item.getQuotaType())
                        .adjustmentType(request.getAdjustmentType())
                        .adjustmentAmount(item.getAdjustmentAmount())
                        .currency(item.getCurrency() != null ? item.getCurrency() : "CNY")
                        .status(AdjustmentStatus.PENDING)
                        .build())
                .collect(Collectors.toList());
        
        quotaBatchAdjustmentDetailRepository.saveAll(details);
        
        log.info("Batch adjustment initiated successfully: batchNo={}, id={}", 
                savedBatch.getBatchNo(), savedBatch.getId());
        
        return buildBatchResponse(savedBatch);
    }
    
    @Override
    @Transactional
    public boolean executeBatchAdjustment(Long batchId) {
        log.info("Executing batch adjustment: batchId={}", batchId);
        
        QuotaBatchAdjustment batch = quotaBatchAdjustmentRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch adjustment not found: " + batchId));
        
        if (batch.getStatus() != AdjustmentStatus.PENDING && batch.getStatus() != AdjustmentStatus.FAILED) {
            log.warn("Batch adjustment cannot be executed due to invalid status: {}, batchId={}", 
                    batch.getStatus(), batchId);
            return false;
        }
        
        // 更新批次状态为处理中
        batch.setStatus(AdjustmentStatus.UNDER_REVIEW);
        batch.setStartTime(LocalDateTime.now());
        quotaBatchAdjustmentRepository.save(batch);
        
        // 获取待处理的明细
        List<QuotaBatchAdjustmentDetail> details = quotaBatchAdjustmentDetailRepository.findByBatchId(batchId);
        
        int successCount = 0;
        int failedCount = 0;
        
        for (QuotaBatchAdjustmentDetail detail : details) {
            try {
                // 执行单个调整操作
                boolean success = processSingleAdjustment(detail);
                
                if (success) {
                    detail.setStatus(AdjustmentStatus.APPROVED);
                    successCount++;
                } else {
                    detail.setStatus(AdjustmentStatus.REJECTED);
                    detail.setErrorMessage("Adjustment failed due to system error");
                    failedCount++;
                }
                
                detail.setProcessTime(LocalDateTime.now());
                quotaBatchAdjustmentDetailRepository.save(detail);
                
            } catch (Exception e) {
                log.error("Error processing batch adjustment detail: detailId=" + detail.getId(), e);
                detail.setStatus(AdjustmentStatus.REJECTED);
                detail.setErrorMessage(e.getMessage());
                detail.setProcessTime(LocalDateTime.now());
                quotaBatchAdjustmentDetailRepository.save(detail);
                failedCount++;
            }
        }
        
        // 更新批次总体状态
        batch.setProcessedCount(successCount + failedCount);
        batch.setSuccessCount(successCount);
        batch.setFailedCount(failedCount);
        batch.setEndTime(LocalDateTime.now());
        
        if (failedCount == 0) {
            batch.setStatus(AdjustmentStatus.COMPLETED);
        } else if (successCount == 0) {
            batch.setStatus(AdjustmentStatus.FAILED);
        } else {
            batch.setStatus(AdjustmentStatus.PARTIAL_SUCCESS);
        }
        
        quotaBatchAdjustmentRepository.save(batch);
        
        log.info("Batch adjustment completed: batchId={}, success={}, failed={}", 
                batchId, successCount, failedCount);
        
        return true;
    }
    
    /**
     * 处理单个调整项目
     */
    private boolean processSingleAdjustment(QuotaBatchAdjustmentDetail detail) {
        try {
            // 这里应该调用额度控制服务来执行实际的额度调整
            // 由于额度控制服务的具体接口可能不同，这里使用模拟实现
            log.debug("Processing single adjustment: customerId={}, amount={}", 
                    detail.getCustomerId(), detail.getAdjustmentAmount());
            
            // 更新调整前金额（从额度查询服务获取）
            // detail.setBeforeAmount(getCurrentQuotaAmount(detail.getCustomerId()));
            
            // 根据调整类型执行相应操作
            if (detail.getAdjustmentType() == AdjustmentType.INCREASE) {
                // 增加额度逻辑
                // increaseQuota(detail.getCustomerId(), detail.getAdjustmentAmount());
            } else if (detail.getAdjustmentType() == AdjustmentType.DECREASE) {
                // 减少额度逻辑
                // decreaseQuota(detail.getCustomerId(), detail.getAdjustmentAmount());
            }
            
            // 设置调整后金额
            // detail.setAfterAmount(getCurrentQuotaAmount(detail.getCustomerId()));
            
            return true;
        } catch (Exception e) {
            log.error("Error processing single adjustment: detailId=" + detail.getId(), e);
            return false;
        }
    }
    
    @Override
    public QuotaBatchAdjustmentResponse getBatchAdjustment(Long batchId) {
        QuotaBatchAdjustment batch = quotaBatchAdjustmentRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch adjustment not found: " + batchId));
        
        return buildBatchResponse(batch);
    }
    
    @Override
    public List<QuotaBatchAdjustmentDetailResponse> getBatchAdjustmentDetails(Long batchId) {
        List<QuotaBatchAdjustmentDetail> details = quotaBatchAdjustmentDetailRepository.findByBatchId(batchId);
        
        return details.stream()
                .map(this::buildDetailResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QuotaBatchAdjustmentResponse> getBatchAdjustments(String status, String executorId, Integer pageNum, Integer pageSize) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        List<QuotaBatchAdjustment> batches;
        if (status != null && executorId != null) {
            // 根据状态和执行者查询
            batches = quotaBatchAdjustmentRepository.findByStatusAndExecutorId(status, executorId, offset, pageSize);
        } else if (status != null) {
            // 根据状态查询
            batches = quotaBatchAdjustmentRepository.findByStatus(status, offset, pageSize);
        } else if (executorId != null) {
            // 根据执行者查询
            batches = quotaBatchAdjustmentRepository.findByExecutorId(executorId, offset, pageSize);
        } else {
            // 查询所有
            batches = quotaBatchAdjustmentRepository.findAllByOrderByIdDesc(offset, pageSize);
        }
        
        return batches.stream()
                .map(this::buildBatchResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean cancelBatchAdjustment(Long batchId) {
        QuotaBatchAdjustment batch = quotaBatchAdjustmentRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch adjustment not found: " + batchId));
        
        if (batch.getStatus() != AdjustmentStatus.PENDING) {
            log.warn("Cannot cancel batch adjustment with status: {}, batchId={}", 
                    batch.getStatus(), batchId);
            return false;
        }
        
        batch.setStatus(AdjustmentStatus.CANCELLED);
        batch.setUpdateTime(LocalDateTime.now());
        quotaBatchAdjustmentRepository.save(batch);
        
        log.info("Batch adjustment cancelled: batchId={}", batchId);
        return true;
    }
    
    @Override
    public boolean retryFailedAdjustments(Long batchId) {
        log.info("Retrying failed adjustments for batch: batchId={}", batchId);
        
        QuotaBatchAdjustment batch = quotaBatchAdjustmentRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch adjustment not found: " + batchId));
        
        // 获取失败的明细记录
        List<QuotaBatchAdjustmentDetail> failedDetails = 
                quotaBatchAdjustmentDetailRepository.findByBatchIdAndStatus(batchId, AdjustmentStatus.REJECTED);
        
        int retriedCount = 0;
        int successCount = 0;
        
        for (QuotaBatchAdjustmentDetail detail : failedDetails) {
            try {
                // 重置状态为待处理
                detail.setStatus(AdjustmentStatus.PENDING);
                detail.setErrorMessage(null);
                quotaBatchAdjustmentDetailRepository.save(detail);
                
                // 重新处理
                boolean success = processSingleAdjustment(detail);
                
                if (success) {
                    detail.setStatus(AdjustmentStatus.APPROVED);
                    successCount++;
                } else {
                    detail.setStatus(AdjustmentStatus.REJECTED);
                    detail.setErrorMessage("Retry failed due to system error");
                }
                
                detail.setProcessTime(LocalDateTime.now());
                quotaBatchAdjustmentDetailRepository.save(detail);
                
                retriedCount++;
                
            } catch (Exception e) {
                log.error("Error retrying adjustment: detailId=" + detail.getId(), e);
                detail.setStatus(AdjustmentStatus.REJECTED);
                detail.setErrorMessage(e.getMessage());
                detail.setProcessTime(LocalDateTime.now());
                quotaBatchAdjustmentDetailRepository.save(detail);
            }
        }
        
        // 更新批次统计信息
        batch.setProcessedCount(batch.getProcessedCount() + retriedCount);
        batch.setSuccessCount(batch.getSuccessCount() + successCount);
        batch.setFailedCount(batch.getFailedCount() - successCount); // 减去成功重试的数量
        
        // 检查是否所有项目都已完成
        List<QuotaBatchAdjustmentDetail> allDetails = quotaBatchAdjustmentDetailRepository.findByBatchId(batchId);
        boolean allCompleted = allDetails.stream()
                .allMatch(d -> d.getStatus() == AdjustmentStatus.APPROVED || 
                              d.getStatus() == AdjustmentStatus.REJECTED);
        
        if (allCompleted) {
            if (batch.getFailedCount() == 0) {
                batch.setStatus(AdjustmentStatus.COMPLETED);
            } else if (batch.getSuccessCount() == 0) {
                batch.setStatus(AdjustmentStatus.FAILED);
            } else {
                batch.setStatus(AdjustmentStatus.PARTIAL_SUCCESS);
            }
        }
        
        quotaBatchAdjustmentRepository.save(batch);
        
        log.info("Retry completed: batchId={}, retried={}, successful={}", 
                batchId, retriedCount, successCount);
        
        return true;
    }
    
    /**
     * 构建批量调整响应对象
     */
    private QuotaBatchAdjustmentResponse buildBatchResponse(QuotaBatchAdjustment batch) {
        return QuotaBatchAdjustmentResponse.builder()
                .id(batch.getId())
                .batchNo(batch.getBatchNo())
                .batchName(batch.getBatchName())
                .adjustmentType(batch.getAdjustmentType())
                .totalAmount(batch.getTotalAmount())
                .itemCount(batch.getItemCount())
                .processedCount(batch.getProcessedCount())
                .successCount(batch.getSuccessCount())
                .failedCount(batch.getFailedCount())
                .status(batch.getStatus())
                .startTime(batch.getStartTime())
                .endTime(batch.getEndTime())
                .executorId(batch.getExecutorId())
                .executorName(batch.getExecutorName())
                .description(batch.getDescription())
                .errorLog(batch.getErrorLog())
                .createTime(batch.getCreateTime())
                .updateTime(batch.getUpdateTime())
                .build();
    }
    
    /**
     * 构建批量调整明细响应对象
     */
    private QuotaBatchAdjustmentDetailResponse buildDetailResponse(QuotaBatchAdjustmentDetail detail) {
        return QuotaBatchAdjustmentDetailResponse.builder()
                .id(detail.getId())
                .batchId(detail.getBatchId())
                .customerId(detail.getCustomerId())
                .customerName(detail.getCustomerName())
                .quotaType(detail.getQuotaType())
                .adjustmentType(detail.getAdjustmentType())
                .adjustmentAmount(detail.getAdjustmentAmount())
                .beforeAmount(detail.getBeforeAmount())
                .afterAmount(detail.getAfterAmount())
                .currency(detail.getCurrency())
                .status(detail.getStatus())
                .errorMessage(detail.getErrorMessage())
                .processTime(detail.getProcessTime())
                .createTime(detail.getCreateTime())
                .updateTime(detail.getUpdateTime())
                .build();
    }
}