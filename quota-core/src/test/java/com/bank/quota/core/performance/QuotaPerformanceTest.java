package com.bank.quota.core.performance;

import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.dto.groupquota.CreateGroupQuotaRequest;
import com.bank.quota.core.dto.groupquota.GroupQuotaResponse;
import com.bank.quota.core.repository.GroupQuotaRepository;
import com.bank.quota.core.service.GroupQuotaService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuotaPerformanceTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private GroupQuotaService groupQuotaService;
    
    private String baseUrl;
    private List<Long> createdGroupIds = new ArrayList<>();
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
    }
    
    @Test
    @Order(1)
    @DisplayName("性能测试 - 并发创建集团额度")
    void testConcurrentGroupQuotaCreation() throws InterruptedException {
        int threadCount = 10;
        int quotasPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < quotasPerThread; j++) {
                        try {
                            CreateGroupQuotaRequest request = new CreateGroupQuotaRequest();
                            request.setGroupId(System.currentTimeMillis() + threadId * 1000 + j);
                            request.setGroupName("性能测试集团-" + threadId + "-" + j);
                            request.setTotalQuota(new BigDecimal("1000000.00"));
                            request.setDescription("性能测试");
                            request.setCreateBy("performance-test");
                            
                            GroupQuotaResponse response = groupQuotaService.createGroupQuota(request);
                            if (response != null) {
                                successCount.incrementAndGet();
                                createdGroupIds.add(response.getId());
                            }
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(300, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalCreated = successCount.get();
        
        System.out.println("=== 并发创建集团额度性能测试结果 ===");
        System.out.println("线程数: " + threadCount);
        System.out.println("每线程创建数量: " + quotasPerThread);
        System.out.println("成功创建: " + totalCreated);
        System.out.println("失败数量: " + failCount.get());
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均每条创建时间: " + (totalTime / (double) totalCreated) + "ms");
        System.out.println("每秒创建数量: " + (totalCreated * 1000.0 / totalTime));
        
        assertThat(totalCreated).isGreaterThan(0);
        assertThat(totalTime).isLessThan(120000);
    }
    
    @Test
    @Order(2)
    @DisplayName("性能测试 - 并发查询集团额度")
    void testConcurrentGroupQuotaQuery() throws InterruptedException {
        int threadCount = 20;
        int queriesPerThread = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < queriesPerThread; j++) {
                        try {
                            if (!createdGroupIds.isEmpty()) {
                                Long groupId = createdGroupIds.get(j % Math.min(createdGroupIds.size(), 100));
                                GroupQuotaResponse response = groupQuotaService.getGroupQuota(groupId);
                                if (response != null) {
                                    successCount.incrementAndGet();
                                }
                            }
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(300, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalQueries = successCount.get();
        
        System.out.println("=== 并发查询集团额度性能测试结果 ===");
        System.out.println("线程数: " + threadCount);
        System.out.println("每线程查询数量: " + queriesPerThread);
        System.out.println("成功查询: " + totalQueries);
        System.out.println("失败数量: " + failCount.get());
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均每条查询时间: " + (totalTime / (double) totalQueries) + "ms");
        System.out.println("每秒查询数量: " + (totalQueries * 1000.0 / totalTime));
        
        assertThat(totalQueries).isGreaterThan(0);
        assertThat(totalTime).isLessThan(180000);
    }
    
    @Test
    @Order(3)
    @DisplayName("性能测试 - 并发额度调整")
    void testConcurrentQuotaAdjustment() throws InterruptedException {
        if (createdGroupIds.isEmpty()) {
            System.out.println("跳过测试: 没有可用的集团ID");
            return;
        }
        
        int threadCount = 10;
        int adjustmentsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < adjustmentsPerThread; j++) {
                        try {
                            Long groupId = createdGroupIds.get(threadId % createdGroupIds.size());
                            BigDecimal adjustment = new BigDecimal("1000.00");
                            groupQuotaService.adjustGroupQuota(groupId, adjustment, "性能测试调整", "performance-test");
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(300, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalAdjustments = successCount.get();
        
        System.out.println("=== 并发额度调整性能测试结果 ===");
        System.out.println("线程数: " + threadCount);
        System.out.println("每线程调整次数: " + adjustmentsPerThread);
        System.out.println("成功调整: " + totalAdjustments);
        System.out.println("失败数量: " + failCount.get());
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均每次调整时间: " + (totalTime / (double) totalAdjustments) + "ms");
        System.out.println("每秒调整次数: " + (totalAdjustments * 1000.0 / totalTime));
        
        assertThat(totalAdjustments).isGreaterThan(0);
    }
    
    @Test
    @Order(4)
    @DisplayName("性能测试 - 大数据量查询")
    void testLargeScaleQuery() {
        if (createdGroupIds.isEmpty()) {
            System.out.println("跳过测试: 没有可用的集团ID");
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        List<GroupQuotaResponse> allQuotas = groupQuotaService.getEnabledGroupQuotas();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        System.out.println("=== 大数据量查询性能测试结果 ===");
        System.out.println("查询到的记录数: " + allQuotas.size());
        System.out.println("查询耗时: " + totalTime + "ms");
        
        assertThat(allQuotas).isNotNull();
        assertThat(totalTime).isLessThan(5000);
    }
    
    @Test
    @Order(5)
    @DisplayName("性能测试 - 额度计算性能")
    void testQuotaCalculationPerformance() {
        int iterations = 10000;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            BigDecimal total = new BigDecimal("1000000.00");
            BigDecimal used = new BigDecimal("250000.00");
            BigDecimal available = MonetaryUtils.subtract(total, used);
            BigDecimal usageRate = MonetaryUtils.usageRate(used, total);
            
            assertThat(available).isNotNull();
            assertThat(usageRate).isNotNull();
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        System.out.println("=== 额度计算性能测试结果 ===");
        System.out.println("计算次数: " + iterations);
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均每次计算时间: " + (totalTime / (double) iterations) + "ms");
        System.out.println("每秒计算次数: " + (iterations * 1000.0 / totalTime));
        
        assertThat(totalTime).isLessThan(5000);
    }
    
    @AfterAll
    static void cleanup(@Autowired(required = false) GroupQuotaRepository groupQuotaRepository,
                        @Autowired(required = false) List<Long> createdGroupIds) {
        if (groupQuotaRepository != null && createdGroupIds != null) {
            System.out.println("=== 清理测试数据 ===");
            System.out.println("将删除 " + createdGroupIds.size() + " 条测试记录");
        }
    }
}
