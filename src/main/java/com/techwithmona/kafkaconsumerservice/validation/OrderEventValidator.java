package com.techwithmona.kafkaconsumerservice.validation;

import com.techwithmona.kafkaconsumerservice.dto.OrderEvent;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class OrderEventValidator {



    public void validate(OrderEvent orderEvent) {

        if(orderEvent.getQuantity() <= 1 ){
            throw new ValidationException("Quantity must be greater than 1");
        }
        if (orderEvent.getOrderId() == null || orderEvent.getOrderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
    }
}
