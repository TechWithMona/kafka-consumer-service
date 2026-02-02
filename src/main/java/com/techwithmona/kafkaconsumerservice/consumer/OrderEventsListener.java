package com.techwithmona.kafkaconsumerservice.consumer;

import com.techwithmona.kafkaconsumerservice.dto.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsListener {

    @KafkaListener(topics = "order-event")
    public void onMessage(OrderEvent event) {
        System.out.println("Consumed event: orderId=" + event.getOrderId()
                + ", type=" + event.getType()
                + ", qty=" + event.getQuantity()
                + ", timestamp=" + event.getTimestamp());
    }
}
