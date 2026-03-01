package com.bank.quota.core.service.impl;

import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.core.domain.AuditLog;
import com.bank.quota.core.domain.CustomerQuota;
import com.bank.quota.core.dto.customerquota.*;
import com.bank.quota.core.enums.CustomerType;
import com.bank.quota.core.enums.QuotaStatus;
import com.bank.quota.core.repository.CustomerQuotaRepository;
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
class CustomerQuotaServiceImplTest {
    
    @Mock
    private CustomerQuotaRepository customerQuotaRepository;
    
    @Mock
    private AuditLogService auditLogService;
    
    @InjectMocks
    private CustomerQuotaServiceImpl customerQuotaService;
    
    private CustomerQuota testCustomerQuota;
    private CreateCustomerQuotaRequest createRequest;
    
    @BeforeEach
    void setUp() {
        testCustomerQuota = CustomerQuota.builder()
                .id(1L)
                .customerId(100L)
                .customerName("测试客户")
                .customerType(CustomerType.INDIVIDUAL)
                .groupId(10L)
                .totalQuota(new BigDecimal("100000.00"))
                .usedQuota(new BigDecimal("20000.00"))
                .availableQuota(new BigDecimal("80000.00"))
                .status(QuotaStatus.ENABLED)
                .description("测试客户额度")
                .createBy("admin")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        createRequest = new CreateCustomerQuotaRequest();
        createRequest.setCustomerId(100L);
        createRequest.setCustomerName("测试客户");
        createRequest.setCustomerType("INDIVIDUAL");
        createRequest.setGroupId(10L);
        createRequest.setTotalQuota(new BigDecimal("100000.00"));
        createRequest.setDescription("测试客户额度");
        createRequest.setCreateBy("admin");
    }
    
