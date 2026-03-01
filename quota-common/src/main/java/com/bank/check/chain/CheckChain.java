package com.bank.check.chain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * 校验责任链管理器
 * 
 * 负责构建和管理校验责任链
 */
public class CheckChain {

    private CheckHandler head;
    private CheckHandler tail;
    private List<CheckHandler> handlers;
    private String chainName;
    private boolean enableBreakOnFail = true;
    private List<Consumer<ChainContext>> beforeCheckListeners;
    private List<Consumer<ChainContext>> afterCheckListeners;

    public CheckChain() {
        this.handlers = new ArrayList<>();
        this.beforeCheckListeners = new ArrayList<>();
        this.afterCheckListeners = new ArrayList<>();
    }

    public CheckChain(String chainName) {
        this();
        this.chainName = chainName;
    }

    public CheckChain addHandler(CheckHandler handler) {
        if (handler == null) {
            return this;
        }

        handlers.add(handler);

        if (head == null) {
            head = handler;
            tail = handler;
        } else {
            tail.setNext(handler);
            tail = handler;
        }

        return this;
    }

    public CheckChain addHandlers(List<CheckHandler> handlerList) {
        if (handlerList == null || handlerList.isEmpty()) {
            return this;
        }

        handlerList.sort(Comparator.comparingInt(CheckHandler::getOrder));

        for (CheckHandler handler : handlerList) {
            addHandler(handler);
        }

        return this;
    }

    public CheckChain removeHandler(String checkItemId) {
        if (head == null || checkItemId == null) {
            return this;
        }

        if (head.getCheckItemId().equals(checkItemId)) {
            head = head.getNext();
            handlers.remove(0);
            if (head == null) {
                tail = null;
            }
            return this;
        }

        CheckHandler current = head;
        while (current.getNext() != null) {
            if (current.getNext().getCheckItemId().equals(checkItemId)) {
                current.setNext(current.getNext().getNext());
                handlers.remove(current.getNext());
                if (current.getNext() == null) {
                    tail = current;
                }
                break;
            }
            current = current.getNext();
        }

        return this;
    }

    public ChainResponse execute(ChainRequest request) {
        ChainResponse response = new ChainResponse();
        response.setRequestId(request.getRequestId());

        ChainContext context = new ChainContext(request, response);

        notifyBeforeCheck(context);

        if (head == null) {
            response.setPassed(true);
            notifyAfterCheck(context);
            return response;
        }

        try {
            head.handle(request, response);
        } catch (Exception e) {
            response.setPassed(false);
            response.setErrorCode("CHAIN_EXECUTE_ERROR");
            response.setErrorMessage("责任链执行异常: " + e.getMessage());
        }

        response.calculateDuration();

        notifyAfterCheck(context);

        return response;
    }

    public CheckChain addBeforeCheckListener(Consumer<ChainContext> listener) {
        if (listener != null) {
            beforeCheckListeners.add(listener);
        }
        return this;
    }

    public CheckChain addAfterCheckListener(Consumer<ChainContext> listener) {
        if (listener != null) {
            afterCheckListeners.add(listener);
        }
        return this;
    }

    private void notifyBeforeCheck(ChainContext context) {
        for (Consumer<ChainContext> listener : beforeCheckListeners) {
            try {
                listener.accept(context);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyAfterCheck(ChainContext context) {
        for (Consumer<ChainContext> listener : afterCheckListeners) {
            try {
                listener.accept(context);
            } catch (Exception ignored) {
            }
        }
    }

    public CheckHandler getHead() {
        return head;
    }

    public CheckHandler getTail() {
        return tail;
    }

    public List<CheckHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }

    public String getChainName() {
        return chainName;
    }

    public CheckChain setChainName(String chainName) {
        this.chainName = chainName;
        return this;
    }

    public boolean isEnableBreakOnFail() {
        return enableBreakOnFail;
    }

    public CheckChain setEnableBreakOnFail(boolean enableBreakOnFail) {
        this.enableBreakOnFail = enableBreakOnFail;
        return this;
    }

    public int getHandlerCount() {
        return handlers.size();
    }

    public void clear() {
        head = null;
        tail = null;
        handlers.clear();
    }

    public static CheckChain createDefaultChain() {
        CheckChain chain = new CheckChain("DEFAULT_CHECK_CHAIN");
        chain.addHandler(new com.bank.check.chain.handler.ParamCheckHandler())
             .addHandler(new com.bank.check.chain.handler.AuthCheckHandler())
             .addHandler(new com.bank.check.chain.handler.StatusCheckHandler())
             .addHandler(new com.bank.check.chain.handler.AmountCheckHandler())
             .addHandler(new com.bank.check.chain.handler.RiskCheckHandler())
             .addHandler(new com.bank.check.chain.handler.ConfigCheckHandler())
             .addHandler(new com.bank.check.chain.handler.TimeCheckHandler())
             .addHandler(new com.bank.check.chain.handler.FrequencyCheckHandler())
             .addHandler(new com.bank.check.chain.handler.WhiteListCheckHandler());
        return chain;
    }

    public static CheckChain createSimpleChain() {
        CheckChain chain = new CheckChain("SIMPLE_CHECK_CHAIN");
        chain.addHandler(new com.bank.check.chain.handler.ParamCheckHandler())
             .addHandler(new com.bank.check.chain.handler.AuthCheckHandler())
             .addHandler(new com.bank.check.chain.handler.AmountCheckHandler());
        return chain;
    }

    public static class ChainContext {
        private ChainRequest request;
        private ChainResponse response;
        private long startTime;

        public ChainContext(ChainRequest request, ChainResponse response) {
            this.request = request;
            this.response = response;
            this.startTime = System.currentTimeMillis();
        }

        public ChainRequest getRequest() {
            return request;
        }

        public ChainResponse getResponse() {
            return response;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
