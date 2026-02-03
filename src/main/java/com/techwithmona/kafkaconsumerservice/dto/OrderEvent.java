package com.techwithmona.kafkaconsumerservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderEvent {
    @NotBlank
    private String orderId;

    @NotNull
    private OrderEventType type;

    @Min(value = 1 , message = "quantity must be more than 1")
    private int quantity;

    @NotBlank
    private String timestamp;
}