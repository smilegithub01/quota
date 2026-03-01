package com.bank.quota.core.service.impl;

import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.core.domain.AuditLog;
import com.bank.quota.core.domain.GroupQuota;
import com.bank.quota.core.dto.groupquota.*;
import com.bank.quota.core.enums.QuotaStatus;
import com.bank.quota.core.repository.GroupQuotaRepository;
import com.bank.quota.core.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupQuotaServiceImplTest {
    
    @Mock
    private GroupQuotaRepository groupQuotaRepository;
    
    @Mock
    private AuditLogService auditLogService;
    
    @InjectMocks
    private GroupQuotaServiceImpl groupQuotaService;
    
    private GroupQuota testGroupQuota;
    private CreateGroupQuotaRequest createRequest;
    private UpdateGroupQuotaRequest updateRequest;
    
    @BeforeEach
    void setUp() {
        testGroupQuota = GroupQuota.builder()
                .id(1L)
                .groupId(100L)
                .groupName("测试集团")
                .totalQuota(new BigDecimal("1000000.00"))
                .usedQuota(new BigDecimal("200000.00"))
                .availableQuota(new BigDecimal("800000.00"))
                .status(QuotaStatus.ENABLED)
                .createBy("admin")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        createRequest = new CreateGroupQuotaRequest();
        createRequest.setGroupId(100L);
        createRequest.setGroupName("测试集团");
        createRequest.setTotalQuota(new BigDecimal("1000000.00"));
        createRequest.setDescription("测试集团额度");
        createRequest.setCreateBy("admin");
        
        updateRequest = new UpdateGroupQuotaRequest();
        updateRequest.setTotalQuota(new BigDecimal("1500000.00"));
        updateRequest.setDescription("更新后的集团额度");
        updateRequest.setUpdateBy("admin");
    }
    
    @Test
    @DisplayName("创建集团额度成功")
    void createGroupQuota_Success() {
        when(groupQuotaRepository.findByGroupId(anyLong())).thenReturn(Optional.empty());
        when(groupQuotaRepository.save(any(GroupQuota.class))).thenReturn(testGroupQuota);
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        GroupQuotaResponse response = groupQuotaService.createGroupQuota(createRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getGroupId()).isEqualTo(100L);
        assertThat(response.getGroupName()).isEqualTo("测试集团");
        assertThat(response.getTotalQuota()).isEqualByComparingTo(new BigDecimal("1000000.00"));
        assertThat(response.getStatus()).isEqualTo("ENABLED");
        
        verify(groupQuotaRepository).save(any(GroupQuota.class));
        verify(auditLogService).logOperation(
                eq(AuditLog.OperationType.QUOTA_CREATE),
                eq(AuditLog.AuditObjectType.GROUP_QUOTA),
                anyString(),
                anyString(),
                eq("admin"),
                eq("SUCCESS"));
    }
    
    @Test
    @DisplayName("创建集团额度 - 集团已存在，抛出异常")
    void createGroupQuota_GroupAlreadyExists_ThrowsException() {
        when(groupQuotaRepository.findByGroupId(anyLong())).thenReturn(Optional.of(testGroupQuota));
        
        assertThatThrownBy(() -> groupQuotaService.createGroupQuota(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("集团限额已存在");
        
        verify(groupQuotaRepository, never()).save(any(GroupQuota.class));
    }
    
    @Test
    @DisplayName("更新集团额度成功")
    void updateGroupQuota_Success() {
        when(groupQuotaRepository.findByGroupIdWithLock(anyLong())).thenReturn(Optional.of(testGroupQuota));
        when(groupQuotaRepository.save(any(GroupQuota.class))).thenAnswer(invocation -> {
            GroupQuota saved = invocation.getArgument(0);
            saved.setUpdateTime(LocalDateTime.now());
            return saved;
        });
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        GroupQuotaResponse response = groupQuotaService.updateGroupQuota(100L, updateRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getTotalQuota()).isEqualByComparingTo(new BigDecimal("1500000.00"));
        
        verify(groupQuotaRepository).save(any(GroupQuota.class));
    }
    
    @Test
    @DisplayName("更新集团额度 - 集团不存在，抛出异常")
    void updateGroupQuota_NotFound_ThrowsException() {
        when(groupQuotaRepository.findByGroupIdWithLock(anyLong())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> groupQuotaService.updateGroupQuota(999L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("集团限额不存在");
    }
    
    @Test
    @DisplayName("冻结集团额度成功")
    void freezeGroupQuota_Success() {
        when(groupQuotaRepository.findByGroupIdWithLock(anyLong())).thenReturn(Optional.of(testGroupQuota));
        when(groupQuotaRepository.save(any(GroupQuota.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        GroupQuotaResponse response = groupQuotaService.freezeGroupQuota(100L, "风控冻结", "admin");
        
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("FROZEN");
        
        verify(groupQuotaRepository).save(any(GroupQuota.class));
    }
    
    @Test
    @DisplayName("冻结集团额度 - 已冻结，抛出异常")
    void freezeGroupQuota_AlreadyFrozen_ThrowsException() {
        testGroupQuota.setStatus(QuotaStatus.FROZEN);
        when(groupQuotaRepository.findByGroupIdWithLock(anyLong())).thenReturn(Optional.of(testGroupQuota));
        
        assertThatThrownBy(() -> groupQuotaService.freezeGroupQuota(100L, "风控冻结", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("集团限额已冻结");
    }
    
    @Test
    @DisplayName("解冻集团额度成功")
    void unfreezeGroupQuota_Success() {
        testGroupQuota.setStatus(QuotaStatus.FROZEN);
        when(groupQuotaRepository.findByGroupIdWithLock(anyLong())).thenReturn(Optional.of(testGroupQuota));
        when(groupQuotaRepository.save(any(GroupQuota.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        GroupQuotaResponse response = groupQuotaService.unfreezeGroupQuota(100L, "admin");
        
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ENABLED");
        
        verify(groupQuotaRepository).save(any(GroupQuota.class));
    }
    
    @Test
    @DisplayName("查询集团额度详情成功")
    void getGroupQuota_Success() {
        when(groupQuotaRepository.findByGroupId(anyLong())).thenReturn(Optional.of(testGroupQuota));
        
        GroupQuotaResponse response = groupQuotaService.getGroupQuota(100L);
        
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getGroupId()).isEqualTo(100L);
    }
    
    @Test
    @DisplayName("查询集团额度 - 不存在，抛出异常")
    void getGroupQuota_NotFound_ThrowsException() {
        when(groupQuotaRepository.findByGroupId(anyLong())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> groupQuotaService.getGroupQuota(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("集团限额不存在");
    }
    
    @Test
    @DisplayName("获取所有有效集团额度列表")
    void getEnabledGroupQuotas_Success() {
        when(groupQuotaRepository.findAllEnabled()).thenReturn(Arrays.asList(testGroupQuota));
        
        List<GroupQuotaResponse> responses = groupQuotaService.getEnabledGroupQuotas();
        
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo("ENABLED");
    }
    
    @Test
    @DisplayName("调整集团额度 - 增加额度成功")
    void adjustGroupQuota_Increase_Success() {
        BigDecimal adjustmentAmount = new BigDecimal("100000.00");
        when(groupQuotaRepository.findByGroupIdWithLock(anyLong())).thenReturn(Optional.of(testGroupQuota));
        when(groupQuotaRepository.save(any(GroupQuota.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        groupQuotaService.adjustGroupQuota(100L, adjustmentAmount, "额度调整", "admin");
        
        verify(groupQuotaRepository).save(any(GroupQuota.class));
        verify(auditLogService).logOperation(
                eq(AuditLog.OperationType.QUOTA_UPDATE),
                eq(AuditLog.AuditObjectType.GROUP_QUOTA),
                anyString(),
                anyString(),
                eq("admin"),
                eq("SUCCESS"));
    }
    
    @Test
    @DisplayName("调整集团额度 - 减少额度超出可用额度，抛出异常")
    void adjustGroupQuota_DecreaseExceedAvailable_ThrowsException() {
        BigDecimal adjustmentAmount = new BigDecimal("-1000000.00");
        when(groupQuotaRepository.findByGroupIdWithLock(anyLong())).thenReturn(Optional.of(testGroupQuota));
        
        assertThatThrownBy(() -> groupQuotaService.adjustGroupQuota(100L, adjustmentAmount, "额度调整", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("调整金额超出可用额度");
    }
    
    @Test
    @DisplayName("查询集团额度使用量成功")
    void getGroupQuotaUsage_Success() {
        when(groupQuotaRepository.findByGroupId(anyLong())).thenReturn(Optional.of(testGroupQuota));
        
        GroupQuotaUsageResponse response = groupQuotaService.getGroupQuotaUsage(100L);
        
        assertThat(response).isNotNull();
        assertThat(response.getTotalQuota()).isEqualByComparingTo(new BigDecimal("1000000.00"));
        assertThat(response.getUsedQuota()).isEqualByComparingTo(new BigDecimal("200000.00"));
        assertThat(response.getUsageRate()).isNotNull();
    }
    
    @Test
    @DisplayName("检查集团额度使用率预警")
    void checkUsageRateWarning_Success() {
        testGroupQuota.setUsedQuota(new BigDecimal("900000.00"));
        when(groupQuotaRepository.findByGroupId(anyLong())).thenReturn(Optional.of(testGroupQuota));
        
        GroupQuotaUsageResponse response = groupQuotaService.getGroupQuotaUsage(100L);
        
        assertThat(response).isNotNull();
        assertThat(response.getUsageRate()).isGreaterThan(new BigDecimal("0.8"));
    }
}
