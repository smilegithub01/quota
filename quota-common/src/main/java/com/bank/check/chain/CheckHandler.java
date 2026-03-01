package com.bank.check.chain;

/**
 * 抽象校验处理器
 * 
 * 责任链模式中的Handler抽象类
 * 定义校验处理器的标准接口
 */
public abstract class CheckHandler {

    protected CheckHandler next;
    protected String checkItemId;
    protected String checkItemName;
    protected int order;
    protected boolean enabled = true;

    public CheckHandler() {
    }

    public CheckHandler(String checkItemId, String checkItemName) {
        this.checkItemId = checkItemId;
        this.checkItemName = checkItemName;
    }

    public void setNext(CheckHandler next) {
        this.next = next;
    }

    public CheckHandler getNext() {
        return next;
    }

    public CheckHandler appendNext(CheckHandler next) {
        if (this.next == null) {
            this.next = next;
        } else {
            this.next.appendNext(next);
        }
        return this;
    }

    public abstract boolean handle(ChainRequest request, ChainResponse response);

    protected boolean continueChain(ChainRequest request, ChainResponse response) {
        if (next != null && response.isPassed()) {
            return next.handle(request, response);
        }
        return response.isPassed();
    }

    public String getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(String checkItemId) {
        this.checkItemId = checkItemId;
    }

    public String getCheckItemName() {
        return checkItemName;
    }

    public void setCheckItemName(String checkItemName) {
        this.checkItemName = checkItemName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
