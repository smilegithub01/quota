package com.bank.quota.core.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuotaEventHandler {
    
    @Async
    @EventListener
    public void handleQuotaAdjustedEvent(QuotaAdjustedEvent event) {
        log.info("处理额度调整事件 - 额度ID: {}, 类型: {}, 调整金额: {}, 操作人: {}",
                event.getQuotaId(), event.getQuotaType(), 
                event.getAdjustmentAmount(), event.getOperator());
    }
    
    @Async
    @EventListener
    public void handleQuotaOccupiedEvent(QuotaOccupiedEvent event) {
        log.info("处理额度占用事件 - 额度ID: {}, 业务参考号: {}, 占用金额: {}, 操作人: {}",
                event.getQuotaId(), event.getBusinessRefNo(),
                event.getOccupyAmount(), event.getOperator());
    }
    
    @Async
    @EventListener
    public void handleQuotaReleasedEvent(QuotaReleasedEvent event) {
        log.info("处理额度释放事件 - 额度ID: {}, 业务参考号: {}, 释放金额: {}, 操作人: {}",
                event.getQuotaId(), event.getBusinessRefNo(),
                event.getReleaseAmount(), event.getOperator());
    }
    
    @Async
    @EventListener
    public void handleQuotaFrozenEvent(QuotaFrozenEvent event) {
        log.info("处理额度冻结事件 - 额度ID: {}, 类型: {}, 原因: {}, 操作人: {}",
                event.getQuotaId(), event.getQuotaType(),
                event.getReason(), event.getOperator());
    }
    
    @Async
    @EventListener
    public void handleQuotaUnfrozenEvent(QuotaUnfrozenEvent event) {
        log.info("处理额度解冻事件 - 额度ID: {}, 类型: {}, 操作人: {}",
                event.getQuotaId(), event.getQuotaType(), event.getOperator());
    }
}
