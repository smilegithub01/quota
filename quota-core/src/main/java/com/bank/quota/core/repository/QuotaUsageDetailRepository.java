package com.bank.quota.core.repository;

import com.bank.quota.core.domain.QuotaUsageDetail;
import com.bank.quota.core.enums.BusinessType;
import com.bank.quota.core.enums.QuotaUsageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 额度使用明细仓储接口
 * 
 * <p>提供对额度使用明细数据访问的操作接口。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Repository
public interface QuotaUsageDetailRepository extends JpaRepository<QuotaUsageDetail, Long> {
    
    /**
     * 根据客户ID查询额度使用明细
     * 
     * @param customerId 客户ID
     * @return 额度使用明细列表
     */
    List<QuotaUsageDetail> findByCustomerId(Long customerId);
    
    /**
     * 根据客户ID和分页参数查询额度使用明细
     * 
     * @param customerId 客户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 额度使用明细列表
     */
    @Query("SELECT qud FROM QuotaUsageDetail qud WHERE qud.customerId = :customerId ORDER BY qud.createTime DESC")
    List<QuotaUsageDetail> findByCustomerIdWithPaging(@Param("customerId") Long customerId, 
                                                      @Param("offset") int offset, 
                                                      @Param("limit") int limit);
    
    /**
     * 根据集团ID查询额度使用明细
     * 
     * @param groupId 集团ID
     * @return 额度使用明细列表
     */
    List<QuotaUsageDetail> findByGroupId(Long groupId);
    
    /**
     * 根据集团ID和分页参数查询额度使用明细
     * 
     * @param groupId 集团ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 额度使用明细列表
     */
    @Query("SELECT qud FROM QuotaUsageDetail qud WHERE qud.groupId = :groupId ORDER BY qud.createTime DESC")
    List<QuotaUsageDetail> findByGroupIdWithPaging(@Param("groupId") Long groupId, 
                                                   @Param("offset") int offset, 
                                                   @Param("limit") int limit);
    
    /**
     * 根据关联ID和类型查询额度使用明细
     * 
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @return 额度使用明细列表
     */
    List<QuotaUsageDetail> findByRelatedIdAndType(@Param("relatedId") String relatedId, 
                                                  @Param("relatedType") String relatedType);
    
    /**
     * 根据业务类型查询额度使用明细
     * 
     * @param businessType 业务类型
     * @return 额度使用明细列表
     */
    List<QuotaUsageDetail> findByBusinessType(BusinessType businessType);
    
    /**
     * 根据使用类型查询额度使用明细
     * 
     * @param usageType 使用类型
     * @return 额度使用明细列表
     */
    List<QuotaUsageDetail> findByUsageType(QuotaUsageType usageType);
    
