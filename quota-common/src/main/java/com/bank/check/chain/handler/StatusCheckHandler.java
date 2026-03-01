package com.bank.check.chain.handler;

import com.bank.check.chain.ChainRequest;
import com.bank.check.chain.ChainResponse;
import com.bank.check.chain.CheckHandler;

import java.util.Map;

/**
 * 状态校验处理器
 * 
 * 校验业务状态是否允许操作
 */
public class StatusCheckHandler extends CheckHandler {

    private static final String ERROR_CODE = "STATUS_CHECK_FAILED";
    private static final String DEFAULT_ERROR_MSG = "状态校验失败";

    private Map<String, String> statusConfig;

    public StatusCheckHandler() {
        super("STATUS_CHECK", "状态校验");
        this.order = 3;
    }

    public StatusCheckHandler(Map<String, String> statusConfig) {
        super("STATUS_CHECK", "状态校验");
        this.statusConfig = statusConfig;
        this.order = 3;
    }

    @Override
    public boolean handle(ChainRequest request, ChainResponse response) {
        long startTime = System.currentTimeMillis();

        if (request.isSkipAllCheck() || request.isSkipStatusCheck()) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));
            return continueChain(request, response);
        }

        if (!response.isPassed()) {
            return false;
        }

        try {
            Object currentStatus = request.getExtData("currentStatus");
            Object targetStatus = request.getExtData("targetStatus");

            if (currentStatus == null) {
                response.addCheckDetail(ChainResponse.CheckDetail.pass(
                    checkItemId, checkItemName, System.currentTimeMillis() - startTime));
                return continueChain(request, response);
            }

            if (!validateStatus(currentStatus.toString(), targetStatus != null ? targetStatus.toString() : null)) {
                String errorMsg = "当前状态不允许此操作";
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
                checkItemId, checkItemName, ERROR_CODE, "状态校验异常: " + e.getMessage(),
                System.currentTimeMillis() - startTime));
            response.setPassed(false);
            response.setErrorCode(ERROR_CODE);
            response.setErrorMessage("状态校验异常: " + e.getMessage());
            return false;
        }

        return continueChain(request, response);
    }

    private boolean validateStatus(String currentStatus, String targetStatus) {
        if (statusConfig != null && !statusConfig.isEmpty()) {
            String allowedTargets = statusConfig.get(currentStatus);
            if (allowedTargets != null && targetStatus != null) {
                return allowedTargets.contains(targetStatus);
            }
        }
        return true;
    }

    public StatusCheckHandler setStatusConfig(Map<String, String> statusConfig) {
        this.statusConfig = statusConfig;
        return this;
    }
}
