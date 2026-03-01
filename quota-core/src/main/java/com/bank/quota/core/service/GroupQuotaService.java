package com.bank.quota.core.service;

import com.bank.quota.core.dto.groupquota.*;

import java.math.BigDecimal;
import java.util.List;

public interface GroupQuotaService {
    
    GroupQuotaResponse createGroupQuota(CreateGroupQuotaRequest request);
    
    GroupQuotaResponse updateGroupQuota(Long groupId, UpdateGroupQuotaRequest request);
    
    GroupQuotaResponse freezeGroupQuota(Long groupId, String reason, String operator);
    
    GroupQuotaResponse unfreezeGroupQuota(Long groupId, String operator);
    
    GroupQuotaResponse disableGroupQuota(Long groupId, String reason, String operator);
    
    GroupQuotaResponse getGroupQuota(Long groupId);
    
    List<GroupQuotaResponse> getAllGroupQuotas();
    
    List<GroupQuotaResponse> getEnabledGroupQuotas();
    
    GroupQuotaQueryResponse queryGroupQuotas(GroupQuotaQueryRequest request);
    
    GroupQuotaSubResponse createGroupQuotaSub(Long groupQuotaId, CreateGroupQuotaSubRequest request);
    
    GroupQuotaSubResponse updateGroupQuotaSub(Long subId, UpdateGroupQuotaSubRequest request);
    
    List<GroupQuotaSubResponse> getGroupQuotaSubs(Long groupQuotaId);
    
    GroupQuotaUsageResponse getGroupQuotaUsage(Long groupId);
    
    void adjustGroupQuota(Long groupId, BigDecimal adjustmentAmount, String reason, String operator);
}
