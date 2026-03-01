package com.bank.quota.core.controller;

import com.bank.quota.core.dto.customerquota.*;
import com.bank.quota.core.enums.CustomerType;
import com.bank.quota.core.service.CustomerQuotaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(CustomerQuotaController.class)
class CustomerQuotaControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private CustomerQuotaService customerQuotaService;
    
    private CustomerQuotaResponse testResponse;
    private CreateCustomerQuotaRequest createRequest;
    
    @BeforeEach
    void setUp() {
        testResponse = new CustomerQuotaResponse();
        testResponse.setId(1L);
        testResponse.setCustomerId(100L);
        testResponse.setCustomerName("测试客户");
        testResponse.setCustomerType("INDIVIDUAL");
        testResponse.setGroupId(10L);
        testResponse.setTotalQuota(new BigDecimal("100000.00"));
        testResponse.setUsedQuota(new BigDecimal("20000.00"));
        testResponse.setAvailableQuota(new BigDecimal("80000.00"));
        testResponse.setStatus("ENABLED");
        testResponse.setDescription("测试客户额度");
        testResponse.setCreateBy("admin");
        testResponse.setCreateTime(LocalDateTime.now());
        
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
    @DisplayName("创建客户额度 - REST API测试")
    void createCustomerQuota_Success() throws Exception {
        when(customerQuotaService.createCustomerQuota(any(CreateCustomerQuotaRequest.class)))
                .thenReturn(testResponse);
        
        mockMvc.perform(post("/api/v1/customer-quota")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.customerId").value(100))
                .andExpect(jsonPath("$.data.customerName").value("测试客户"))
                .andExpect(jsonPath("$.data.status").value("ENABLED"));
    }
    
    @Test
    @DisplayName("查询客户额度详情 - REST API测试")
    void getCustomerQuota_Success() throws Exception {
        when(customerQuotaService.getCustomerQuota(100L)).thenReturn(testResponse);
        
        mockMvc.perform(get("/api/v1/customer-quota/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.customerId").value(100));
    }
    
    @Test
    @DisplayName("冻结客户额度 - REST API测试")
    void freezeCustomerQuota_Success() throws Exception {
        testResponse.setStatus("FROZEN");
        when(customerQuotaService.freezeCustomerQuota(eq(100L), anyString(), anyString()))
                .thenReturn(testResponse);
        
        mockMvc.perform(post("/api/v1/customer-quota/100/freeze")
                        .param("reason", "风控冻结")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.status").value("FROZEN"));
    }
    
    @Test
    @DisplayName("解冻客户额度 - REST API测试")
    void unfreezeCustomerQuota_Success() throws Exception {
        when(customerQuotaService.unfreezeCustomerQuota(eq(100L), anyString()))
                .thenReturn(testResponse);
        
        mockMvc.perform(post("/api/v1/customer-quota/100/unfreeze")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"));
    }
    
    @Test
    @DisplayName("停用客户额度 - REST API测试")
    void disableCustomerQuota_Success() throws Exception {
        testResponse.setStatus("DISABLED");
        when(customerQuotaService.disableCustomerQuota(eq(100L), anyString(), anyString()))
                .thenReturn(testResponse);
        
        mockMvc.perform(post("/api/v1/customer-quota/100/disable")
                        .param("reason", "客户注销")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }
    
    @Test
    @DisplayName("查询集团下客户额度 - REST API测试")
    void getCustomerQuotasByGroupId_Success() throws Exception {
        List<CustomerQuotaResponse> responses = Arrays.asList(testResponse);
        when(customerQuotaService.getCustomerQuotasByGroupId(10L)).thenReturn(responses);
        
        mockMvc.perform(get("/api/v1/customer-quota/group/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].groupId").value(10));
    }
    
    @Test
    @DisplayName("查询有效客户额度列表 - REST API测试")
    void getEnabledCustomerQuotas_Success() throws Exception {
        List<CustomerQuotaResponse> responses = Arrays.asList(testResponse);
        when(customerQuotaService.getEnabledCustomerQuotas()).thenReturn(responses);
        
        mockMvc.perform(get("/api/v1/customer-quota/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("ENABLED"));
    }
    
    @Test
    @DisplayName("按类型查询客户额度 - REST API测试")
    void getCustomerQuotasByType_Success() throws Exception {
        List<CustomerQuotaResponse> responses = Arrays.asList(testResponse);
        when(customerQuotaService.getCustomerQuotasByType(CustomerType.INDIVIDUAL))
                .thenReturn(responses);
        
        mockMvc.perform(get("/api/v1/customer-quota/type/INDIVIDUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].customerType").value("INDIVIDUAL"));
    }
    
    @Test
    @DisplayName("查询客户额度使用量 - REST API测试")
    void getCustomerQuotaUsage_Success() throws Exception {
        CustomerQuotaUsageResponse usageResponse = new CustomerQuotaUsageResponse();
        usageResponse.setCustomerId(100L);
        usageResponse.setTotalQuota(new BigDecimal("100000.00"));
        usageResponse.setUsedQuota(new BigDecimal("20000.00"));
        usageResponse.setAvailableQuota(new BigDecimal("80000.00"));
        usageResponse.setUsageRate(new BigDecimal("0.20"));
        
        when(customerQuotaService.getCustomerQuotaUsage(100L)).thenReturn(usageResponse);
        
        mockMvc.perform(get("/api/v1/customer-quota/100/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.usageRate").value(0.20));
    }
    
    @Test
    @DisplayName("调整客户额度 - REST API测试")
    void adjustCustomerQuota_Success() throws Exception {
        mockMvc.perform(post("/api/v1/customer-quota/100/adjust")
                        .param("adjustmentAmount", "50000.00")
                        .param("reason", "额度调整")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"));
    }
    
    @Test
    @DisplayName("额度转移 - REST API测试")
    void transferQuota_Success() throws Exception {
        mockMvc.perform(post("/api/v1/customer-quota/transfer")
                        .param("fromCustomerId", "100")
                        .param("toCustomerId", "200")
                        .param("amount", "30000.00")
                        .param("reason", "内部调拨")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"));
    }
}
