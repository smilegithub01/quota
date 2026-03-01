package com.bank.check.chain.handler;

import com.bank.check.chain.ChainRequest;
import com.bank.check.chain.ChainResponse;
import com.bank.check.chain.CheckHandler;

import java.util.Map;

/**
 * 参数校验处理器
 * 
 * 校验请求参数的有效性
 */
public class ParamCheckHandler extends CheckHandler {

    private static final String ERROR_CODE = "PARAM_CHECK_FAILED";
    private static final String DEFAULT_ERROR_MSG = "参数校验失败";

    public ParamCheckHandler() {
        super("PARAM_CHECK", "参数校验");
        this.order = 1;
    }

    @Override
    public boolean handle(ChainRequest request, ChainResponse response) {
        long startTime = System.currentTimeMillis();

        if (request.isSkipAllCheck() || request.isSkipParamCheck()) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));
            return continueChain(request, response);
        }

        if (!response.isPassed()) {
            return false;
        }

        try {
            Map<String, Object> params = request.getParams();
            if (params == null || params.isEmpty()) {
                response.addCheckDetail(ChainResponse.CheckDetail.fail(
                    checkItemId, checkItemName, ERROR_CODE, "请求参数不能为空",
                    System.currentTimeMillis() - startTime));
                response.setPassed(false);
                response.setFailedCheckItemId(checkItemId);
                response.setFailedCheckItemName(checkItemName);
                response.setErrorCode(ERROR_CODE);
                response.setErrorMessage("请求参数不能为空");
                return false;
            }

            if (!validateParams(params)) {
                String errorMsg = getErrorMessage(params);
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
                checkItemId, checkItemName, ERROR_CODE, "参数校验异常: " + e.getMessage(),
                System.currentTimeMillis() - startTime));
            response.setPassed(false);
            response.setErrorCode(ERROR_CODE);
            response.setErrorMessage("参数校验异常: " + e.getMessage());
            return false;
        }

        return continueChain(request, response);
    }

    private boolean validateParams(Map<String, Object> params) {
        if (params.containsKey("amount")) {
            Object amount = params.get("amount");
            if (amount instanceof Number) {
                return ((Number) amount).longValue() > 0;
            }
        }
        return true;
    }

    private String getErrorMessage(Map<String, Object> params) {
        if (params.containsKey("amount")) {
            Object amount = params.get("amount");
            if (amount instanceof Number) {
                if (((Number) amount).longValue() <= 0) {
                    return "金额必须大于0";
                }
            }
        }
        return DEFAULT_ERROR_MSG;
    }
}
