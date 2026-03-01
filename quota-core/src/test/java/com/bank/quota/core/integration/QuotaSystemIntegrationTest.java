package com.bank.quota.core.integration;

import com.bank.quota.QuotaSystemApplication;
import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.groupquota.*;
import com.bank.quota.core.dto.customerquota.*;
import com.bank.quota.core.dto.whitelist.AddWhitelistRequest;
import com.bank.quota.core.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = QuotaSystemApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuotaSystemIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private GroupQuotaService groupQuotaService;
    
    @Autowired
    private CustomerQuotaService customerQuotaService;
    
    @Autowired
    private WhitelistService whitelistService;
    
    private String baseUrl;
    private Long testGroupId;
    private Long testCustomerId;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
    }
    
    @Test
    @Order(1)
    @DisplayName("集成测试 - 创建集团额度")
    void testCreateGroupQuota() {
        CreateGroupQuotaRequest request = new CreateGroupQuotaRequest();
        request.setGroupId(System.currentTimeMillis() % 10000);
        request.setGroupName("集成测试集团");
        request.setTotalQuota(new BigDecimal("5000000.00"));
        request.setDescription("集成测试集团额度");
        request.setCreateBy("integration-test");
        
        ResponseEntity<Result> response = restTemplate.postForEntity(
                baseUrl + "/group-quota",
                request,
                Result.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
        assertThat(data.get("groupName")).isEqualTo("集成测试集团");
        assertThat(data.get("status")).isEqualTo("ENABLED");
        
        testGroupId = ((Number) data.get("id")).longValue();
    }
    
    @Test
    @Order(2)
    @DisplayName("集成测试 - 创建客户额度")
    void testCreateCustomerQuota() {
        assertThat(testGroupId).isNotNull();
        
        CreateCustomerQuotaRequest request = new CreateCustomerQuotaRequest();
        request.setCustomerId(System.currentTimeMillis() % 10000);
        request.setCustomerName("集成测试客户");
        request.setCustomerType("INDIVIDUAL");
        request.setGroupId(testGroupId);
        request.setTotalQuota(new BigDecimal("500000.00"));
        request.setDescription("集成测试客户额度");
        request.setCreateBy("integration-test");
        
        ResponseEntity<Result> response = restTemplate.postForEntity(
                baseUrl + "/customer-quota",
                request,
                Result.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
        assertThat(data.get("customerName")).isEqualTo("集成测试客户");
        assertThat(data.get("status")).isEqualTo("ENABLED");
        
        testCustomerId = ((Number) data.get("id")).longValue();
    }
    
    @Test
    @Order(3)
    @DisplayName("集成测试 - 添加白名单")
    void testAddToWhitelist() {
        assertThat(testCustomerId).isNotNull();
        
        AddWhitelistRequest request = new AddWhitelistRequest();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("白名单测试客户");
        request.setWhitelistType("VIP_CUSTOMER");
        request.setDescription("集成测试白名单");
        request.setCreateBy("integration-test");
        
        ResponseEntity<Result> response = restTemplate.postForEntity(
                baseUrl + "/whitelist",
                request,
                Result.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
        assertThat(data.get("customerName")).isEqualTo("白名单测试客户");
        assertThat(data.get("status")).isEqualTo("ENABLED");
    }
    
    @Test
    @Order(4)
    @DisplayName("集成测试 - 冻结集团额度")
    void testFreezeGroupQuota() {
        assertThat(testGroupId).isNotNull();
        
        Map<String, String> params = new HashMap<>();
        params.put("reason", "集成测试冻结");
        params.put("operator", "integration-test");
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params);
        
        ResponseEntity<Result> response = restTemplate.exchange(
                baseUrl + "/group-quota/" + testGroupId + "/freeze",
                HttpMethod.POST,
                entity,
                Result.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
        assertThat(data.get("status")).isEqualTo("FROZEN");
    }
    
    @Test
    @Order(5)
    @DisplayName("集成测试 - 解冻集团额度")
    void testUnfreezeGroupQuota() {
        assertThat(testGroupId).isNotNull();
        
        Map<String, String> params = new HashMap<>();
        params.put("operator", "integration-test");
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params);
        
        ResponseEntity<Result> response = restTemplate.exchange(
                baseUrl + "/group-quota/" + testGroupId + "/unfreeze",
                HttpMethod.POST,
                entity,
                Result.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
        assertThat(data.get("status")).isEqualTo("ENABLED");
    }
    
    @Test
    @Order(6)
    @DisplayName("集成测试 - 查询集团额度")
    void testGetGroupQuota() {
        assertThat(testGroupId).isNotNull();
        
        ResponseEntity<Result> response = restTemplate.getForEntity(
                baseUrl + "/group-quota/" + testGroupId,
                Result.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
        assertThat(data.get("id")).isEqualTo(testGroupId);
        assertThat(data.get("groupName")).isEqualTo("集成测试集团");
    }
    
    @Test
    @Order(7)
    @DisplayName("集成测试 - 查询客户额度使用量")
    void testGetCustomerQuotaUsage() {
        assertThat(testCustomerId).isNotNull();
        
        ResponseEntity<Result> response = restTemplate.getForEntity(
                baseUrl + "/customer-quota/" + testCustomerId + "/usage",
                Result.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
        assertThat(data.get("totalQuota")).isNotNull();
        assertThat(data.get("usedQuota")).isNotNull();
        assertThat(data.get("availableQuota")).isNotNull();
    }
    
    @Test
    @Order(8)
    @DisplayName("集成测试 - 查询有效集团额度列表")
    void testGetEnabledGroupQuotas() {
        ResponseEntity<Result> response = restTemplate.getForEntity(
                baseUrl + "/group-quota/enabled",
                Result.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> data = 
                (java.util.List<Map<String, Object>>) response.getBody().getData();
        assertThat(data).isNotEmpty();
    }
    
    @Test
    @Order(9)
    @DisplayName("集成测试 - 检查客户是否在白名单中")
    void testCheckCustomerInWhitelist() {
        assertThat(testCustomerId).isNotNull();
        
        ResponseEntity<Result> response = restTemplate.getForEntity(
                baseUrl + "/whitelist/check/" + testCustomerId,
                Result.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
        assertThat(data.get("inWhitelist")).isEqualTo(true);
    }
    
    @Test
    @Order(10)
    @DisplayName("集成测试 - 查询审计日志")
    void testQueryAuditLogs() {
        Map<String, String> params = new HashMap<>();
        params.put("pageNum", "1");
        params.put("pageSize", "10");
        
        ResponseEntity<Result> response = restTemplate.getForEntity(
                baseUrl + "/audit/logs?pageNum={pageNum}&pageSize={pageSize}",
                Result.class,
                params);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
        assertThat(data.get("total")).isNotNull();
        assertThat(data.get("list")).isNotNull();
    }
}
