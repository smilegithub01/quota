package com.bank.quota.core.repository;

import com.bank.quota.core.domain.Customer;
import com.bank.quota.core.domain.Customer.CustomerCategory;
import com.bank.quota.core.domain.Customer.CustomerStatus;
import com.bank.quota.core.domain.Customer.CustomerType;
import com.bank.quota.core.domain.Customer.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户仓储接口
 * 
 * <p>提供客户基础信息的数据库访问操作。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>客户基本信息CRUD操作</li>
 *   <li>按客户编号、证件号码查询</li>
 *   <li>按集团ID查询集团下所有客户</li>
 *   <li>按客户类型、分类、风险等级查询</li>
 *   <li>分页查询支持</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * 根据客户编号查询
     * 
     * @param customerNo 客户编号
     * @return 客户信息
     */
    Optional<Customer> findByCustomerNo(String customerNo);

    /**
     * 根据证件号码查询
     * 
     * @param idNumber 证件号码
     * @return 客户信息
     */
    Optional<Customer> findByIdNumber(String idNumber);

    /**
     * 根据集团ID查询所有客户
     * 
     * @param groupId 集团ID
     * @return 客户列表
     */
    List<Customer> findByGroupId(Long groupId);

    /**
     * 根据集团ID查询有效客户
     * 
     * @param groupId 集团ID
     * @param status 客户状态
     * @return 客户列表
     */
    List<Customer> findByGroupIdAndStatus(Long groupId, CustomerStatus status);

    /**
     * 根据客户类型查询
     * 
     * @param customerType 客户类型
     * @return 客户列表
     */
    List<Customer> findByCustomerType(CustomerType customerType);

    /**
     * 根据客户分类查询
     * 
     * @param category 客户分类
     * @return 客户列表
     */
    List<Customer> findByCategory(CustomerCategory category);

    /**
     * 根据风险等级查询
     * 
     * @param riskLevel 风险等级
     * @return 客户列表
     */
    List<Customer> findByRiskLevel(RiskLevel riskLevel);

    /**
     * 根据状态查询
     * 
     * @param status 客户状态
     * @return 客户列表
     */
    List<Customer> findByStatus(CustomerStatus status);

    /**
     * 分页查询客户
     * 
     * @param customerType 客户类型
     * @param category 客户分类
     * @param status 客户状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Customer> findByCustomerTypeAndCategoryAndStatus(
            CustomerType customerType, 
            CustomerCategory category, 
            CustomerStatus status, 
            Pageable pageable);

    /**
     * 按客户名称模糊查询
     * 
     * @param customerName 客户名称
     * @return 客户列表
     */
    List<Customer> findByCustomerNameContaining(String customerName);

    /**
     * 检查客户编号是否存在
     * 
     * @param customerNo 客户编号
     * @return 是否存在
     */
    boolean existsByCustomerNo(String customerNo);

    /**
     * 检查证件号码是否存在
     * 
     * @param idNumber 证件号码
     * @return 是否存在
     */
    boolean existsByIdNumber(String idNumber);

    /**
     * 统计集团下客户数量
     * 
     * @param groupId 集团ID
     * @return 客户数量
     */
    long countByGroupId(Long groupId);

    /**
     * 统计指定状态的客户数量
     * 
     * @param status 客户状态
     * @return 客户数量
     */
    long countByStatus(CustomerStatus status);

    /**
     * 查询单一客户（不属于任何集团）
     * 
     * @return 单一客户列表
     */
    @Query("SELECT c FROM Customer c WHERE c.category = 'SINGLE_CUSTOMER' AND c.status = :status")
    List<Customer> findSingleCustomers(@Param("status") CustomerStatus status);

    /**
     * 查询集团客户（属于某个集团）
     * 
     * @return 集团客户列表
     */
    @Query("SELECT c FROM Customer c WHERE c.category = 'GROUP_CUSTOMER' AND c.status = :status")
    List<Customer> findGroupCustomers(@Param("status") CustomerStatus status);

    /**
     * 根据行业代码查询客户
     * 
     * @param industryCode 行业代码
     * @return 客户列表
     */
    List<Customer> findByIndustryCode(String industryCode);
}
