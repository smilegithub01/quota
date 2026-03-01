package com.bank.check.chain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 校验响应结果
 */
public class ChainResponse {

    private boolean passed;
    private String requestId;
    private String failedCheckItemId;
    private String failedCheckItemName;
    private String errorCode;
    private String errorMessage;
    private List<CheckDetail> checkDetails;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long totalDurationMs;

    public ChainResponse() {
        this.checkDetails = new ArrayList<>();
        this.startTime = LocalDateTime.now();
        this.passed = true;
    }

    public static ChainResponse success(String requestId) {
        ChainResponse response = new ChainResponse();
        response.requestId = requestId;
        response.endTime = LocalDateTime.now();
        response.totalDurationMs = java.time.Duration.between(response.startTime, response.endTime).toMillis();
        return response;
    }

    public static ChainResponse fail(String requestId, String checkItemId, String checkItemName,
                                     String errorCode, String errorMessage) {
        ChainResponse response = new ChainResponse();
        response.requestId = requestId;
        response.passed = false;
        response.failedCheckItemId = checkItemId;
        response.failedCheckItemName = checkItemName;
        response.errorCode = errorCode;
        response.errorMessage = errorMessage;
        response.endTime = LocalDateTime.now();
        response.totalDurationMs = java.time.Duration.between(response.startTime, response.endTime).toMillis();
        return response;
    }

    public void addCheckDetail(CheckDetail detail) {
        this.checkDetails.add(detail);
    }

    public void calculateDuration() {
        if (this.endTime == null) {
            this.endTime = LocalDateTime.now();
            this.totalDurationMs = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFailedCheckItemId() {
        return failedCheckItemId;
    }

    public void setFailedCheckItemId(String failedCheckItemId) {
        this.failedCheckItemId = failedCheckItemId;
    }

    public String getFailedCheckItemName() {
        return failedCheckItemName;
    }

    public void setFailedCheckItemName(String failedCheckItemName) {
        this.failedCheckItemName = failedCheckItemName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<CheckDetail> getCheckDetails() {
        return checkDetails;
    }

    public void setCheckDetails(List<CheckDetail> checkDetails) {
        this.checkDetails = checkDetails;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public void setTotalDurationMs(long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
    }

    public static class CheckDetail {
        private String checkItemId;
        private String checkItemName;
        private boolean passed;
        private String errorCode;
        private String errorMessage;
        private long durationMs;
        private LocalDateTime checkTime;

        public CheckDetail() {
            this.checkTime = LocalDateTime.now();
        }

        public static CheckDetail pass(String checkItemId, String checkItemName, long durationMs) {
            CheckDetail detail = new CheckDetail();
            detail.checkItemId = checkItemId;
            detail.checkItemName = checkItemName;
            detail.passed = true;
            detail.durationMs = durationMs;
            return detail;
        }

        public static CheckDetail fail(String checkItemId, String checkItemName,
                                       String errorCode, String errorMessage, long durationMs) {
            CheckDetail detail = new CheckDetail();
            detail.checkItemId = checkItemId;
            detail.checkItemName = checkItemName;
            detail.passed = false;
            detail.errorCode = errorCode;
            detail.errorMessage = errorMessage;
            detail.durationMs = durationMs;
            return detail;
        }

        public String getCheckItemId() {
            return checkItemId;
        }

        public void setCheckItemId(String checkItemId) {
            this.checkItemId = checkItemId;
        }

        public String getCheckItemName() {
            return checkItemName;
        }

        public void setCheckItemName(String checkItemName) {
            this.checkItemName = checkItemName;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(long durationMs) {
            this.durationMs = durationMs;
        }

        public LocalDateTime getCheckTime() {
            return checkTime;
        }

        public void setCheckTime(LocalDateTime checkTime) {
            this.checkTime = checkTime;
        }
    }

    @Override
    public String toString() {
        return "ChainResponse{" +
                "passed=" + passed +
                ", requestId='" + requestId + '\'' +
                ", failedCheckItemId='" + failedCheckItemId + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", totalDurationMs=" + totalDurationMs +
                '}';
    }
}
