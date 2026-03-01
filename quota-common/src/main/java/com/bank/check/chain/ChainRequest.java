package com.bank.check.chain;

import java.util.Map;

/**
 * 校验请求上下文
 * 
 * 承载校验所需的全部信息，支持动态跳过校验
 */
public class ChainRequest {

    private String requestId;
    private String businessType;
    private Map<String, Object> params;
    private String userId;
    private String username;
    private String ip;
    private String deviceId;
    private String phone;
    private String apiCode;
    private String bizCode;
    private Long amount;
    private String channel;
    private String productCode;

    private boolean skipAllCheck = false;
    private boolean skipParamCheck = false;
    private boolean skipAuthCheck = false;
    private boolean skipStatusCheck = false;
    private boolean skipAmountCheck = false;
    private boolean skipRiskCheck = false;
    private boolean skipConfigCheck = false;
    private boolean skipTimeCheck = false;
    private boolean skipFrequencyCheck = false;
    private boolean skipWhiteListCheck = false;

    private Map<String, Object> extData;

    public ChainRequest() {
    }

    public ChainRequest(String requestId, String businessType) {
        this.requestId = requestId;
        this.businessType = businessType;
    }

    public static ChainRequest create(String requestId, String businessType) {
        return new ChainRequest(requestId, businessType);
    }

    public ChainRequest withParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public ChainRequest withUser(String userId, String username) {
        this.userId = userId;
        this.username = username;
        return this;
    }

    public ChainRequest withIp(String ip) {
        this.ip = ip;
        return this;
    }

    public ChainRequest withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public ChainRequest skipAllCheck() {
        this.skipAllCheck = true;
        return this;
    }

    public ChainRequest skipParamCheck() {
        this.skipParamCheck = true;
        return this;
    }

    public ChainRequest skipAuthCheck() {
        this.skipAuthCheck = true;
        return this;
    }

    public ChainRequest skipStatusCheck() {
        this.skipStatusCheck = true;
        return this;
    }

    public ChainRequest skipAmountCheck() {
        this.skipAmountCheck = true;
        return this;
    }

    public ChainRequest skipRiskCheck() {
        this.skipRiskCheck = true;
        return this;
    }

    public ChainRequest skipConfigCheck() {
        this.skipConfigCheck = true;
        return this;
    }

    public ChainRequest skipTimeCheck() {
        this.skipTimeCheck = true;
        return this;
    }

    public ChainRequest skipFrequencyCheck() {
        this.skipFrequencyCheck = true;
        return this;
    }

    public ChainRequest skipWhiteListCheck() {
        this.skipWhiteListCheck = true;
        return this;
    }

    public ChainRequest putExtData(String key, Object value) {
        if (this.extData == null) {
            this.extData = new java.util.HashMap<>();
        }
        this.extData.put(key, value);
        return this;
    }

    public Object getExtData(String key) {
        return this.extData != null ? this.extData.get(key) : null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public boolean isSkipAllCheck() {
        return skipAllCheck;
    }

    public void setSkipAllCheck(boolean skipAllCheck) {
        this.skipAllCheck = skipAllCheck;
    }

    public boolean isSkipParamCheck() {
        return skipParamCheck;
    }

    public void setSkipParamCheck(boolean skipParamCheck) {
        this.skipParamCheck = skipParamCheck;
    }

    public boolean isSkipAuthCheck() {
        return skipAuthCheck;
    }

    public void setSkipAuthCheck(boolean skipAuthCheck) {
        this.skipAuthCheck = skipAuthCheck;
    }

    public boolean isSkipStatusCheck() {
        return skipStatusCheck;
    }

    public void setSkipStatusCheck(boolean skipStatusCheck) {
        this.skipStatusCheck = skipStatusCheck;
    }

    public boolean isSkipAmountCheck() {
        return skipAmountCheck;
    }

    public void setSkipAmountCheck(boolean skipAmountCheck) {
        this.skipAmountCheck = skipAmountCheck;
    }

    public boolean isSkipRiskCheck() {
        return skipRiskCheck;
    }

    public void setSkipRiskCheck(boolean skipRiskCheck) {
        this.skipRiskCheck = skipRiskCheck;
    }

    public boolean isSkipConfigCheck() {
        return skipConfigCheck;
    }

    public void setSkipConfigCheck(boolean skipConfigCheck) {
        this.skipConfigCheck = skipConfigCheck;
    }

    public boolean isSkipTimeCheck() {
        return skipTimeCheck;
    }

    public void setSkipTimeCheck(boolean skipTimeCheck) {
        this.skipTimeCheck = skipTimeCheck;
    }

    public boolean isSkipFrequencyCheck() {
        return skipFrequencyCheck;
    }

    public void setSkipFrequencyCheck(boolean skipFrequencyCheck) {
        this.skipFrequencyCheck = skipFrequencyCheck;
    }

    public boolean isSkipWhiteListCheck() {
        return skipWhiteListCheck;
    }

    public void setSkipWhiteListCheck(boolean skipWhiteListCheck) {
        this.skipWhiteListCheck = skipWhiteListCheck;
    }

    public Map<String, Object> getExtData() {
        return extData;
    }

    public void setExtData(Map<String, Object> extData) {
        this.extData = extData;
    }

    @Override
    public String toString() {
        return "ChainRequest{" +
                "requestId='" + requestId + '\'' +
                ", businessType='" + businessType + '\'' +
                ", userId='" + userId + '\'' +
                ", ip='" + ip + '\'' +
                ", amount=" + amount +
                ", skipAllCheck=" + skipAllCheck +
                ", skipParamCheck=" + skipParamCheck +
                ", skipAuthCheck=" + skipAuthCheck +
                ", skipRiskCheck=" + skipRiskCheck +
                '}';
    }
}
