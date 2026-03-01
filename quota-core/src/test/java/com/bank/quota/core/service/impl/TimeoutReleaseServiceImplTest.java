package com.bank.quota.core.service.impl;

import com.bank.quota.core.config.QuotaTimeoutConfig;
import com.bank.quota.core.domain.ContractOccupancy;
import com.bank.quota.core.domain.CreditApplication;
import com.bank.quota.core.domain.UsageApplication;
import com.bank.quota.core.enums.ApprovalStatus;
import com.bank.quota.core.enums.UsageStatus;
import com.bank.quota.core.repository.ContractOccupancyRepository;
import com.bank.quota.core.repository.CreditApplicationRepository;
import com.bank.quota.core.repository.UsageApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeoutReleaseServiceImplTest {

    @Mock
    private QuotaTimeoutConfig quotaTimeoutConfig;

    @Mock
    private ContractOccupancyRepository contractOccupancyRepository;

    @Mock
    private CreditApplicationRepository creditApplicationRepository;

    @Mock
    private UsageApplicationRepository usageApplicationRepository;

    @InjectMocks
    private TimeoutReleaseServiceImpl timeoutReleaseService;

    private LocalDateTime pastTime;
    private LocalDateTime currentTime;

    @BeforeEach
    void setUp() {
        currentTime = LocalDateTime.now();
        pastTime = currentTime.minusMinutes(40); // 超时时间
    }

    @Test
    void testReleaseTimeoutOccupancies() {
        // 设置配置
        when(quotaTimeoutConfig.getOccupyLockTimeoutMinutes()).thenReturn(30);
        when(quotaTimeoutConfig.getAutoReleaseEnabled()).thenReturn(true);

        // 创建超时的占用记录
        ContractOccupancy timeoutOccupancy = ContractOccupancy.builder()
                .id(1L)
                .createTime(pastTime)
                .status(ContractOccupancy.OccupancyStatus.OCCUPIED)
                .occupancyAmount(BigDecimal.valueOf(1000))
                .contractNo("CONTRACT001")
                .build();

        when(contractOccupancyRepository.findTimeoutOccupancies(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(timeoutOccupancy));

        // 执行
        int result = timeoutReleaseService.releaseTimeoutOccupancies();

        // 验证
        assertEquals(1, result);
        verify(contractOccupancyRepository, times(1)).save(any(ContractOccupancy.class));
    }

    @Test
    void testReleaseTimeoutApplications_CreditApplication() {
        // 设置配置
        when(quotaTimeoutConfig.getApplicationTimeoutMinutes()).thenReturn(60);
        when(quotaTimeoutConfig.getAutoReleaseEnabled()).thenReturn(true);

        // 创建超时的授信申请
        CreditApplication timeoutApplication = CreditApplication.builder()
                .id(1L)
                .customerId(100L)
                .customerName("Test Customer")
                .appliedQuota(BigDecimal.valueOf(10000))
                .status(ApprovalStatus.SUBMITTED)
                .createTime(pastTime)
                .build();

        when(creditApplicationRepository.findTimeoutApplications(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(timeoutApplication));

        // 执行
        int result = timeoutReleaseService.releaseTimeoutApplications();

        // 验证
        assertEquals(1, result);
        verify(creditApplicationRepository, times(1)).save(any(CreditApplication.class));
    }

    @Test
    void testReleaseTimeoutApplications_UsageApplication() {
        // 设置配置
        when(quotaTimeoutConfig.getApplicationTimeoutMinutes()).thenReturn(60);
        when(quotaTimeoutConfig.getAutoReleaseEnabled()).thenReturn(true);

        // 创建超时的用信申请
        UsageApplication timeoutApplication = UsageApplication.builder()
                .id(1L)
                .customerId(100L)
                .customerName("Test Customer")
                .usageQuota(BigDecimal.valueOf(5000))
                .status(UsageStatus.IN_REVIEW)
                .createTime(pastTime)
                .build();

        when(usageApplicationRepository.findTimeoutApplications(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(timeoutApplication));

        // 执行
        int result = timeoutReleaseService.releaseTimeoutApplications();

        // 验证
        assertEquals(1, result);
        verify(usageApplicationRepository, times(1)).save(any(UsageApplication.class));
    }

    @Test
    void testReleaseTimeoutApplications_Multiple() {
        // 设置配置
        when(quotaTimeoutConfig.getApplicationTimeoutMinutes()).thenReturn(60);
        when(quotaTimeoutConfig.getAutoReleaseEnabled()).thenReturn(true);

        // 创建多个超时申请
        CreditApplication creditApp = CreditApplication.builder()
                .id(1L)
                .customerId(100L)
                .status(ApprovalStatus.SUBMITTED)
                .createTime(pastTime)
                .build();

        UsageApplication usageApp = UsageApplication.builder()
                .id(1L)
                .customerId(100L)
                .status(UsageStatus.IN_REVIEW)
                .createTime(pastTime)
                .build();

        when(creditApplicationRepository.findTimeoutApplications(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(creditApp));
        when(usageApplicationRepository.findTimeoutApplications(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(usageApp));

        // 执行
        int result = timeoutReleaseService.releaseTimeoutApplications();

        // 验证
        assertEquals(2, result); // 1个授信申请 + 1个用信申请
        verify(creditApplicationRepository, times(1)).save(any(CreditApplication.class));
        verify(usageApplicationRepository, times(1)).save(any(UsageApplication.class));
    }

    @Test
    void testManualReleaseTimeout() {
        // 设置配置
        when(quotaTimeoutConfig.getOccupyLockTimeoutMinutes()).thenReturn(30);
        when(quotaTimeoutConfig.getApplicationTimeoutMinutes()).thenReturn(60);
        when(quotaTimeoutConfig.getAutoReleaseEnabled()).thenReturn(true);

        // Mock各种查询结果
        ContractOccupancy timeoutOccupancy = ContractOccupancy.builder()
                .id(1L)
                .createTime(pastTime)
                .status(ContractOccupancy.OccupancyStatus.OCCUPIED)
                .build();

        CreditApplication timeoutCreditApp = CreditApplication.builder()
                .id(1L)
                .status(ApprovalStatus.SUBMITTED)
                .createTime(pastTime)
                .build();

        UsageApplication timeoutUsageApp = UsageApplication.builder()
                .id(1L)
                .status(UsageStatus.IN_REVIEW)
                .createTime(pastTime)
                .build();

        when(contractOccupancyRepository.findTimeoutOccupancies(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(timeoutOccupancy));
        when(creditApplicationRepository.findTimeoutApplications(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(timeoutCreditApp));
        when(usageApplicationRepository.findTimeoutApplications(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(timeoutUsageApp));

        // 执行
        var result = timeoutReleaseService.manualReleaseTimeout();

        // 验证
        assertTrue(result.isSuccess());
        assertEquals(1, result.getOccupanciesReleased());
        assertEquals(2, result.getApplicationsReleased()); // 1个授信 + 1个用信
        assertEquals(0, result.getApprovalsReleased());
        assertEquals(0, result.getExpiredDataCleaned());
    }

    @Test
    void testReleaseTimeoutOccupancies_WithZeroTimeout() {
        // 设置配置
        when(quotaTimeoutConfig.getOccupyLockTimeoutMinutes()).thenReturn(0);

        // 执行
        int result = timeoutReleaseService.releaseTimeoutOccupancies();

        // 验证
        assertEquals(0, result);
        verify(contractOccupancyRepository, never()).findTimeoutOccupancies(any(LocalDateTime.class));
    }
}