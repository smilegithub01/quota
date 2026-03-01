package com.bank.quota.core.converter;

import com.bank.quota.core.domain.GroupQuota;
import com.bank.quota.core.dto.groupquota.GroupQuotaResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupQuotaConverter implements BaseConverter<GroupQuota, GroupQuotaResponse> {
    
    @Override
    public GroupQuotaResponse toDto(GroupQuota source) {
        if (source == null) {
            return null;
        }
        
        GroupQuotaResponse response = new GroupQuotaResponse();
        response.setId(source.getId());
        response.setGroupId(source.getGroupId());
        response.setGroupName(source.getGroupName());
        response.setTotalQuota(source.getTotalQuota());
        response.setUsedQuota(source.getUsedQuota());
        response.setAvailableQuota(source.getAvailableQuota());
        response.setStatus(source.getStatus() != null ? source.getStatus().name() : null);
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
    public GroupQuota toEntity(GroupQuotaResponse target) {
        if (target == null) {
            return null;
        }
        
        return GroupQuota.builder()
                .id(target.getId())
                .groupId(target.getGroupId())
                .groupName(target.getGroupName())
                .totalQuota(target.getTotalQuota())
                .usedQuota(target.getUsedQuota())
                .availableQuota(target.getAvailableQuota())
                .build();
    }
    
    @Override
    public List<GroupQuotaResponse> toDtoList(List<GroupQuota> sourceList) {
        if (sourceList == null) {
            return null;
        }
        return sourceList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GroupQuota> toEntityList(List<GroupQuotaResponse> targetList) {
        if (targetList == null) {
            return null;
        }
        return targetList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