    @Test
    @DisplayName("创建客户额度成功")
    void createCustomerQuota_Success() {
        when(customerQuotaRepository.findByCustomerId(anyLong())).thenReturn(Optional.empty());
        when(customerQuotaRepository.save(any(CustomerQuota.class))).thenReturn(testCustomerQuota);
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        CustomerQuotaResponse response = customerQuotaService.createCustomerQuota(createRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo(100L);
        assertThat(response.getCustomerName()).isEqualTo("测试客户");
        assertThat(response.getTotalQuota()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(response.getStatus()).isEqualTo("ENABLED");
        
        verify(customerQuotaRepository).save(any(CustomerQuota.class));
    }
    
    @Test
    @DisplayName("创建客户额度 - 客户已存在，抛出异常")
    void createCustomerQuota_CustomerAlreadyExists_ThrowsException() {
        when(customerQuotaRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(testCustomerQuota));
        
        assertThatThrownBy(() -> customerQuotaService.createCustomerQuota(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("客户额度已存在");
        
        verify(customerQuotaRepository, never()).save(any(CustomerQuota.class));
    }
    
    @Test
    @DisplayName("冻结客户额度成功")
    void freezeCustomerQuota_Success() {
        when(customerQuotaRepository.findByCustomerIdWithLock(anyLong())).thenReturn(Optional.of(testCustomerQuota));
        when(customerQuotaRepository.save(any(CustomerQuota.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        CustomerQuotaResponse response = customerQuotaService.freezeCustomerQuota(100L, "风控冻结", "admin");
        
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("FROZEN");
        
        verify(customerQuotaRepository).save(any(CustomerQuota.class));
    }
    
    @Test
    @DisplayName("冻结客户额度 - 已冻结，抛出异常")
    void freezeCustomerQuota_AlreadyFrozen_ThrowsException() {
        testCustomerQuota.setStatus(QuotaStatus.FROZEN);
        when(customerQuotaRepository.findByCustomerIdWithLock(anyLong())).thenReturn(Optional.of(testCustomerQuota));
        
        assertThatThrownBy(() -> customerQuotaService.freezeCustomerQuota(100L, "风控冻结", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("客户额度已冻结");
    }
    
    @Test
    @DisplayName("解冻客户额度成功")
    void unfreezeCustomerQuota_Success() {
        testCustomerQuota.setStatus(QuotaStatus.FROZEN);
        when(customerQuotaRepository.findByCustomerIdWithLock(anyLong())).thenReturn(Optional.of(testCustomerQuota));
        when(customerQuotaRepository.save(any(CustomerQuota.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        CustomerQuotaResponse response = customerQuotaService.unfreezeCustomerQuota(100L, "admin");
        
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ENABLED");
        
        verify(customerQuotaRepository).save(any(CustomerQuota.class));
    }
    
    @Test
    @DisplayName("停用客户额度成功")
    void disableCustomerQuota_Success() {
        when(customerQuotaRepository.findByCustomerIdWithLock(anyLong())).thenReturn(Optional.of(testCustomerQuota));
        when(customerQuotaRepository.save(any(CustomerQuota.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        CustomerQuotaResponse response = customerQuotaService.disableCustomerQuota(100L, "客户注销", "admin");
        
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("DISABLED");
        
        verify(customerQuotaRepository).save(any(CustomerQuota.class));
    }
    
    @Test
    @DisplayName("查询客户额度详情成功")
    void getCustomerQuota_Success() {
        when(customerQuotaRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(testCustomerQuota));
        
        CustomerQuotaResponse response = customerQuotaService.getCustomerQuota(100L);
        
        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo(100L);
        assertThat(response.getCustomerName()).isEqualTo("测试客户");
    }
    
    @Test
    @DisplayName("查询客户额度 - 不存在，抛出异常")
    void getCustomerQuota_NotFound_ThrowsException() {
        when(customerQuotaRepository.findByCustomerId(anyLong())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> customerQuotaService.getCustomerQuota(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("客户额度不存在");
    }
    
    @Test
    @DisplayName("根据集团ID查询客户额度列表成功")
    void getCustomerQuotasByGroupId_Success() {
        when(customerQuotaRepository.findByGroupId(anyLong())).thenReturn(Arrays.asList(testCustomerQuota));
        
        List<CustomerQuotaResponse> responses = customerQuotaService.getCustomerQuotasByGroupId(10L);
        
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getGroupId()).isEqualTo(10L);
    }
    
    @Test
    @DisplayName("获取所有有效客户额度列表")
    void getEnabledCustomerQuotas_Success() {
        when(customerQuotaRepository.findAllEnabled()).thenReturn(Arrays.asList(testCustomerQuota));
        
        List<CustomerQuotaResponse> responses = customerQuotaService.getEnabledCustomerQuotas();
        
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo("ENABLED");
    }
    
    @Test
    @DisplayName("根据客户类型查询客户额度成功")
    void getCustomerQuotasByType_Success() {
        when(customerQuotaRepository.findByCustomerType(CustomerType.INDIVIDUAL))
                .thenReturn(Arrays.asList(testCustomerQuota));
        
        List<CustomerQuotaResponse> responses = customerQuotaService.getCustomerQuotasByType(
                CustomerType.INDIVIDUAL);
        
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCustomerType()).isEqualTo("INDIVIDUAL");
    }
    
    @Test
    @DisplayName("查询客户额度使用量成功")
    void getCustomerQuotaUsage_Success() {
        when(customerQuotaRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(testCustomerQuota));
        
        CustomerQuotaUsageResponse response = customerQuotaService.getCustomerQuotaUsage(100L);
        
        assertThat(response).isNotNull();
        assertThat(response.getTotalQuota()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(response.getUsedQuota()).isEqualByComparingTo(new BigDecimal("20000.00"));
        assertThat(response.getUsageRate()).isNotNull();
    }
    
    @Test
    @DisplayName("调整客户额度成功")
    void adjustCustomerQuota_Success() {
        BigDecimal adjustmentAmount = new BigDecimal("50000.00");
        when(customerQuotaRepository.findByCustomerIdWithLock(anyLong())).thenReturn(Optional.of(testCustomerQuota));
        when(customerQuotaRepository.save(any(CustomerQuota.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        customerQuotaService.adjustCustomerQuota(100L, adjustmentAmount, "额度调整", "admin");
        
        verify(customerQuotaRepository).save(any(CustomerQuota.class));
    }
    
    @Test
    @DisplayName("调整客户额度 - 减少额度超出可用额度，抛出异常")
    void adjustCustomerQuota_DecreaseExceedAvailable_ThrowsException() {
        BigDecimal adjustmentAmount = new BigDecimal("-100000.00");
        when(customerQuotaRepository.findByCustomerIdWithLock(anyLong())).thenReturn(Optional.of(testCustomerQuota));
        
        assertThatThrownBy(() -> customerQuotaService.adjustCustomerQuota(100L, adjustmentAmount, "额度调整", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("调整金额超出可用额度");
    }
    
    @Test
    @DisplayName("额度转移成功")
    void transferQuota_Success() {
        CustomerQuota fromQuota = CustomerQuota.builder()
                .id(1L)
                .customerId(100L)
                .totalQuota(new BigDecimal("100000.00"))
                .usedQuota(new BigDecimal("20000.00"))
                .availableQuota(new BigDecimal("80000.00"))
                .status(QuotaStatus.ENABLED)
                .build();
        
        CustomerQuota toQuota = CustomerQuota.builder()
                .id(2L)
                .customerId(200L)
                .totalQuota(new BigDecimal("50000.00"))
                .usedQuota(new BigDecimal("10000.00"))
                .availableQuota(new BigDecimal("40000.00"))
                .status(QuotaStatus.ENABLED)
                .build();
        
        when(customerQuotaRepository.findByCustomerIdWithLock(100L)).thenReturn(Optional.of(fromQuota));
        when(customerQuotaRepository.findByCustomerIdWithLock(200L)).thenReturn(Optional.of(toQuota));
        when(customerQuotaRepository.save(any(CustomerQuota.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogService.logOperation(any(), any(), anyString(), anyString(), anyString(), anyString())).thenReturn(mock(AuditLog.class));
        
        customerQuotaService.transferQuota(100L, 200L, new BigDecimal("30000.00"), "内部调拨", "admin");
        
        verify(customerQuotaRepository, times(2)).save(any(CustomerQuota.class));
    }
    
    @Test
    @DisplayName("额度转移 - 源客户额度不足，抛出异常")
    void transferQuota_InsufficientQuota_ThrowsException() {
        CustomerQuota fromQuota = CustomerQuota.builder()
                .id(1L)
                .customerId(100L)
                .totalQuota(new BigDecimal("100000.00"))
                .usedQuota(new BigDecimal("90000.00"))
                .availableQuota(new BigDecimal("10000.00"))
                .status(QuotaStatus.ENABLED)
                .build();
        
        CustomerQuota toQuota = CustomerQuota.builder()
                .id(2L)
                .customerId(200L)
                .totalQuota(new BigDecimal("50000.00"))
                .usedQuota(new BigDecimal("10000.00"))
                .availableQuota(new BigDecimal("40000.00"))
                .status(QuotaStatus.ENABLED)
                .build();
        
        when(customerQuotaRepository.findByCustomerIdWithLock(100L)).thenReturn(Optional.of(fromQuota));
        when(customerQuotaRepository.findByCustomerIdWithLock(200L)).thenReturn(Optional.of(toQuota));
        
        assertThatThrownBy(() -> 
                customerQuotaService.transferQuota(100L, 200L, new BigDecimal("30000.00"), "内部调拨", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("源客户可用额度不足");
    }
    
    @Test
    @DisplayName("额度转移 - 目标客户不存在，抛出异常")
    void transferQuota_TargetNotFound_ThrowsException() {
        CustomerQuota fromQuota = CustomerQuota.builder()
                .id(1L)
                .customerId(100L)
                .totalQuota(new BigDecimal("100000.00"))
                .usedQuota(new BigDecimal("20000.00"))
                .availableQuota(new BigDecimal("80000.00"))
                .status(QuotaStatus.ENABLED)
                .build();
        
        when(customerQuotaRepository.findByCustomerIdWithLock(100L)).thenReturn(Optional.of(fromQuota));
        when(customerQuotaRepository.findByCustomerIdWithLock(200L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> 
                customerQuotaService.transferQuota(100L, 200L, new BigDecimal("30000.00"), "内部调拨", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("目标客户额度不存在");
    }
}
