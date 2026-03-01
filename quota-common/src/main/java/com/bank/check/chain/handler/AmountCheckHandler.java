package com.bank.check.chain.handler;

import com.bank.check.chain.ChainRequest;
import com.bank.check.chain.ChainResponse;
import com.bank.check.chain.CheckHandler;

/**
 * 金额校验处理器
 * 
 * 校验交易金额是否在允许范围内
 */
public class AmountCheckHandler extends CheckHandler {

    private static final String ERROR_CODE = "AMOUNT_CHECK_FAILED";
    private static final String DEFAULT_ERROR_MSG = "金额校验失败";

    private Long minAmount;
    private Long maxAmount;
    private Long singleLimit;
    private Long dayLimit;

    public AmountCheckHandler() {
        super("AMOUNT_CHECK", "金额校验");
        this.order = 4;
    }

    public AmountCheckHandler(Long minAmount, Long maxAmount) {
        super("AMOUNT_CHECK", "金额校验");
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.order = 4;
    }

    @Override
    public boolean handle(ChainRequest request, ChainResponse response) {
        long startTime = System.currentTimeMillis();

        if (request.isSkipAllCheck() || request.isSkipAmountCheck()) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));
            return continueChain(request, response);
        }

        if (!response.isPassed()) {
            return false;
        }

        try {
            Long amount = request.getAmount();
            if (amount == null) {
                response.addCheckDetail(ChainResponse.CheckDetail.pass(
                    checkItemId, checkItemName, System.currentTimeMillis() - startTime));
                return continueChain(request, response);
            }

            String errorMsg = validateAmount(amount, request);
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
                checkItemId, checkItemName, ERROR_CODE, "金额校验异常: " + e.getMessage(),
                System.currentTimeMillis() - startTime));
            response.setPassed(false);
            response.setErrorCode(ERROR_CODE);
            response.setErrorMessage("金额校验异常: " + e.getMessage());
            return false;
        }

        return continueChain(request, response);
    }

    private String validateAmount(Long amount, ChainRequest request) {
        if (minAmount != null && amount < minAmount) {
            return "金额不能小于最小限制: " + minAmount;
        }
        if (maxAmount != null && amount > maxAmount) {
            return "金额不能超过最大限制: " + maxAmount;
        }
        if (singleLimit != null && amount > singleLimit) {
            return "单笔金额不能超过限制: " + singleLimit;
        }
        return null;
    }

    public AmountCheckHandler setMinAmount(Long minAmount) {
        this.minAmount = minAmount;
        return this;
    }

    public AmountCheckHandler setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
        return this;
    }

    public AmountCheckHandler setSingleLimit(Long singleLimit) {
        this.singleLimit = singleLimit;
        return this;
    }

    public AmountCheckHandler setDayLimit(Long dayLimit) {
        this.dayLimit = dayLimit;
        return this;
    }
}
