package com.bank.check.chain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "check")
public class CheckChainProperties {

    private Chain chain = new Chain();
    private Param param = new Param();
    private Auth auth = new Auth();
    private Amount amount = new Amount();
    private Risk risk = new Risk();
    private Frequency frequency = new Frequency();
    private Time time = new Time();
    private Whitelist whitelist = new Whitelist();

    public static class Chain {
        private boolean defaultEnabled = true;
        private boolean simpleEnabled = true;
        private boolean strictEnabled = false;
        private List<String> defaultHandlers = new ArrayList<>();

        public boolean isDefaultEnabled() { return defaultEnabled; }
        public void setDefaultEnabled(boolean defaultEnabled) { this.defaultEnabled = defaultEnabled; }
        public boolean isSimpleEnabled() { return simpleEnabled; }
        public void setSimpleEnabled(boolean simpleEnabled) { this.simpleEnabled = simpleEnabled; }
        public boolean isStrictEnabled() { return strictEnabled; }
        public void setStrictEnabled(boolean strictEnabled) { this.strictEnabled = strictEnabled; }
        public List<String> getDefaultHandlers() { return defaultHandlers; }
        public void setDefaultHandlers(List<String> defaultHandlers) { this.defaultHandlers = defaultHandlers; }
    }

    public static class Param {
        private boolean enabled = true;
        private List<String> requiredParams = new ArrayList<>();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public List<String> getRequiredParams() { return requiredParams; }
        public void setRequiredParams(List<String> requiredParams) { this.requiredParams = requiredParams; }
    }

    public static class Auth {
        private boolean enabled = true;
        private String requiredRole;
        private boolean checkIp = true;
        private List<String> allowedIps = new ArrayList<>();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getRequiredRole() { return requiredRole; }
        public void setRequiredRole(String requiredRole) { this.requiredRole = requiredRole; }
        public boolean isCheckIp() { return checkIp; }
        public void setCheckIp(boolean checkIp) { this.checkIp = checkIp; }
        public List<String> getAllowedIps() { return allowedIps; }
        public void setAllowedIps(List<String> allowedIps) { this.allowedIps = allowedIps; }
    }

    public static class Amount {
        private boolean enabled = true;
        private Long minAmount = 1L;
        private Long maxAmount = 1000000L;
        private Long singleLimit = 50000L;
        private Long dayLimit = 200000L;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Long getMinAmount() { return minAmount; }
        public void setMinAmount(Long minAmount) { this.minAmount = minAmount; }
        public Long getMaxAmount() { return maxAmount; }
        public void setMaxAmount(Long maxAmount) { this.maxAmount = maxAmount; }
        public Long getSingleLimit() { return singleLimit; }
        public void setSingleLimit(Long singleLimit) { this.singleLimit = singleLimit; }
        public Long getDayLimit() { return dayLimit; }
        public void setDayLimit(Long dayLimit) { this.dayLimit = dayLimit; }
    }

    public static class Risk {
        private boolean enabled = true;
        private int maxRiskLevel = 3;
        private boolean enableRealTimeCheck = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxRiskLevel() { return maxRiskLevel; }
        public void setMaxRiskLevel(int maxRiskLevel) { this.maxRiskLevel = maxRiskLevel; }
        public boolean isEnableRealTimeCheck() { return enableRealTimeCheck; }
        public void setEnableRealTimeCheck(boolean enableRealTimeCheck) { this.enableRealTimeCheck = enableRealTimeCheck; }
    }

    public static class Frequency {
        private boolean enabled = true;
        private int maxRequestsPerMinute = 60;
        private int maxRequestsPerHour = 1000;
        private int maxRequestsPerDay = 10000;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
        public void setMaxRequestsPerMinute(int maxRequestsPerMinute) { this.maxRequestsPerMinute = maxRequestsPerMinute; }
        public int getMaxRequestsPerHour() { return maxRequestsPerHour; }
        public void setMaxRequestsPerHour(int maxRequestsPerHour) { this.maxRequestsPerHour = maxRequestsPerHour; }
        public int getMaxRequestsPerDay() { return maxRequestsPerDay; }
        public void setMaxRequestsPerDay(int maxRequestsPerDay) { this.maxRequestsPerDay = maxRequestsPerDay; }
    }

    public static class Time {
        private boolean enabled = true;
        private String startTime = "00:00:00";
        private String endTime = "23:59:59";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }

    public static class Whitelist {
        private boolean enabled = true;
        private String defaultRuleId;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getDefaultRuleId() { return defaultRuleId; }
        public void setDefaultRuleId(String defaultRuleId) { this.defaultRuleId = defaultRuleId; }
    }

    public Chain getChain() { return chain; }
    public void setChain(Chain chain) { this.chain = chain; }
    public Param getParam() { return param; }
    public void setParam(Param param) { this.param = param; }
    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }
    public Amount getAmount() { return amount; }
    public void setAmount(Amount amount) { this.amount = amount; }
    public Risk getRisk() { return risk; }
    public void setRisk(Risk risk) { this.risk = risk; }
    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }
    public Time getTime() { return time; }
    public void setTime(Time time) { this.time = time; }
    public Whitelist getWhitelist() { return whitelist; }
    public void setWhitelist(Whitelist whitelist) { this.whitelist = whitelist; }
}
