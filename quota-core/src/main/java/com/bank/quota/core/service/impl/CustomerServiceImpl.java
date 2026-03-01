package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.*;
import com.bank.quota.core.dto.customer.*;
import com.bank.quota.core.repository.*;
import com.bank.quota.core.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户服务实现类
 * 
 * <p>提供客户基础信息的业务操作实现。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerQuotaRepository customerQuotaRepository;
    private final WhitelistRepository whitelistRepository;
    private final AuditLogRepository auditLogRepository;
    private final GroupQuotaRepository groupQuotaRepository;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer: customerName={}, customerType={}, category={}", 
                request.getCustomerName(), request.getCustomerType(), request.getCategory());

        if (request.getIdNumber() != null && customerRepository.existsByIdNumber(request.getIdNumber())) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                    "证件号码已存在: " + request.getIdNumber());
        }

        if (Customer.CustomerCategory.GROUP_CUSTOMER.name().equals(request.getCategory())) {
            if (request.getGroupId() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                        "集团客户必须指定所属集团");
            }
            if (!groupQuotaRepository.findByGroupId(request.getGroupId()).isPresent()) {
                throw new BusinessException(ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团限额不存在: groupId=" + request.getGroupId());
            }
        }

        Customer customer = Customer.builder()
                .customerNo(generateCustomerNo())
                .customerName(request.getCustomerName())
                .customerType(Customer.CustomerType.valueOf(request.getCustomerType()))
                .category(Customer.CustomerCategory.valueOf(request.getCategory()))
                .groupId(request.getGroupId())
                .groupName(request.getGroupName())
                .idType(request.getIdType() != null ? Customer.IdType.valueOf(request.getIdType()) : null)
                .idNumber(request.getIdNumber())
                .legalPerson(request.getLegalPerson())
                .registeredAddress(request.getRegisteredAddress())
                .businessScope(request.getBusinessScope())
                .registeredCapital(request.getRegisteredCapital())
                .establishDate(request.getEstablishDate())
                .industryCode(request.getIndustryCode())
                .industryName(request.getIndustryName())
                .riskLevel(request.getRiskLevel() != null ? 
                        Customer.RiskLevel.valueOf(request.getRiskLevel()) : Customer.RiskLevel.R3)
                .creditRating(request.getCreditRating())
                .creditScore(request.getCreditScore())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .description(request.getDescription())
                .createBy(request.getCreateBy())
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Customer created successfully: id={}, customerNo={}", 
                saved.getId(), saved.getCustomerNo());

        return buildCustomerResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request) {
        log.info("Updating customer: customerId={}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户不存在: " + customerId));

        if (request.getCustomerName() != null) {
            customer.setCustomerName(request.getCustomerName());
        }
        if (request.getLegalPerson() != null) {
            customer.setLegalPerson(request.getLegalPerson());
        }
        if (request.getRegisteredAddress() != null) {
            customer.setRegisteredAddress(request.getRegisteredAddress());
        }
        if (request.getBusinessScope() != null) {
            customer.setBusinessScope(request.getBusinessScope());
        }
        if (request.getRegisteredCapital() != null) {
            customer.setRegisteredCapital(request.getRegisteredCapital());
        }
        if (request.getIndustryCode() != null) {
            customer.setIndustryCode(request.getIndustryCode());
        }
        if (request.getIndustryName() != null) {
            customer.setIndustryName(request.getIndustryName());
        }
        if (request.getCreditRating() != null) {
            customer.setCreditRating(request.getCreditRating());
        }
        if (request.getCreditScore() != null) {
            customer.setCreditScore(request.getCreditScore());
        }
        if (request.getContactPerson() != null) {
            customer.setContactPerson(request.getContactPerson());
        }
        if (request.getContactPhone() != null) {
            customer.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            customer.setContactEmail(request.getContactEmail());
        }
        if (request.getEffectiveDate() != null) {
            customer.setEffectiveDate(request.getEffectiveDate());
        }
        if (request.getExpiryDate() != null) {
            customer.setExpiryDate(request.getExpiryDate());
        }
        if (request.getDescription() != null) {
            customer.setDescription(request.getDescription());
        }
        if (request.getUpdateBy() != null) {
            customer.setUpdateBy(request.getUpdateBy());
        }

        Customer saved = customerRepository.save(customer);
        log.info("Customer updated successfully: customerId={}", customerId);

        return buildCustomerResponse(saved);
    }

    @Override
    public CustomerResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户不存在: " + customerId));
        return buildCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByNo(String customerNo) {
        Customer customer = customerRepository.findByCustomerNo(customerNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户不存在: " + customerNo));
        return buildCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByIdNumber(String idNumber) {
        Customer customer = customerRepository.findByIdNumber(idNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户不存在: " + idNumber));
        return buildCustomerResponse(customer);
    }

    @Override
    public List<CustomerResponse> getCustomersByGroupId(Long groupId) {
        return customerRepository.findByGroupIdAndStatus(groupId, Customer.CustomerStatus.ACTIVE)
                .stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> getCustomersByType(String customerType) {
        return customerRepository.findByCustomerType(Customer.CustomerType.valueOf(customerType))
                .stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> getCustomersByCategory(String category) {
        return customerRepository.findByCategory(Customer.CustomerCategory.valueOf(category))
                .stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> getCustomersByRiskLevel(String riskLevel) {
        return customerRepository.findByRiskLevel(Customer.RiskLevel.valueOf(riskLevel))
                .stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerResponse freezeCustomer(Long customerId, String reason, String operator) {
        log.info("Freezing customer: customerId={}, reason={}", customerId, reason);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户不存在: " + customerId));

        if (customer.getStatus() == Customer.CustomerStatus.FROZEN) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "客户已处于冻结状态");
        }

        customer.setStatus(Customer.CustomerStatus.FROZEN);
        customer.setUpdateBy(operator);
        Customer saved = customerRepository.save(customer);

        log.info("Customer frozen successfully: customerId={}", customerId);
        return buildCustomerResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse unfreezeCustomer(Long customerId, String operator) {
        log.info("Unfreezing customer: customerId={}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户不存在: " + customerId));

        if (customer.getStatus() != Customer.CustomerStatus.FROZEN) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "客户未处于冻结状态");
        }

        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customer.setUpdateBy(operator);
        Customer saved = customerRepository.save(customer);

        log.info("Customer unfrozen successfully: customerId={}", customerId);
        return buildCustomerResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse disableCustomer(Long customerId, String reason, String operator) {
        log.info("Disabling customer: customerId={}, reason={}", customerId, reason);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户不存在: " + customerId));

        customer.setStatus(Customer.CustomerStatus.DISABLED);
        customer.setUpdateBy(operator);
        Customer saved = customerRepository.save(customer);

        log.info("Customer disabled successfully: customerId={}", customerId);
        return buildCustomerResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse updateRiskLevel(Long customerId, String riskLevel, String reason, String operator) {
        log.info("Updating customer risk level: customerId={}, riskLevel={}", customerId, riskLevel);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户不存在: " + customerId));

        customer.setRiskLevel(Customer.RiskLevel.valueOf(riskLevel));
        customer.setUpdateBy(operator);
        Customer saved = customerRepository.save(customer);

        log.info("Customer risk level updated successfully: customerId={}, riskLevel={}", 
                customerId, riskLevel);
        return buildCustomerResponse(saved);
    }

    @Override
    public CustomerQueryResponse queryCustomers(CustomerQueryRequest request) {
        log.info("Querying customers: pageNum={}, pageSize={}", request.getPageNum(), request.getPageSize());

        Pageable pageable = PageRequest.of(
                request.getPageNum() - 1, 
                request.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "createTime"));

        Page<Customer> page = customerRepository.findAll(pageable);

        CustomerQueryResponse response = new CustomerQueryResponse();
        response.setTotal(page.getTotalElements());
        response.setPageNum(request.getPageNum());
        response.setPageSize(request.getPageSize());
        response.setPages(page.getTotalPages());
        response.setList(page.getContent().stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    public CustomerUnifiedViewResponse getUnifiedView(Long customerId) {
        log.info("Getting unified customer view: customerId={}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户不存在: " + customerId));

        CustomerUnifiedViewResponse response = new CustomerUnifiedViewResponse();

        response.setCustomerInfo(buildCustomerBasicInfo(customer));

        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerId(customerId)
                .orElse(null);
        response.setQuotaOverview(buildQuotaOverview(customerQuota));

        response.setRiskInfo(buildRiskInfo(customer));

        List<Whitelist> whitelists = whitelistRepository.findActiveByCustomerId(customerId, LocalDateTime.now());
        response.setWhitelistStatus(buildWhitelistStatus(whitelists));

        List<AuditLog> auditLogs = auditLogRepository.findByObjectIdOrderByCreateTimeDesc(customerId.toString());
        response.setRecentUsageRecords(buildRecentUsageRecords(auditLogs));

        return response;
    }

    @Override
    public List<CustomerResponse> searchByName(String customerName) {
        return customerRepository.findByCustomerNameContaining(customerName)
                .stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList());
    }

    private String generateCustomerNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = System.currentTimeMillis() % 100000;
        return String.format("CUS%s%05d", dateStr, sequence);
    }

    private CustomerResponse buildCustomerResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setCustomerNo(customer.getCustomerNo());
        response.setCustomerName(customer.getCustomerName());
        response.setCustomerType(customer.getCustomerType().name());
        response.setCustomerTypeDesc(customer.getCustomerType().getDescription());
        response.setCategory(customer.getCategory().name());
        response.setCategoryDesc(customer.getCategory().getDescription());
        response.setGroupId(customer.getGroupId());
        response.setGroupName(customer.getGroupName());
        response.setIdType(customer.getIdType() != null ? customer.getIdType().name() : null);
        response.setIdTypeDesc(customer.getIdType() != null ? customer.getIdType().getDescription() : null);
        response.setIdNumber(customer.getIdNumber());
        response.setLegalPerson(customer.getLegalPerson());
        response.setRegisteredAddress(customer.getRegisteredAddress());
        response.setBusinessScope(customer.getBusinessScope());
        response.setRegisteredCapital(customer.getRegisteredCapital());
        response.setEstablishDate(customer.getEstablishDate());
        response.setIndustryCode(customer.getIndustryCode());
        response.setIndustryName(customer.getIndustryName());
        response.setRiskLevel(customer.getRiskLevel().name());
        response.setRiskLevelDesc(customer.getRiskLevel().getDescription());
        response.setCreditRating(customer.getCreditRating());
        response.setCreditScore(customer.getCreditScore());
        response.setContactPerson(customer.getContactPerson());
        response.setContactPhone(customer.getContactPhone());
        response.setContactEmail(customer.getContactEmail());
        response.setStatus(customer.getStatus().name());
        response.setStatusDesc(customer.getStatus().getDescription());
        response.setEffectiveDate(customer.getEffectiveDate());
        response.setExpiryDate(customer.getExpiryDate());
        response.setDescription(customer.getDescription());
        response.setCreateBy(customer.getCreateBy());
        response.setUpdateBy(customer.getUpdateBy());
        response.setCreateTime(customer.getCreateTime());
        response.setUpdateTime(customer.getUpdateTime());
        return response;
    }

    private CustomerUnifiedViewResponse.CustomerBasicInfo buildCustomerBasicInfo(Customer customer) {
        CustomerUnifiedViewResponse.CustomerBasicInfo info = new CustomerUnifiedViewResponse.CustomerBasicInfo();
        info.setCustomerId(customer.getId());
        info.setCustomerNo(customer.getCustomerNo());
        info.setCustomerName(customer.getCustomerName());
        info.setCustomerType(customer.getCustomerType().name());
        info.setCustomerTypeDesc(customer.getCustomerType().getDescription());
        info.setCategory(customer.getCategory().name());
        info.setCategoryDesc(customer.getCategory().getDescription());
        info.setGroupId(customer.getGroupId());
        info.setGroupName(customer.getGroupName());
        info.setIndustryName(customer.getIndustryName());
        info.setStatus(customer.getStatus().name());
        info.setStatusDesc(customer.getStatus().getDescription());
        info.setContactPerson(customer.getContactPerson());
        info.setContactPhone(customer.getContactPhone());
        info.setCreateTime(customer.getCreateTime());
        return info;
    }

    private CustomerUnifiedViewResponse.QuotaOverview buildQuotaOverview(CustomerQuota quota) {
        CustomerUnifiedViewResponse.QuotaOverview overview = new CustomerUnifiedViewResponse.QuotaOverview();
        if (quota != null) {
            overview.setTotalQuota(quota.getTotalQuota());
            overview.setUsedQuota(quota.getUsedQuota());
            overview.setLockedQuota(quota.getLockedQuota() != null ? quota.getLockedQuota() : BigDecimal.ZERO);
            overview.setAvailableQuota(quota.getAvailableQuota());
            
            if (quota.getTotalQuota().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal usageRate = quota.getUsedQuota()
                        .divide(quota.getTotalQuota(), 4, RoundingMode.HALF_EVEN)
                        .multiply(BigDecimal.valueOf(100));
                overview.setUsageRate(usageRate);
            } else {
                overview.setUsageRate(BigDecimal.ZERO);
            }
            
            overview.setQuotaStatus(quota.getStatus().name());
            overview.setQuotaStatusDesc(quota.getStatus().getDescription());
        } else {
            overview.setTotalQuota(BigDecimal.ZERO);
            overview.setUsedQuota(BigDecimal.ZERO);
            overview.setLockedQuota(BigDecimal.ZERO);
            overview.setAvailableQuota(BigDecimal.ZERO);
            overview.setUsageRate(BigDecimal.ZERO);
            overview.setQuotaStatus("N/A");
            overview.setQuotaStatusDesc("未配置额度");
        }
        return overview;
    }

    private CustomerUnifiedViewResponse.RiskInfo buildRiskInfo(Customer customer) {
        CustomerUnifiedViewResponse.RiskInfo info = new CustomerUnifiedViewResponse.RiskInfo();
        info.setRiskLevel(customer.getRiskLevel().name());
        info.setRiskLevelDesc(customer.getRiskLevel().getDescription());
        info.setCreditRating(customer.getCreditRating());
        info.setCreditScore(customer.getCreditScore());
        info.setWarningStatus("NORMAL");
        info.setWarningMessage(null);
        return info;
    }

    private CustomerUnifiedViewResponse.WhitelistStatus buildWhitelistStatus(List<Whitelist> whitelists) {
        CustomerUnifiedViewResponse.WhitelistStatus status = new CustomerUnifiedViewResponse.WhitelistStatus();
        if (!whitelists.isEmpty()) {
            Whitelist whitelist = whitelists.get(0);
            status.setInWhitelist(true);
            status.setWhitelistType(whitelist.getWhitelistType().name());
            status.setWhitelistNo(whitelist.getWhitelistNo());
            status.setExemptRule(whitelist.getExemptRule().name());
            status.setEffectiveTime(whitelist.getEffectiveTime());
            status.setExpiryTime(whitelist.getExpiryTime());
        } else {
            status.setInWhitelist(false);
        }
        return status;
    }

    private List<CustomerUnifiedViewResponse.QuotaUsageRecord> buildRecentUsageRecords(List<AuditLog> auditLogs) {
        return auditLogs.stream()
                .limit(10)
                .map(log -> {
                    CustomerUnifiedViewResponse.QuotaUsageRecord record = new CustomerUnifiedViewResponse.QuotaUsageRecord();
                    record.setRecordId(log.getId());
                    record.setOperationType(log.getOperationType().name());
                    record.setOperationTypeDesc(log.getOperationDesc());
                    record.setOperationTime(log.getCreateTime());
                    record.setOperator(log.getOperator());
                    record.setRemark(log.getOperationDesc());
                    return record;
                })
                .collect(Collectors.toList());
    }
}
