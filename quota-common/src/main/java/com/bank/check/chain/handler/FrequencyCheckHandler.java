package com.bank.check.chain.handler;

import com.bank.check.chain.ChainRequest;
import com.bank.check.chain.ChainResponse;
import com.bank.check.chain.CheckHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 频率校验处理器
 * 
 * 校验用户操作频率是否超限
 */
public class FrequencyCheckHandler extends CheckHandler {

    private static final String ERROR_CODE = "FREQUENCY_CHECK_FAILED";
    private static final String DEFAULT_ERROR_MSG = "频率校验失败";

    private int maxRequestsPerMinute = 60;
    private int maxRequestsPerHour = 1000;
    private int maxRequestsPerDay = 10000;
    
    private static final Map<String, AtomicLong> MINUTE_COUNTER = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> HOUR_COUNTER = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> DAY_COUNTER = new ConcurrentHashMap<>();
    
    private static final Map<String, Long> MINUTE_RESET_TIME = new ConcurrentHashMap<>();
    private static final Map<String, Long> HOUR_RESET_TIME = new ConcurrentHashMap<>();
    private static final Map<String, Long> DAY_RESET_TIME = new ConcurrentHashMap<>();

    public FrequencyCheckHandler() {
        super("FREQUENCY_CHECK", "频率校验");
        this.order = 8;
    }

    public FrequencyCheckHandler(int maxRequestsPerMinute, int maxRequestsPerHour, int maxRequestsPerDay) {
        super("FREQUENCY_CHECK", "频率校验");
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.maxRequestsPerHour = maxRequestsPerHour;
        this.maxRequestsPerDay = maxRequestsPerDay;
        this.order = 8;
    }

    @Override
    public boolean handle(ChainRequest request, ChainResponse response) {
        long startTime = System.currentTimeMillis();

        if (request.isSkipAllCheck() || request.isSkipFrequencyCheck()) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));
            return continueChain(request, response);
        }

        if (!response.isPassed()) {
            return false;
        }

        try {
            String key = buildKey(request);
            String errorMsg = validateFrequency(key);
            
            if (errorMsg != null) {
                response.addCheckDetail(ChainResponse.CheckDetail.fail(
                    checkItemId, checkItemName, ERROR_CODE, errorMsg,
                    System.currentTimeMillis() - startTime));
                response.setPassed(false);
                response.setFailedCheckItemId(checkItemId);
                response.setFailedCheckItemName(checkItemName);
                response.setErrorCode(ERROR_CODE);
                response.setErrorMessage(errorMsg);
                return false;
            }

            incrementCounter(key);
            
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            response.addCheckDetail(ChainResponse.CheckDetail.fail(
                checkItemId, checkItemName, ERROR_CODE, "频率校验异常: " + e.getMessage(),
                System.currentTimeMillis() - startTime));
            response.setPassed(false);
            response.setErrorCode(ERROR_CODE);
            response.setErrorMessage("频率校验异常: " + e.getMessage());
            return false;
        }

        return continueChain(request, response);
    }

    private String buildKey(ChainRequest request) {
        String userId = request.getUserId();
        String apiCode = request.getApiCode();
        if (userId != null && apiCode != null) {
            return userId + ":" + apiCode;
        }
        return request.getRequestId();
    }

    private String validateFrequency(String key) {
        long now = System.currentTimeMillis();
        
        checkAndResetCounter(key, now, MINUTE_COUNTER, MINUTE_RESET_TIME, 60 * 1000L);
        checkAndResetCounter(key, now, HOUR_COUNTER, HOUR_RESET_TIME, 60 * 60 * 1000L);
        checkAndResetCounter(key, now, DAY_COUNTER, DAY_RESET_TIME, 24 * 60 * 60 * 1000L);
        
        Long minuteCount = MINUTE_COUNTER.get(key) != null ? MINUTE_COUNTER.get(key).get() : 0L;
        Long hourCount = HOUR_COUNTER.get(key) != null ? HOUR_COUNTER.get(key).get() : 0L;
        Long dayCount = DAY_COUNTER.get(key) != null ? DAY_COUNTER.get(key).get() : 0L;
        
        if (minuteCount > maxRequestsPerMinute) {
            return "请求过于频繁，请稍后再试（每分钟限制" + maxRequestsPerMinute + "次）";
        }
        if (hourCount > maxRequestsPerHour) {
            return "请求过于频繁，请稍后再试（每小时限制" + maxRequestsPerHour + "次）";
        }
        if (dayCount > maxRequestsPerDay) {
            return "请求过于频繁，请明天再试（每日限制" + maxRequestsPerDay + "次）";
        }
        
        return null;
    }

    private void checkAndResetCounter(String key, long now, 
                                      Map<String, AtomicLong> counterMap,
                                      Map<String, Long> resetTimeMap,
                                      long resetInterval) {
        Long resetTime = resetTimeMap.get(key);
        if (resetTime == null || now > resetTime) {
            counterMap.putIfAbsent(key, new AtomicLong(0));
            resetTimeMap.put(key, now + resetInterval);
        }
    }

    private void incrementCounter(String key) {
        MINUTE_COUNTER.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        HOUR_COUNTER.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        DAY_COUNTER.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    public FrequencyCheckHandler setMaxRequestsPerMinute(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        return this;
    }

    public FrequencyCheckHandler setMaxRequestsPerHour(int maxRequestsPerHour) {
        this.maxRequestsPerHour = maxRequestsPerHour;
        return this;
    }

    public FrequencyCheckHandler setMaxRequestsPerDay(int maxRequestsPerDay) {
        this.maxRequestsPerDay = maxRequestsPerDay;
        return this;
    }
}
