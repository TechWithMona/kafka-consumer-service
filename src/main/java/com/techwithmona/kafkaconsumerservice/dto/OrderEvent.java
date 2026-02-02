package com.techwithmona.kafkaconsumerservice.dto;

import java.time.Instant;

public class OrderEvent {
    private String orderId;
    private String type;
    private int quantity;
    private Instant timestamp;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}