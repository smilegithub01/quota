package com.bank.check.chain.config;

import com.bank.check.chain.CheckChain;
import com.bank.check.chain.CheckHandler;
import com.bank.check.chain.handler.*;
import com.bank.whitelist.core.WhitelistManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(CheckChainProperties.class)
public class CheckChainConfig {

    private final CheckChainProperties properties;

    public CheckChainConfig(CheckChainProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public WhitelistManager whitelistManager() {
        return new WhitelistManager();
    }

    @Bean
    public ParamCheckHandler paramCheckHandler() {
        ParamCheckHandler handler = new ParamCheckHandler();
        handler.setEnabled(properties.getParam().isEnabled());
        return handler;
    }

    @Bean
    public AuthCheckHandler authCheckHandler() {
        AuthCheckHandler handler = new AuthCheckHandler();
        handler.setEnabled(properties.getAuth().isEnabled());
        handler.setRequiredRole(properties.getAuth().getRequiredRole());
        handler.setCheckIp(properties.getAuth().isCheckIp());
        return handler;
    }

    @Bean
    public StatusCheckHandler statusCheckHandler() {
        return new StatusCheckHandler();
    }

    @Bean
    public AmountCheckHandler amountCheckHandler() {
        AmountCheckHandler handler = new AmountCheckHandler();
        handler.setEnabled(properties.getAmount().isEnabled());
        handler.setMinAmount(properties.getAmount().getMinAmount());
        handler.setMaxAmount(properties.getAmount().getMaxAmount());
        handler.setSingleLimit(properties.getAmount().getSingleLimit());
        handler.setDayLimit(properties.getAmount().getDayLimit());
        return handler;
    }

    @Bean
    public RiskCheckHandler riskCheckHandler() {
        RiskCheckHandler handler = new RiskCheckHandler();
        handler.setEnabled(properties.getRisk().isEnabled());
        handler.setMaxRiskLevel(properties.getRisk().getMaxRiskLevel());
        handler.setEnableRealTimeCheck(properties.getRisk().isEnableRealTimeCheck());
        return handler;
    }

    @Bean
    public ConfigCheckHandler configCheckHandler() {
        return new ConfigCheckHandler();
    }

    @Bean
    public TimeCheckHandler timeCheckHandler() {
        TimeCheckHandler handler = new TimeCheckHandler();
        handler.setEnabled(properties.getTime().isEnabled());
        handler.setStartTime(properties.getTime().getStartTime());
        handler.setEndTime(properties.getTime().getEndTime());
        return handler;
    }

    @Bean
    public FrequencyCheckHandler frequencyCheckHandler() {
        FrequencyCheckHandler handler = new FrequencyCheckHandler();
        handler.setEnabled(properties.getFrequency().isEnabled());
        handler.setMaxRequestsPerMinute(properties.getFrequency().getMaxRequestsPerMinute());
        handler.setMaxRequestsPerHour(properties.getFrequency().getMaxRequestsPerHour());
        handler.setMaxRequestsPerDay(properties.getFrequency().getMaxRequestsPerDay());
        return handler;
    }

    @Bean
    public WhiteListCheckHandler whiteListCheckHandler(WhitelistManager whitelistManager) {
        return new WhiteListCheckHandler(whitelistManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "check.chain", name = "default-enabled", havingValue = "true", matchIfMissing = true)
    public CheckChain defaultCheckChain(
            ParamCheckHandler paramCheckHandler,
            AuthCheckHandler authCheckHandler,
            StatusCheckHandler statusCheckHandler,
            AmountCheckHandler amountCheckHandler,
            RiskCheckHandler riskCheckHandler,
            ConfigCheckHandler configCheckHandler,
            TimeCheckHandler timeCheckHandler,
            FrequencyCheckHandler frequencyCheckHandler,
            WhiteListCheckHandler whiteListCheckHandler) {

        CheckChain chain = new CheckChain("DEFAULT_CHECK_CHAIN");
        chain.addHandler(paramCheckHandler)
             .addHandler(authCheckHandler)
             .addHandler(statusCheckHandler)
             .addHandler(amountCheckHandler)
             .addHandler(riskCheckHandler)
             .addHandler(configCheckHandler)
             .addHandler(timeCheckHandler)
             .addHandler(frequencyCheckHandler)
             .addHandler(whiteListCheckHandler);

        return chain;
    }

    @Bean
    @ConditionalOnProperty(prefix = "check.chain", name = "simple-enabled", havingValue = "true")
    public CheckChain simpleCheckChain(
            ParamCheckHandler paramCheckHandler,
            AuthCheckHandler authCheckHandler,
            AmountCheckHandler amountCheckHandler) {

        CheckChain chain = new CheckChain("SIMPLE_CHECK_CHAIN");
        chain.addHandler(paramCheckHandler)
             .addHandler(authCheckHandler)
             .addHandler(amountCheckHandler);

        return chain;
    }

    @Bean
    @ConditionalOnProperty(prefix = "check.chain", name = "strict-enabled", havingValue = "true")
    public CheckChain strictCheckChain(
            ParamCheckHandler paramCheckHandler,
            AuthCheckHandler authCheckHandler,
            StatusCheckHandler statusCheckHandler,
            AmountCheckHandler amountCheckHandler,
            RiskCheckHandler riskCheckHandler,
            FrequencyCheckHandler frequencyCheckHandler,
            WhiteListCheckHandler whiteListCheckHandler) {

        CheckChain chain = new CheckChain("STRICT_CHECK_CHAIN");
        chain.addHandler(paramCheckHandler)
             .addHandler(authCheckHandler)
             .addHandler(statusCheckHandler)
             .addHandler(amountCheckHandler)
             .addHandler(riskCheckHandler)
             .addHandler(frequencyCheckHandler)
             .addHandler(whiteListCheckHandler);

        return chain;
    }

    @Bean
    public CheckChainBuilder checkChainBuilder(
            ParamCheckHandler paramCheckHandler,
            AuthCheckHandler authCheckHandler,
            StatusCheckHandler statusCheckHandler,
            AmountCheckHandler amountCheckHandler,
            RiskCheckHandler riskCheckHandler,
            ConfigCheckHandler configCheckHandler,
            TimeCheckHandler timeCheckHandler,
            FrequencyCheckHandler frequencyCheckHandler,
            WhiteListCheckHandler whiteListCheckHandler) {

        return new CheckChainBuilder(paramCheckHandler, authCheckHandler, statusCheckHandler,
                amountCheckHandler, riskCheckHandler, configCheckHandler,
                timeCheckHandler, frequencyCheckHandler, whiteListCheckHandler);
    }

    public static class CheckChainBuilder {
        private final ParamCheckHandler paramCheckHandler;
        private final AuthCheckHandler authCheckHandler;
        private final StatusCheckHandler statusCheckHandler;
        private final AmountCheckHandler amountCheckHandler;
        private final RiskCheckHandler riskCheckHandler;
        private final ConfigCheckHandler configCheckHandler;
        private final TimeCheckHandler timeCheckHandler;
        private final FrequencyCheckHandler frequencyCheckHandler;
        private final WhiteListCheckHandler whiteListCheckHandler;

        public CheckChainBuilder(ParamCheckHandler paramCheckHandler, AuthCheckHandler authCheckHandler,
                                StatusCheckHandler statusCheckHandler, AmountCheckHandler amountCheckHandler,
                                RiskCheckHandler riskCheckHandler, ConfigCheckHandler configCheckHandler,
                                TimeCheckHandler timeCheckHandler, FrequencyCheckHandler frequencyCheckHandler,
                                WhiteListCheckHandler whiteListCheckHandler) {
            this.paramCheckHandler = paramCheckHandler;
            this.authCheckHandler = authCheckHandler;
            this.statusCheckHandler = statusCheckHandler;
            this.amountCheckHandler = amountCheckHandler;
            this.riskCheckHandler = riskCheckHandler;
            this.configCheckHandler = configCheckHandler;
            this.timeCheckHandler = timeCheckHandler;
            this.frequencyCheckHandler = frequencyCheckHandler;
            this.whiteListCheckHandler = whiteListCheckHandler;
        }

        public CheckChain build(List<String> handlerIds) {
            CheckChain chain = new CheckChain("CUSTOM_CHECK_CHAIN");
            for (String handlerId : handlerIds) {
                CheckHandler handler = createHandler(handlerId);
                if (handler != null) {
                    chain.addHandler(handler);
                }
            }
            return chain;
        }

        public CheckChain buildFullChain() {
            CheckChain chain = new CheckChain("FULL_CHECK_CHAIN");
            chain.addHandler(paramCheckHandler)
                 .addHandler(authCheckHandler)
                 .addHandler(statusCheckHandler)
                 .addHandler(amountCheckHandler)
                 .addHandler(riskCheckHandler)
                 .addHandler(configCheckHandler)
                 .addHandler(timeCheckHandler)
                 .addHandler(frequencyCheckHandler)
                 .addHandler(whiteListCheckHandler);
            return chain;
        }

        public CheckChain buildSecureChain() {
            CheckChain chain = new CheckChain("SECURE_CHECK_CHAIN");
            chain.addHandler(paramCheckHandler)
                 .addHandler(authCheckHandler)
                 .addHandler(amountCheckHandler)
                 .addHandler(riskCheckHandler)
                 .addHandler(whiteListCheckHandler);
            return chain;
        }

        private CheckHandler createHandler(String handlerId) {
            switch (handlerId.toUpperCase()) {
                case "PARAM":
                    return paramCheckHandler;
                case "AUTH":
                    return authCheckHandler;
                case "STATUS":
                    return statusCheckHandler;
                case "AMOUNT":
                    return amountCheckHandler;
                case "RISK":
                    return riskCheckHandler;
                case "CONFIG":
                    return configCheckHandler;
                case "TIME":
                    return timeCheckHandler;
                case "FREQUENCY":
                    return frequencyCheckHandler;
                case "WHITELIST":
                    return whiteListCheckHandler;
                default:
                    return null;
            }
        }
    }
}
