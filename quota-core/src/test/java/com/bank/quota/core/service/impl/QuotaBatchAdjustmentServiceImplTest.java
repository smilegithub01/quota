package com.bank.quota.core.service.impl;

import com.bank.quota.core.domain.QuotaBatchAdjustment;
import com.bank.quota.core.domain.QuotaBatchAdjustmentDetail;
import com.bank.quota.core.dto.quota.*;
import com.bank.quota.core.enums.AdjustmentStatus;
import com.bank.quota.core.enums.AdjustmentType;
import com.bank.quota.core.repository.QuotaBatchAdjustmentDetailRepository;
import com.bank.quota.core.repository.QuotaBatchAdjustmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotaBatchAdjustmentServiceImplTest {

    @Mock
    private QuotaBatchAdjustmentRepository quotaBatchAdjustmentRepository;

    @Mock
    private QuotaBatchAdjustmentDetailRepository quotaBatchAdjustmentDetailRepository;

    @Mock
    private com.bank.quota.core.service.QuotaControlService quotaControlService;

    @InjectMocks
    private QuotaBatchAdjustmentServiceImpl quotaBatchAdjustmentService;

    private QuotaBatchAdjustment sampleBatch;
    private QuotaBatchAdjustmentDetail sampleDetail;

    @BeforeEach
    void setUp() {
        sampleBatch = QuotaBatchAdjustment.builder()
                .id(1L)
                .batchNo("QBA20260214000001")
                .batchName("Test Batch Adjustment")
                .adjustmentType(AdjustmentType.INCREASE)
                .totalAmount(BigDecimal.valueOf(100000))
                .itemCount(2)
                .processedCount(0)
                .successCount(0)
                .failedCount(0)
                .status(AdjustmentStatus.PENDING)
                .executorId("ADMIN001")
                .executorName("Admin User")
                .description("Test batch adjustment")
                .createBy("ADMIN001")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        sampleDetail = QuotaBatchAdjustmentDetail.builder()
                .id(1L)
                .batchId(1L)
                .customerId(100L)
                .customerName("Test Customer")
                .adjustmentType(AdjustmentType.INCREASE)
                .adjustmentAmount(BigDecimal.valueOf(50000))
                .status(AdjustmentStatus.PENDING)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testInitiateBatchAdjustment() {
        // 准备数据
        QuotaBatchAdjustmentRequest request = QuotaBatchAdjustmentRequest.builder()
                .batchName("Test Batch Adjustment")
                .adjustmentType(AdjustmentType.INCREASE)
                .description("Test batch adjustment")
                .executorId("ADMIN001")
                .executorName("Admin User")
                .adjustmentItems(Arrays.asList(
                        QuotaBatchAdjustmentRequest.QuotaBatchAdjustmentItem.builder()
                                .customerId(100L)
                                .customerName("Customer 1")
                                .adjustmentAmount(BigDecimal.valueOf(50000))
                                .currency("CNY")
                                .build(),
                        QuotaBatchAdjustmentRequest.QuotaBatchAdjustmentItem.builder()
                                .customerId(101L)
                                .customerName("Customer 2")
                                .adjustmentAmount(BigDecimal.valueOf(30000))
                                .currency("CNY")
                                .build()
                ))
                .build();

        when(quotaBatchAdjustmentRepository.save(any(QuotaBatchAdjustment.class)))
                .thenReturn(sampleBatch);

        // 执行
        QuotaBatchAdjustmentResponse response = quotaBatchAdjustmentService.initiateBatchAdjustment(request);

        // 验证
        assertNotNull(response);
        assertEquals("Test Batch Adjustment", response.getBatchName());
        assertEquals(AdjustmentType.INCREASE, response.getAdjustmentType());
        assertEquals(BigDecimal.valueOf(80000), response.getTotalAmount()); // 50000 + 30000
        assertEquals(2, response.getItemCount());

        verify(quotaBatchAdjustmentRepository, times(1)).save(any(QuotaBatchAdjustment.class));
        verify(quotaBatchAdjustmentDetailRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGetBatchAdjustment() {
        // 准备数据
        when(quotaBatchAdjustmentRepository.findById(1L))
                .thenReturn(Optional.of(sampleBatch));

        // 执行
        QuotaBatchAdjustmentResponse response = quotaBatchAdjustmentService.getBatchAdjustment(1L);

        // 验证
        assertNotNull(response);
        assertEquals("QBA20260214000001", response.getBatchNo());
        assertEquals("Test Batch Adjustment", response.getBatchName());
        assertEquals(AdjustmentStatus.PENDING, response.getStatus());
    }

    @Test
    void testGetBatchAdjustmentNotFound() {
        // 准备数据
        when(quotaBatchAdjustmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        // 执行和验证
        assertThrows(RuntimeException.class, () -> {
            quotaBatchAdjustmentService.getBatchAdjustment(999L);
        });
    }

    @Test
    void testGetBatchAdjustmentDetails() {
        // 准备数据
        when(quotaBatchAdjustmentDetailRepository.findByBatchId(1L))
                .thenReturn(Arrays.asList(sampleDetail));

        // 执行
        List<QuotaBatchAdjustmentDetailResponse> details = 
                quotaBatchAdjustmentService.getBatchAdjustmentDetails(1L);

        // 验证
        assertNotNull(details);
        assertEquals(1, details.size());
        assertEquals("Test Customer", details.get(0).getCustomerName());
        assertEquals(AdjustmentType.INCREASE, details.get(0).getAdjustmentType());
    }

    @Test
    void testCancelBatchAdjustment() {
        // 准备数据
        when(quotaBatchAdjustmentRepository.findById(1L))
                .thenReturn(Optional.of(sampleBatch));
        when(quotaBatchAdjustmentRepository.save(any(QuotaBatchAdjustment.class)))
                .thenReturn(sampleBatch);

        // 执行
        boolean result = quotaBatchAdjustmentService.cancelBatchAdjustment(1L);

        // 验证
        assertTrue(result);

        ArgumentCaptor<QuotaBatchAdjustment> captor = ArgumentCaptor.forClass(QuotaBatchAdjustment.class);
        verify(quotaBatchAdjustmentRepository, times(1)).save(captor.capture());
        assertEquals(AdjustmentStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void testCancelBatchAdjustmentInvalidStatus() {
        // 准备数据 - 更改样本批次状态为处理中
        QuotaBatchAdjustment processingBatch = sampleBatch.toBuilder()
                .status(AdjustmentStatus.UNDER_REVIEW)
                .build();
        when(quotaBatchAdjustmentRepository.findById(1L))
                .thenReturn(Optional.of(processingBatch));

        // 执行
        boolean result = quotaBatchAdjustmentService.cancelBatchAdjustment(1L);

        // 验证
        assertFalse(result);
    }

    @Test
    void testExecuteBatchAdjustment() {
        // 准备数据
        when(quotaBatchAdjustmentRepository.findById(1L))
                .thenReturn(Optional.of(sampleBatch));
        when(quotaBatchAdjustmentRepository.save(any(QuotaBatchAdjustment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(quotaBatchAdjustmentDetailRepository.findByBatchId(1L))
                .thenReturn(Arrays.asList(sampleDetail));

        // 执行
        boolean result = quotaBatchAdjustmentService.executeBatchAdjustment(1L);

        // 验证
        assertTrue(result);

        ArgumentCaptor<QuotaBatchAdjustment> captor = ArgumentCaptor.forClass(QuotaBatchAdjustment.class);
        verify(quotaBatchAdjustmentRepository, atLeastOnce()).save(captor.capture());
        
        // 验证状态变化
        QuotaBatchAdjustment updatedBatch = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(AdjustmentStatus.UNDER_REVIEW, updatedBatch.getStatus());
    }

    @Test
    void testRetryFailedAdjustments() {
        // 准备数据
        when(quotaBatchAdjustmentRepository.findById(1L))
                .thenReturn(Optional.of(sampleBatch));
        when(quotaBatchAdjustmentRepository.save(any(QuotaBatchAdjustment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(quotaBatchAdjustmentDetailRepository.findByBatchId(1L))
                .thenReturn(Arrays.asList(sampleDetail));
        when(quotaBatchAdjustmentDetailRepository.findByBatchIdAndStatus(eq(1L), any(AdjustmentStatus.class)))
                .thenReturn(Arrays.asList(sampleDetail));

        // 执行
        boolean result = quotaBatchAdjustmentService.retryFailedAdjustments(1L);

        // 验证
        assertTrue(result);
        verify(quotaBatchAdjustmentRepository, atLeastOnce()).save(any(QuotaBatchAdjustment.class));
    }
}