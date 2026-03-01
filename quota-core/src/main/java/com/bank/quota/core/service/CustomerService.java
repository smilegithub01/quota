package com.bank.quota.core.service;

import com.bank.quota.core.domain.Customer;
import com.bank.quota.core.dto.customer.*;

import java.util.List;

/**
 * 客户服务接口
 * 
 * <p>提供客户基础信息的业务操作服务。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>客户创建、更新、查询、删除</li>
 *   <li>集团客户和单一客户分类管理</li>
 *   <li>客户风险等级管理</li>
 *   <li>客户状态变更（冻结、解冻、停用）</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
public interface CustomerService {

    /**
     * 创建客户
     * 
     * @param request 创建请求
     * @return 客户信息
     */
    CustomerResponse createCustomer(CreateCustomerRequest request);

    /**
     * 更新客户信息
     * 
     * @param customerId 客户ID
     * @param request 更新请求
     * @return 客户信息
     */
    CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request);

    /**
     * 根据ID查询客户
     * 
     * @param customerId 客户ID
     * @return 客户信息
     */
    CustomerResponse getCustomerById(Long customerId);

    /**
     * 根据客户编号查询
     * 
     * @param customerNo 客户编号
     * @return 客户信息
     */
    CustomerResponse getCustomerByNo(String customerNo);

    /**
     * 根据证件号码查询
     * 
     * @param idNumber 证件号码
     * @return 客户信息
     */
    CustomerResponse getCustomerByIdNumber(String idNumber);

    /**
     * 查询集团下所有客户
     * 
     * @param groupId 集团ID
     * @return 客户列表
     */
    List<CustomerResponse> getCustomersByGroupId(Long groupId);

    /**
     * 按客户类型查询
     * 
     * @param customerType 客户类型
     * @return 客户列表
     */
    List<CustomerResponse> getCustomersByType(String customerType);

    /**
     * 按客户分类查询
     * 
     * @param category 客户分类
     * @return 客户列表
     */
    List<CustomerResponse> getCustomersByCategory(String category);

    /**
     * 按风险等级查询
     * 
     * @param riskLevel 风险等级
     * @return 客户列表
     */
    List<CustomerResponse> getCustomersByRiskLevel(String riskLevel);

    /**
     * 冻结客户
     * 
     * @param customerId 客户ID
     * @param reason 冻结原因
     * @param operator 操作人
     * @return 客户信息
     */
    CustomerResponse freezeCustomer(Long customerId, String reason, String operator);

    /**
     * 解冻客户
     * 
     * @param customerId 客户ID
     * @param operator 操作人
     * @return 客户信息
     */
    CustomerResponse unfreezeCustomer(Long customerId, String operator);

    /**
     * 停用客户
     * 
     * @param customerId 客户ID
     * @param reason 停用原因
     * @param operator 操作人
     * @return 客户信息
     */
    CustomerResponse disableCustomer(Long customerId, String reason, String operator);

    /**
     * 更新客户风险等级
     * 
     * @param customerId 客户ID
     * @param riskLevel 风险等级
     * @param reason 变更原因
     * @param operator 操作人
     * @return 客户信息
     */
    CustomerResponse updateRiskLevel(Long customerId, String riskLevel, String reason, String operator);

    /**
     * 分页查询客户
     * 
     * @param request 查询请求
     * @return 分页结果
     */
    CustomerQueryResponse queryCustomers(CustomerQueryRequest request);

    /**
     * 获取统一客户视图
     * 
     * <p>整合展示客户基本信息、额度信息、使用记录等关键信息。</p>
     * 
     * @param customerId 客户ID
     * @return 统一客户视图
     */
    CustomerUnifiedViewResponse getUnifiedView(Long customerId);

    /**
     * 按名称模糊查询客户
     * 
     * @param customerName 客户名称
     * @return 客户列表
     */
    List<CustomerResponse> searchByName(String customerName);
}
