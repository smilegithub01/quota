package com.bank.quota.core.converter;

import com.bank.quota.core.domain.CustomerQuota;
import com.bank.quota.core.dto.customerquota.CustomerQuotaResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerQuotaConverter implements BaseConverter<CustomerQuota, CustomerQuotaResponse> {
    
    @Override
    public CustomerQuotaResponse toDto(CustomerQuota source) {
        if (source == null) {
            return null;
        }
        
        CustomerQuotaResponse response = new CustomerQuotaResponse();
        response.setId(source.getId());
        response.setCustomerId(source.getCustomerId());
        response.setCustomerName(source.getCustomerName());
        response.setCustomerType(source.getCustomerType() != null ? source.getCustomerType().name() : null);
        response.setGroupId(source.getGroupId());
        response.setTotalQuota(source.getTotalQuota());
        response.setUsedQuota(source.getUsedQuota());
        response.setAvailableQuota(source.getAvailableQuota());
        response.setStatus(source.getStatus() != null ? source.getStatus().name() : null);
        response.setDescription(source.getDescription());
        response.setCreateTime(source.getCreateTime());
        response.setUpdateTime(source.getUpdateTime());
        
        BigDecimal usageRate = BigDecimal.ZERO;
        if (source.getTotalQuota() != null && source.getTotalQuota().compareTo(BigDecimal.ZERO) > 0) {
            usageRate = source.getUsedQuota() != null 
                ? source.getUsedQuota().divide(source.getTotalQuota(), 4, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        }
        response.setUsageRate(usageRate);
        
        return response;
    }
    
    @Override
    public CustomerQuota toEntity(CustomerQuotaResponse target) {
        if (target == null) {
            return null;
        }
        
        return CustomerQuota.builder()
                .id(target.getId())
                .customerId(target.getCustomerId())
                .customerName(target.getCustomerName())
                .groupId(target.getGroupId())
                .totalQuota(target.getTotalQuota())
                .usedQuota(target.getUsedQuota())
                .availableQuota(target.getAvailableQuota())
                .description(target.getDescription())
                .build();
    }
    
    @Override
    public List<CustomerQuotaResponse> toDtoList(List<CustomerQuota> sourceList) {
        if (sourceList == null) {
            return null;
        }
        return sourceList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CustomerQuota> toEntityList(List<CustomerQuotaResponse> targetList) {
        if (targetList == null) {
            return null;
        }
        return targetList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
