package com.bank.quota.core.service.impl;

import com.bank.quota.core.domain.QuotaUsageDetail;
import com.bank.quota.core.dto.quota.*;
import com.bank.quota.core.enums.BusinessType;
import com.bank.quota.core.enums.QuotaUsageType;
import com.bank.quota.core.repository.QuotaUsageDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotaUsageDetailServiceImplTest {

    @Mock
    private QuotaUsageDetailRepository quotaUsageDetailRepository;

    @InjectMocks
    private QuotaUsageDetailServiceImpl quotaUsageDetailService;

    private QuotaUsageDetail sampleDetail;

    @BeforeEach
    void setUp() {
        sampleDetail = QuotaUsageDetail.builder()
                .id(1L)
                .usageNo("QUD20260214000001")
                .customerId(100L)
                .customerName("Test Customer")
                .groupId(10L)
                .groupName("Test Group")
                .businessType(BusinessType.CREDIT_APPLICATION)
                .usageType(QuotaUsageType.OCCUPY)
                .usageAmount(BigDecimal.valueOf(10000))
                .currency("CNY")
                .originalBalance(BigDecimal.valueOf(50000))
                .currentBalance(BigDecimal.valueOf(40000))
                .balanceAfter(BigDecimal.valueOf(40000))
                .relatedId("REL001")
                .relatedType("LOAN_CONTRACT")
                .usageDate(LocalDateTime.now())
                .description("Test usage detail")
                .operatorId("OP001")
                .operatorName("Test Operator")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testQueryUsageDetails() {
        // 准备数据
        List<QuotaUsageDetail> details = Arrays.asList(sampleDetail);
        QuotaUsageDetailQueryRequest request = QuotaUsageDetailQueryRequest.builder()
                .customerId(100L)
                .build();

        when(quotaUsageDetailRepository.findByConditions(
                eq(100L), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(details);

        // 执行
        List<QuotaUsageDetailResponse> result = quotaUsageDetailService.queryUsageDetails(request);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("QUD20260214000001", result.get(0).getUsageNo());
        assertEquals("Test Customer", result.get(0).getCustomerName());
        verify(quotaUsageDetailRepository, times(1))
                .findByConditions(eq(100L), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testQueryUsageDetailsWithPaging() {
        // 准备数据
        List<QuotaUsageDetail> details = Arrays.asList(sampleDetail);
        QuotaUsageDetailQueryRequest request = QuotaUsageDetailQueryRequest.builder()
                .customerId(100L)
                .pageNum(1)
                .pageSize(10)
                .build();

        when(quotaUsageDetailRepository.findByConditionsWithPaging(
                eq(100L), any(), any(), any(), any(), any(), any(), any(), any(), eq(0), eq(10)))
                .thenReturn(details);

        // 执行
        List<QuotaUsageDetailResponse> result = quotaUsageDetailService.queryUsageDetailsWithPaging(request);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("QUD20260214000001", result.get(0).getUsageNo());
        verify(quotaUsageDetailRepository, times(1))
                .findByConditionsWithPaging(eq(100L), any(), any(), any(), any(), any(), any(), any(), any(), eq(0), eq(10));
    }

    @Test
    void testGetTotalUsageCount() {
        // 准备数据
        QuotaUsageDetailQueryRequest request = QuotaUsageDetailQueryRequest.builder()
                .customerId(100L)
                .build();

        when(quotaUsageDetailRepository.countByConditions(
                eq(100L), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(5L);

        // 执行
        Long result = quotaUsageDetailService.getTotalUsageCount(request);

        // 验证
        assertEquals(5L, result);
        verify(quotaUsageDetailRepository, times(1))
                .countByConditions(eq(100L), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetUsageStatistics() {
        // 准备数据
        QuotaUsageDetailQueryRequest request = QuotaUsageDetailQueryRequest.builder()
                .customerId(100L)
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now())
                .build();

        // Mock统计查询结果
        List<Object[]> usageTypeStats = new ArrayList<>();
        usageTypeStats.add(new Object[]{"OCCUPY", 3L, BigDecimal.valueOf(30000)});
        usageTypeStats.add(new Object[]{"RELEASE", 2L, BigDecimal.valueOf(10000)});

        List<Object[]> customerStats = new ArrayList<>();
        customerStats.add(new Object[]{100L, "Test Customer", 5L, BigDecimal.valueOf(40000)});

        // 使用 doReturn 语法绕过泛型类型检查
        Mockito.<List<Object[]>>doReturn(usageTypeStats).when(quotaUsageDetailRepository)
                .getUsageTypeStatistics(any(), any(), any(), any(), any());
        Mockito.<List<Object[]>>doReturn(customerStats).when(quotaUsageDetailRepository)
                .getCustomerStatistics(any(), any(), any(), any(), any());

        // 执行
        QuotaUsageStatisticsResponse result = quotaUsageDetailService.getUsageStatistics(request);

        // 验证
        assertNotNull(result);
        assertEquals(2, result.getUsageTypeSummaries().size());
        assertEquals(1, result.getCustomerSummaries().size());
        assertEquals("OCCUPY", result.getUsageTypeSummaries().get(0).getUsageType());
        assertEquals("Test Customer", result.getCustomerSummaries().get(0).getCustomerName());
    }

    @Test
    void testGetUsageDetailsByCustomer() {
        // 准备数据
        List<QuotaUsageDetail> details = Arrays.asList(sampleDetail);

        when(quotaUsageDetailRepository.findByCustomerIdWithPaging(eq(100L), eq(0), eq(10)))
                .thenReturn(details);

        // 执行
        List<QuotaUsageDetailResponse> result = quotaUsageDetailService
                .getUsageDetailsByCustomer(100L, 1, 10);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Customer", result.get(0).getCustomerName());
        verify(quotaUsageDetailRepository, times(1))
                .findByCustomerIdWithPaging(eq(100L), eq(0), eq(10));
    }

    @Test
    void testGetUsageDetailsByGroup() {
        // 准备数据
        List<QuotaUsageDetail> details = Arrays.asList(sampleDetail);

        when(quotaUsageDetailRepository.findByGroupIdWithPaging(eq(10L), eq(0), eq(10)))
                .thenReturn(details);

        // 执行
        List<QuotaUsageDetailResponse> result = quotaUsageDetailService
                .getUsageDetailsByGroup(10L, 1, 10);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Group", result.get(0).getGroupName());
        verify(quotaUsageDetailRepository, times(1))
                .findByGroupIdWithPaging(eq(10L), eq(0), eq(10));
    }

    @Test
    void testGetUsageDetailsByRelatedId() {
        // 准备数据
        List<QuotaUsageDetail> details = Arrays.asList(sampleDetail);

        when(quotaUsageDetailRepository.findByRelatedIdAndType(eq("REL001"), eq("LOAN_CONTRACT")))
                .thenReturn(details);

        // 执行
        List<QuotaUsageDetailResponse> result = quotaUsageDetailService
                .getUsageDetailsByRelatedId("REL001", "LOAN_CONTRACT");

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("REL001", result.get(0).getRelatedId());
        verify(quotaUsageDetailRepository, times(1))
                .findByRelatedIdAndType(eq("REL001"), eq("LOAN_CONTRACT"));
    }

    @Test
    void testCreateUsageDetail() {
        // Mock save 方法返回保存的对象
        when(quotaUsageDetailRepository.save(any(QuotaUsageDetail.class)))
                .thenAnswer(invocation -> {
                    QuotaUsageDetail detail = invocation.getArgument(0);
                    return detail;
                });

        // 执行
        QuotaUsageDetailResponse result = quotaUsageDetailService.createUsageDetail(sampleDetail);

        // 验证
        assertNotNull(result);
        assertEquals("QUD20260214000001", result.getUsageNo());
        assertEquals("Test Customer", result.getCustomerName());
        verify(quotaUsageDetailRepository, times(1)).save(any(QuotaUsageDetail.class));
    }
}