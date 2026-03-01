package com.bank.quota.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public abstract class BaseDomainEvent extends ApplicationEvent implements Serializable {
    
    private final LocalDateTime occurredOn;
    private final String eventType;
    
    public BaseDomainEvent(Object source, String eventType) {
        super(source);
        this.occurredOn = LocalDateTime.now();
        this.eventType = eventType;
    }
}