    /**
     * 根据日期范围查询额度使用明细
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 额度使用明细列表
     */
    List<QuotaUsageDetail> findByUsageDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 根据多个条件查询额度使用明细
     * 
     * @param customerId 客户ID
     * @param groupId 集团ID
     * @param businessType 业务类型
     * @param usageType 使用类型
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @param operatorId 操作员ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 额度使用明细列表
     */
    @Query("SELECT qud FROM QuotaUsageDetail qud WHERE " +
           "(:customerId IS NULL OR qud.customerId = :customerId) AND " +
           "(:groupId IS NULL OR qud.groupId = :groupId) AND " +
           "(:businessType IS NULL OR qud.businessType = :businessType) AND " +
           "(:usageType IS NULL OR qud.usageType = :usageType) AND " +
           "(:relatedId IS NULL OR qud.relatedId = :relatedId) AND " +
           "(:relatedType IS NULL OR qud.relatedType = :relatedType) AND " +
           "(:operatorId IS NULL OR qud.operatorId = :operatorId) AND " +
           "(:startDate IS NULL OR qud.usageDate >= :startDate) AND " +
           "(:endDate IS NULL OR qud.usageDate <= :endDate) " +
           "ORDER BY qud.createTime DESC")
    List<QuotaUsageDetail> findByConditions(@Param("customerId") Long customerId,
                                            @Param("groupId") Long groupId,
                                            @Param("businessType") BusinessType businessType,
                                            @Param("usageType") QuotaUsageType usageType,
                                            @Param("relatedId") String relatedId,
                                            @Param("relatedType") String relatedType,
                                            @Param("operatorId") String operatorId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根据多个条件查询额度使用明细（带分页）
     * 
     * @param customerId 客户ID
     * @param groupId 集团ID
     * @param businessType 业务类型
     * @param usageType 使用类型
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @param operatorId 操作员ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 额度使用明细列表
     */
    @Query("SELECT qud FROM QuotaUsageDetail qud WHERE " +
           "(:customerId IS NULL OR qud.customerId = :customerId) AND " +
           "(:groupId IS NULL OR qud.groupId = :groupId) AND " +
           "(:businessType IS NULL OR qud.businessType = :businessType) AND " +
           "(:usageType IS NULL OR qud.usageType = :usageType) AND " +
           "(:relatedId IS NULL OR qud.relatedId = :relatedId) AND " +
           "(:relatedType IS NULL OR qud.relatedType = :relatedType) AND " +
           "(:operatorId IS NULL OR qud.operatorId = :operatorId) AND " +
           "(:startDate IS NULL OR qud.usageDate >= :startDate) AND " +
           "(:endDate IS NULL OR qud.usageDate <= :endDate) " +
           "ORDER BY qud.createTime DESC")
    List<QuotaUsageDetail> findByConditionsWithPaging(@Param("customerId") Long customerId,
                                                      @Param("groupId") Long groupId,
                                                      @Param("businessType") BusinessType businessType,
                                                      @Param("usageType") QuotaUsageType usageType,
                                                      @Param("relatedId") String relatedId,
                                                      @Param("relatedType") String relatedType,
                                                      @Param("operatorId") String operatorId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      @Param("offset") int offset,
                                                      @Param("limit") int limit);
    
    /**
     * 根据多个条件统计额度使用明细数量
     * 
     * @param customerId 客户ID
     * @param groupId 集团ID
     * @param businessType 业务类型
     * @param usageType 使用类型
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @param operatorId 操作员ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 符合条件的明细数量
     */
    @Query("SELECT COUNT(qud) FROM QuotaUsageDetail qud WHERE " +
           "(:customerId IS NULL OR qud.customerId = :customerId) AND " +
           "(:groupId IS NULL OR qud.groupId = :groupId) AND " +
           "(:businessType IS NULL OR qud.businessType = :businessType) AND " +
           "(:usageType IS NULL OR qud.usageType = :usageType) AND " +
           "(:relatedId IS NULL OR qud.relatedId = :relatedId) AND " +
           "(:relatedType IS NULL OR qud.relatedType = :relatedType) AND " +
           "(:operatorId IS NULL OR qud.operatorId = :operatorId) AND " +
           "(:startDate IS NULL OR qud.usageDate >= :startDate) AND " +
           "(:endDate IS NULL OR qud.usageDate <= :endDate)")
    Long countByConditions(@Param("customerId") Long customerId,
                           @Param("groupId") Long groupId,
                           @Param("businessType") BusinessType businessType,
                           @Param("usageType") QuotaUsageType usageType,
                           @Param("relatedId") String relatedId,
                           @Param("relatedType") String relatedType,
                           @Param("operatorId") String operatorId,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate);
    

    
    /**
     * 获取按使用类型统计的信息
     * 
     * @param customerId 客户ID
     * @param groupId 集团ID
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 使用类型统计信息列表
     */
    @Query("SELECT " +
           "qud.usageType.code as usageType, " +
           "COUNT(qud) as count, " +
           "SUM(qud.usageAmount) as amount " +
           "FROM QuotaUsageDetail qud WHERE " +
           "(:customerId IS NULL OR qud.customerId = :customerId) AND " +
           "(:groupId IS NULL OR qud.groupId = :groupId) AND " +
           "(:businessType IS NULL OR qud.businessType = :businessType) AND " +
           "(:startDate IS NULL OR qud.usageDate >= :startDate) AND " +
           "(:endDate IS NULL OR qud.usageDate <= :endDate) " +
           "GROUP BY qud.usageType")
    List<Object[]> getUsageTypeStatistics(@Param("customerId") Long customerId,
                                          @Param("groupId") Long groupId,
                                          @Param("businessType") BusinessType businessType,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取按客户统计的信息
     * 
     * @param customerId 客户ID
     * @param groupId 集团ID
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 客户统计信息列表
     */
    @Query("SELECT " +
           "qud.customerId as customerId, " +
           "qud.customerName as customerName, " +
           "COUNT(qud) as count, " +
           "SUM(qud.usageAmount) as amount " +
           "FROM QuotaUsageDetail qud WHERE " +
           "(:customerId IS NULL OR qud.customerId = :customerId) AND " +
           "(:groupId IS NULL OR qud.groupId = :groupId) AND " +
           "(:businessType IS NULL OR qud.businessType = :businessType) AND " +
           "(:startDate IS NULL OR qud.usageDate >= :startDate) AND " +
           "(:endDate IS NULL OR qud.usageDate <= :endDate) " +
           "GROUP BY qud.customerId, qud.customerName")
    List<Object[]> getCustomerStatistics(@Param("customerId") Long customerId,
                                         @Param("groupId") Long groupId,
                                         @Param("businessType") BusinessType businessType,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根据业务参考号查询额度使用明细
     * 
     * @param businessRefNo 业务参考号
     * @return 额度使用明细列表
     */
    List<QuotaUsageDetail> findByRelatedId(String businessRefNo);
    
    /**
     * 根据条件统计总使用金额
     * 
     * @param customerId 客户ID
     * @param groupId 集团ID
     * @param businessType 业务类型
     * @param usageType 使用类型
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @param operatorId 操作员ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总使用金额
     */
    @Query("SELECT COALESCE(SUM(qud.usageAmount), 0) FROM QuotaUsageDetail qud WHERE " +
           "(:customerId IS NULL OR qud.customerId = :customerId) AND " +
           "(:groupId IS NULL OR qud.groupId = :groupId) AND " +
           "(:businessType IS NULL OR qud.businessType = :businessType) AND " +
           "(:usageType IS NULL OR qud.usageType = :usageType) AND " +
           "(:relatedId IS NULL OR qud.relatedId = :relatedId) AND " +
           "(:relatedType IS NULL OR qud.relatedType = :relatedType) AND " +
           "(:operatorId IS NULL OR qud.operatorId = :operatorId) AND " +
           "(:startDate IS NULL OR qud.usageDate >= :startDate) AND " +
           "(:endDate IS NULL OR qud.usageDate <= :endDate)")
    BigDecimal sumUsageAmountByConditions(@Param("customerId") Long customerId,
                                          @Param("groupId") Long groupId,
                                          @Param("businessType") BusinessType businessType,
                                          @Param("usageType") QuotaUsageType usageType,
                                          @Param("relatedId") String relatedId,
                                          @Param("relatedType") String relatedType,
                                          @Param("operatorId") String operatorId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}