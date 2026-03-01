package com.bank.quota.core.repository;

import com.bank.quota.core.domain.QuotaBatchAdjustmentDetail;
import com.bank.quota.core.enums.AdjustmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 额度批量调整明细仓储接口
 * 
 * <p>提供对额度批量调整明细数据访问的操作接口。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Repository
public interface QuotaBatchAdjustmentDetailRepository extends JpaRepository<QuotaBatchAdjustmentDetail, Long> {
    
    /**
     * 根据批量ID查询明细
     * 
     * @param batchId 批量ID
     * @return 明细列表
     */
    List<QuotaBatchAdjustmentDetail> findByBatchId(Long batchId);
    
    /**
     * 根据批量ID和状态查询明细
     * 
     * @param batchId 批量ID
     * @param status 状态
     * @return 明细列表
     */
    List<QuotaBatchAdjustmentDetail> findByBatchIdAndStatus(@Param("batchId") Long batchId, 
                                                          @Param("status") AdjustmentStatus status);
    
    /**
     * 根据批量ID统计各状态的数量
     * 
     * @param batchId 批量ID
     * @return 状态统计结果
     */
    @Query("SELECT d.status, COUNT(d) FROM QuotaBatchAdjustmentDetail d WHERE d.batchId = :batchId GROUP BY d.status")
    List<Object[]> countByBatchIdAndStatus(@Param("batchId") Long batchId);
}