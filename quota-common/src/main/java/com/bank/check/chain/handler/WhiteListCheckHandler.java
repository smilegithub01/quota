package com.bank.check.chain.handler;

import com.bank.check.chain.ChainRequest;
import com.bank.check.chain.ChainResponse;
import com.bank.check.chain.CheckHandler;
import com.bank.whitelist.common.CheckResult;
import com.bank.whitelist.core.CheckContext;
import com.bank.whitelist.core.WhitelistManager;
import com.bank.whitelist.common.WhitelistRule.RuleType;

/**
 * 白名单校验处理器
 * 
 * 集成现有白名单系统进行校验
 */
public class WhiteListCheckHandler extends CheckHandler {

    private static final String ERROR_CODE = "WHITELIST_CHECK_FAILED";
    private static final String DEFAULT_ERROR_MSG = "白名单校验失败";

    private WhitelistManager whitelistManager;
    private String ruleId;

    public WhiteListCheckHandler() {
        super("WHITELIST_CHECK", "白名单校验");
        this.order = 9;
    }

    public WhiteListCheckHandler(WhitelistManager whitelistManager) {
        super("WHITELIST_CHECK", "白名单校验");
        this.whitelistManager = whitelistManager;
        this.order = 9;
    }

    public WhiteListCheckHandler(WhitelistManager whitelistManager, String ruleId) {
        super("WHITELIST_CHECK", "白名单校验");
        this.whitelistManager = whitelistManager;
        this.ruleId = ruleId;
        this.order = 9;
    }

    @Override
    public boolean handle(ChainRequest request, ChainResponse response) {
        long startTime = System.currentTimeMillis();

        if (request.isSkipAllCheck() || request.isSkipWhiteListCheck()) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));
            return continueChain(request, response);
        }

        if (!response.isPassed()) {
            return false;
        }

        if (whitelistManager == null) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));
            return continueChain(request, response);
        }

        try {
            CheckContext context = buildCheckContext(request);
            String errorMsg = validateWhitelist(context, request);
            
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

            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            response.addCheckDetail(ChainResponse.CheckDetail.fail(
                checkItemId, checkItemName, ERROR_CODE, "白名单校验异常: " + e.getMessage(),
                System.currentTimeMillis() - startTime));
            response.setPassed(false);
            response.setErrorCode(ERROR_CODE);
            response.setErrorMessage("白名单校验异常: " + e.getMessage());
            return false;
        }

        return continueChain(request, response);
    }

    private CheckContext buildCheckContext(ChainRequest request) {
        CheckContext context = new CheckContext();
        
        if (request.getIp() != null) {
            context.setIp(request.getIp());
        }
        if (request.getUserId() != null) {
            context.setUserId(request.getUserId());
        }
        if (request.getUsername() != null) {
            context.setUsername(request.getUsername());
        }
        if (request.getDeviceId() != null) {
            context.setDeviceId(request.getDeviceId());
        }
        if (request.getPhone() != null) {
            context.setPhone(request.getPhone());
        }
        if (request.getApiCode() != null) {
            context.setApiCode(request.getApiCode());
        }
        if (request.getBizCode() != null) {
            context.setBizCode(request.getBizCode());
        }
        
        return context;
    }

    private String validateWhitelist(CheckContext context, ChainRequest request) {
        if (ruleId != null && !ruleId.isEmpty()) {
            CheckResult result = whitelistManager.check(ruleId, context);
            if (!result.isPassed()) {
                return result.getErrorMessage() != null ? result.getErrorMessage() : "不在白名单中";
            }
            return null;
        }

        for (RuleType ruleType : RuleType.values()) {
            var rules = whitelistManager.getRulesByType(ruleType);
            for (var rule : rules) {
                CheckResult result = whitelistManager.check(rule.getRuleId(), context);
                if (result.isPassed()) {
                    return null;
                }
            }
        }

        return "不在白名单中";
    }

    public WhiteListCheckHandler setWhitelistManager(WhitelistManager whitelistManager) {
        this.whitelistManager = whitelistManager;
        return this;
    }

    public WhiteListCheckHandler setRuleId(String ruleId) {
        this.ruleId = ruleId;
        return this;
    }
}
