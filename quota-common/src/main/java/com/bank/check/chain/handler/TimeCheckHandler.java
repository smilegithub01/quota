package com.bank.check.chain.handler;

import com.bank.check.chain.ChainRequest;
import com.bank.check.chain.ChainResponse;
import com.bank.check.chain.CheckHandler;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 时间校验处理器
 * 
 * 校验操作时间是否在允许范围内
 */
public class TimeCheckHandler extends CheckHandler {

    private static final String ERROR_CODE = "TIME_CHECK_FAILED";
    private static final String DEFAULT_ERROR_MSG = "时间校验失败";

    private String startTime = "00:00:00";
    private String endTime = "23:59:59";
    private Map<String, String> timeWindows;

    public TimeCheckHandler() {
        super("TIME_CHECK", "时间校验");
        this.order = 7;
    }

    public TimeCheckHandler(String startTime, String endTime) {
        super("TIME_CHECK", "时间校验");
        this.startTime = startTime;
        this.endTime = endTime;
        this.order = 7;
    }

    @Override
    public boolean handle(ChainRequest request, ChainResponse response) {
        long startTimeMillis = System.currentTimeMillis();

        if (request.isSkipAllCheck() || request.isSkipTimeCheck()) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTimeMillis));
            return continueChain(request, response);
        }

        if (!response.isPassed()) {
            return false;
        }

        try {
            String businessType = request.getBusinessType();
            String errorMsg = validateTime(businessType);
            
            if (errorMsg != null) {
                response.addCheckDetail(ChainResponse.CheckDetail.fail(
                    checkItemId, checkItemName, ERROR_CODE, errorMsg,
                    System.currentTimeMillis() - startTimeMillis));
                response.setPassed(false);
                response.setFailedCheckItemId(checkItemId);
                response.setFailedCheckItemName(checkItemName);
                response.setErrorCode(ERROR_CODE);
                response.setErrorMessage(errorMsg);
                return false;
            }

            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTimeMillis));

        } catch (Exception e) {
            response.addCheckDetail(ChainResponse.CheckDetail.fail(
                checkItemId, checkItemName, ERROR_CODE, "时间校验异常: " + e.getMessage(),
                System.currentTimeMillis() - startTimeMillis));
            response.setPassed(false);
            response.setErrorCode(ERROR_CODE);
            response.setErrorMessage("时间校验异常: " + e.getMessage());
            return false;
        }

        return continueChain(request, response);
    }

    private String validateTime(String businessType) {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        if (timeWindows != null && businessType != null) {
            String window = timeWindows.get(businessType);
            if (window != null) {
                String[] times = window.split("-");
                if (times.length == 2) {
                    LocalTime windowStart = LocalTime.parse(times[0].trim(), formatter);
                    LocalTime windowEnd = LocalTime.parse(times[1].trim(), formatter);
                    if (now.isBefore(windowStart) || now.isAfter(windowEnd)) {
                        return "业务类型[" + businessType + "]不在允许的时间范围内";
                    }
                    return null;
                }
            }
        }

        LocalTime start = LocalTime.parse(this.startTime, formatter);
        LocalTime end = LocalTime.parse(this.endTime, formatter);
        
        if (now.isBefore(start) || now.isAfter(end)) {
            return "当前时间不在允许范围内: " + this.startTime + "-" + this.endTime;
        }
        
        return null;
    }

    public TimeCheckHandler setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public TimeCheckHandler setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public TimeCheckHandler setTimeWindows(Map<String, String> timeWindows) {
        this.timeWindows = timeWindows;
        return this;
    }
}
