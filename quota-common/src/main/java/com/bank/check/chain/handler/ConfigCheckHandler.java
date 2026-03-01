package com.bank.check.chain.handler;

import com.bank.check.chain.ChainRequest;
import com.bank.check.chain.ChainResponse;
import com.bank.check.chain.CheckHandler;

import java.util.Map;

/**
 * 配置校验处理器
 * 
 * 校验业务配置是否符合要求
 */
public class ConfigCheckHandler extends CheckHandler {

    private static final String ERROR_CODE = "CONFIG_CHECK_FAILED";
    private static final String DEFAULT_ERROR_MSG = "配置校验失败";

    private Map<String, Object> configRules;

    public ConfigCheckHandler() {
        super("CONFIG_CHECK", "配置校验");
        this.order = 6;
    }

    public ConfigCheckHandler(Map<String, Object> configRules) {
        super("CONFIG_CHECK", "配置校验");
        this.configRules = configRules;
        this.order = 6;
    }

    @Override
    public boolean handle(ChainRequest request, ChainResponse response) {
        long startTime = System.currentTimeMillis();

        if (request.isSkipAllCheck() || request.isSkipConfigCheck()) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));
            return continueChain(request, response);
        }

        if (!response.isPassed()) {
            return false;
        }

        try {
            String productCode = request.getProductCode();
            String channel = request.getChannel();

            if (productCode == null || productCode.isEmpty()) {
                response.addCheckDetail(ChainResponse.CheckDetail.fail(
                    checkItemId, checkItemName, ERROR_CODE, "产品编码不能为空",
                    System.currentTimeMillis() - startTime));
                response.setPassed(false);
                response.setFailedCheckItemId(checkItemId);
                response.setFailedCheckItemName(checkItemName);
                response.setErrorCode(ERROR_CODE);
                response.setErrorMessage("产品编码不能为空");
                return false;
            }

            if (!validateConfig(productCode, channel, request)) {
                String errorMsg = "配置校验失败";
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
                checkItemId, checkItemName, ERROR_CODE, "配置校验异常: " + e.getMessage(),
                System.currentTimeMillis() - startTime));
            response.setPassed(false);
            response.setErrorCode(ERROR_CODE);
            response.setErrorMessage("配置校验异常: " + e.getMessage());
            return false;
        }

        return continueChain(request, response);
    }

    private boolean validateConfig(String productCode, String channel, ChainRequest request) {
        if (configRules != null && !configRules.isEmpty()) {
            Object productEnabled = configRules.get(productCode + "_enabled");
            if (productEnabled != null && !Boolean.parseBoolean(productEnabled.toString())) {
                return false;
            }
        }
        return true;
    }

    public ConfigCheckHandler setConfigRules(Map<String, Object> configRules) {
        this.configRules = configRules;
        return this;
    }
}
