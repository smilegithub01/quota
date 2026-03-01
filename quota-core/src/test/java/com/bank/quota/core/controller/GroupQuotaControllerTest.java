package com.bank.quota.core.controller;

import com.bank.quota.core.dto.groupquota.*;
import com.bank.quota.core.service.GroupQuotaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(GroupQuotaController.class)
class GroupQuotaControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private GroupQuotaService groupQuotaService;
    
    private GroupQuotaResponse testResponse;
    private CreateGroupQuotaRequest createRequest;
    
    @BeforeEach
    void setUp() {
        testResponse = new GroupQuotaResponse();
        testResponse.setId(1L);
        testResponse.setGroupId(100L);
        testResponse.setGroupName("测试集团");
        testResponse.setTotalQuota(new BigDecimal("1000000.00"));
        testResponse.setUsedQuota(new BigDecimal("200000.00"));
        testResponse.setAvailableQuota(new BigDecimal("800000.00"));
        testResponse.setStatus("ENABLED");
        testResponse.setDescription("测试集团额度");
        testResponse.setCreateBy("admin");
        testResponse.setCreateTime(LocalDateTime.now());
        
        createRequest = new CreateGroupQuotaRequest();
        createRequest.setGroupId(100L);
        createRequest.setGroupName("测试集团");
        createRequest.setTotalQuota(new BigDecimal("1000000.00"));
        createRequest.setDescription("测试集团额度");
        createRequest.setCreateBy("admin");
    }
    
    @Test
    @DisplayName("创建集团额度 - REST API测试")
    void createGroupQuota_Success() throws Exception {
        when(groupQuotaService.createGroupQuota(any(CreateGroupQuotaRequest.class)))
                .thenReturn(testResponse);
        
        mockMvc.perform(post("/api/v1/group-quota")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.groupId").value(100))
                .andExpect(jsonPath("$.data.groupName").value("测试集团"))
                .andExpect(jsonPath("$.data.status").value("ENABLED"));
    }
    
    @Test
    @DisplayName("创建集团额度 - 参数验证失败")
    void createGroupQuota_ValidationFailed() throws Exception {
        CreateGroupQuotaRequest invalidRequest = new CreateGroupQuotaRequest();
        invalidRequest.setGroupId(null);
        invalidRequest.setGroupName(null);
        
        mockMvc.perform(post("/api/v1/group-quota")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("查询集团额度详情 - REST API测试")
    void getGroupQuota_Success() throws Exception {
        when(groupQuotaService.getGroupQuota(1L)).thenReturn(testResponse);
        
        mockMvc.perform(get("/api/v1/group-quota/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.groupId").value(100));
    }
    
    @Test
    @DisplayName("查询所有有效集团额度 - REST API测试")
    void getEnabledGroupQuotas_Success() throws Exception {
        List<GroupQuotaResponse> responses = Arrays.asList(testResponse);
        when(groupQuotaService.getEnabledGroupQuotas()).thenReturn(responses);
        
        mockMvc.perform(get("/api/v1/group-quota/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("ENABLED"));
    }
    
    @Test
    @DisplayName("冻结集团额度 - REST API测试")
    void freezeGroupQuota_Success() throws Exception {
        testResponse.setStatus("FROZEN");
        when(groupQuotaService.freezeGroupQuota(eq(1L), anyString(), anyString()))
                .thenReturn(testResponse);
        
        mockMvc.perform(post("/api/v1/group-quota/1/freeze")
                        .param("reason", "风控冻结")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.status").value("FROZEN"));
    }
    
    @Test
    @DisplayName("解冻集团额度 - REST API测试")
    void unfreezeGroupQuota_Success() throws Exception {
        when(groupQuotaService.unfreezeGroupQuota(eq(1L), anyString()))
                .thenReturn(testResponse);
        
        mockMvc.perform(post("/api/v1/group-quota/1/unfreeze")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"));
    }
    
    @Test
    @DisplayName("停用集团额度 - REST API测试")
    void disableGroupQuota_Success() throws Exception {
        testResponse.setStatus("DISABLED");
        when(groupQuotaService.disableGroupQuota(eq(1L), anyString(), anyString()))
                .thenReturn(testResponse);
        
        mockMvc.perform(post("/api/v1/group-quota/1/disable")
                        .param("reason", "业务调整")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }
    
    @Test
    @DisplayName("查询集团额度使用量 - REST API测试")
    void getGroupQuotaUsage_Success() throws Exception {
        GroupQuotaUsageResponse usageResponse = new GroupQuotaUsageResponse();
        usageResponse.setGroupId(100L);
        usageResponse.setTotalQuota(new BigDecimal("1000000.00"));
        usageResponse.setUsedQuota(new BigDecimal("200000.00"));
        usageResponse.setAvailableQuota(new BigDecimal("800000.00"));
        usageResponse.setUsageRate(new BigDecimal("0.20"));
        
        when(groupQuotaService.getGroupQuotaUsage(1L)).thenReturn(usageResponse);
        
        mockMvc.perform(get("/api/v1/group-quota/1/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.usageRate").value(0.20));
    }
    
    @Test
    @DisplayName("调整集团额度 - REST API测试")
    void adjustGroupQuota_Success() throws Exception {
        mockMvc.perform(post("/api/v1/group-quota/1/adjust")
                        .param("adjustmentAmount", "100000.00")
                        .param("reason", "额度调整")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"));
    }
}
