package com.bank.quota.core.repository;

import com.bank.quota.core.domain.QuotaBatchAdjustment;
import com.bank.quota.core.domain.QuotaBatchAdjustmentDetail;
import com.bank.quota.core.enums.AdjustmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 额度批量调整仓储接口
 * 
 * <p>提供对额度批量调整数据访问的操作接口。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Repository
public interface QuotaBatchAdjustmentRepository extends JpaRepository<QuotaBatchAdjustment, Long> {
    
    /**
     * 根据状态查询批量调整
     * 
     * @param status 状态
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 批量调整列表
     */
    @Query("SELECT qba FROM QuotaBatchAdjustment qba WHERE qba.status = :status ORDER BY qba.createTime DESC")
    List<QuotaBatchAdjustment> findByStatus(@Param("status") String status, 
                                            @Param("offset") int offset, 
                                            @Param("limit") int limit);
    
    /**
     * 根据执行者ID查询批量调整
     * 
     * @param executorId 执行者ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 批量调整列表
     */
    @Query("SELECT qba FROM QuotaBatchAdjustment qba WHERE qba.executorId = :executorId ORDER BY qba.createTime DESC")
    List<QuotaBatchAdjustment> findByExecutorId(@Param("executorId") String executorId, 
                                               @Param("offset") int offset, 
                                               @Param("limit") int limit);
    
    /**
     * 根据状态和执行者ID查询批量调整
     * 
     * @param status 状态
     * @param executorId 执行者ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 批量调整列表
     */
    @Query("SELECT qba FROM QuotaBatchAdjustment qba WHERE qba.status = :status AND qba.executorId = :executorId ORDER BY qba.createTime DESC")
    List<QuotaBatchAdjustment> findByStatusAndExecutorId(@Param("status") String status, 
                                                        @Param("executorId") String executorId, 
                                                        @Param("offset") int offset, 
                                                        @Param("limit") int limit);
    
    /**
     * 查询所有批量调整（按ID倒序）
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 批量调整列表
     */
    @Query("SELECT qba FROM QuotaBatchAdjustment qba ORDER BY qba.id DESC")
    List<QuotaBatchAdjustment> findAllByOrderByIdDesc(@Param("offset") int offset, 
                                                     @Param("limit") int limit);
}