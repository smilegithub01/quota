package com.bank.quota.core.converter;

import java.util.List;

public interface BaseConverter<S, T> {
    
    T toDto(S source);
    
    S toEntity(T target);
    
    List<T> toDtoList(List<S> sourceList);
    
    List<S> toEntityList(List<T> targetList);
}
