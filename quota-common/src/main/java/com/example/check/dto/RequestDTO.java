package com.example.check.dto;
import lombok.Data;
@Data
public class RequestDTO {
    private Long userId;
    private String orderId;
    private Double amount;
}

