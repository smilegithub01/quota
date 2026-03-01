package com.bank.whitelist.common;

import java.time.LocalDateTime;

/**
 * 白名单校验结果
 */
public class CheckResult {
    
    private boolean passed;
    private String checkItemId;
    private String checkItemName;
    private String errorCode;
    private String errorMessage;
    private String matchedRuleId;
    private LocalDateTime checkTime;
    private long durationMs;
    
    public CheckResult() {
        this.checkTime = LocalDateTime.now();
    }
    
    public CheckResult(boolean passed, String checkItemId) {
        this();
        this.passed = passed;
        this.checkItemId = checkItemId;
    }
    
    public static CheckResult pass(String checkItemId) {
        CheckResult result = new CheckResult(true, checkItemId);
        result.setCheckItemName(checkItemId);
        return result;
    }
    
    public static CheckResult pass(String checkItemId, String matchedRuleId) {
        CheckResult result = pass(checkItemId);
        result.setMatchedRuleId(matchedRuleId);
        return result;
    }
    
    public static CheckResult reject(String checkItemId, String errorCode, String errorMessage) {
        CheckResult result = new CheckResult(false, checkItemId);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        result.setCheckItemName(checkItemId);
        return result;
    }
    
    public static CheckResult reject(String checkItemId, String errorCode, String errorMessage, String matchedRuleId) {
        CheckResult result = reject(checkItemId, errorCode, errorMessage);
        result.setMatchedRuleId(matchedRuleId);
        return result;
    }
    
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    
    public String getCheckItemId() { return checkItemId; }
    public void setCheckItemId(String checkItemId) { this.checkItemId = checkItemId; }
    
    public String getCheckItemName() { return checkItemName; }
    public void setCheckItemName(String checkItemName) { this.checkItemName = checkItemName; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getMatchedRuleId() { return matchedRuleId; }
    public void setMatchedRuleId(String matchedRuleId) { this.matchedRuleId = matchedRuleId; }
    
    public LocalDateTime getCheckTime() { return checkTime; }
    public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }
    
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    
    @Override
    public String toString() {
        return "CheckResult{" +
                "passed=" + passed +
                ", checkItemId='" + checkItemId + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", matchedRuleId='" + matchedRuleId + '\'' +
                ", durationMs=" + durationMs +
                '}';
    }
}
