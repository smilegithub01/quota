package com.bank.check.chain.service;

import com.bank.check.chain.*;
import com.bank.check.chain.handler.*;
import com.bank.whitelist.core.WhitelistManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 校验链服务门面
 * 
 * 提供简化API，支持链的动态配置
 */
public class CheckChainService {

    private static final Map<String, CheckChain> CHAIN_CACHE = new ConcurrentHashMap<>();
    private WhitelistManager whitelistManager;

    public CheckChainService() {
    }

    public CheckChainService(WhitelistManager whitelistManager) {
        this.whitelistManager = whitelistManager;
    }

    public void setWhitelistManager(WhitelistManager whitelistManager) {
        this.whitelistManager = whitelistManager;
    }

    public CheckChain createChain(String chainName) {
        CheckChain chain = new CheckChain(chainName);
        CHAIN_CACHE.put(chainName, chain);
        return chain;
    }

    public CheckChain getOrCreateChain(String chainName) {
        return CHAIN_CACHE.computeIfAbsent(chainName, this::createChain);
    }

    public CheckChain buildDefaultChain() {
        CheckChain chain = new CheckChain("DEFAULT");
        chain.addHandler(new ParamCheckHandler())
             .addHandler(new AuthCheckHandler())
             .addHandler(new StatusCheckHandler())
             .addHandler(new AmountCheckHandler())
             .addHandler(new RiskCheckHandler())
             .addHandler(new ConfigCheckHandler())
             .addHandler(new TimeCheckHandler())
             .addHandler(new FrequencyCheckHandler())
             .addHandler(new WhiteListCheckHandler(whitelistManager));
        return chain;
    }

    public CheckChain buildSimpleChain() {
        CheckChain chain = new CheckChain("SIMPLE");
        chain.addHandler(new ParamCheckHandler())
             .addHandler(new AuthCheckHandler())
             .addHandler(new AmountCheckHandler());
        return chain;
    }

    public CheckChain buildCustomChain(List<String> handlerIds) {
        CheckChain chain = new CheckChain("CUSTOM");
        
        for (String handlerId : handlerIds) {
            CheckHandler handler = createHandler(handlerId);
            if (handler != null) {
                chain.addHandler(handler);
            }
        }
        
        return chain;
    }

    private CheckHandler createHandler(String handlerId) {
        switch (handlerId) {
            case "PARAM":
                return new ParamCheckHandler();
            case "AUTH":
                return new AuthCheckHandler();
            case "STATUS":
                return new StatusCheckHandler();
            case "AMOUNT":
                return new AmountCheckHandler();
            case "RISK":
                return new RiskCheckHandler();
            case "CONFIG":
                return new ConfigCheckHandler();
            case "TIME":
                return new TimeCheckHandler();
            case "FREQUENCY":
                return new FrequencyCheckHandler();
            case "WHITELIST":
                return new WhiteListCheckHandler(whitelistManager);
            default:
                return null;
        }
    }

    public ChainResponse execute(String chainName, ChainRequest request) {
        CheckChain chain = CHAIN_CACHE.get(chainName);
        if (chain == null) {
            chain = buildDefaultChain();
            CHAIN_CACHE.put(chainName, chain);
        }
        return chain.execute(request);
    }

    public ChainResponse executeWithSkip(String chainName, ChainRequest request, String... skipChecks) {
        if (skipChecks != null) {
            for (String skip : skipChecks) {
                switch (skip.toUpperCase()) {
                    case "ALL":
                        request.skipAllCheck();
                        break;
                    case "PARAM":
                        request.skipParamCheck();
                        break;
                    case "AUTH":
                        request.skipAuthCheck();
                        break;
                    case "STATUS":
                        request.skipStatusCheck();
                        break;
                    case "AMOUNT":
                        request.skipAmountCheck();
                        break;
                    case "RISK":
                        request.skipRiskCheck();
                        break;
                    case "CONFIG":
                        request.skipConfigCheck();
                        break;
                    case "TIME":
                        request.skipTimeCheck();
                        break;
                    case "FREQUENCY":
                        request.skipFrequencyCheck();
                        break;
                    case "WHITELIST":
                        request.skipWhiteListCheck();
                        break;
                }
            }
        }
        return execute(chainName, request);
    }

    public void registerChain(String chainName, CheckChain chain) {
        CHAIN_CACHE.put(chainName, chain);
    }

    public CheckChain getChain(String chainName) {
        return CHAIN_CACHE.get(chainName);
    }

    public void removeChain(String chainName) {
        CHAIN_CACHE.remove(chainName);
    }

    public void clearChains() {
        CHAIN_CACHE.clear();
    }

    public Map<String, CheckChain> getAllChains() {
        return new ConcurrentHashMap<>(CHAIN_CACHE);
    }
}
