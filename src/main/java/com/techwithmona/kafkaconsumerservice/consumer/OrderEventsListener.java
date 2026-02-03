package com.techwithmona.kafkaconsumerservice.consumer;

import com.techwithmona.kafkaconsumerservice.dto.OrderEvent;
import com.techwithmona.kafkaconsumerservice.validation.OrderEventValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsListener {

    private final OrderEventValidator orderEventValidator;

    public OrderEventsListener(OrderEventValidator orderEventValidator) {
        this.orderEventValidator = orderEventValidator;
    }


    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 2000,multiplier = 2.0), dltTopicSuffix = "-dlt")

    @KafkaListener(topics = "${app.kafka.topic.order-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(OrderEvent event, Acknowledgment ack) {
        orderEventValidator.validate(event);
        System.out.println("Consumed event: orderId=" + event.getOrderId()
                + ", type=" + event.getType()
                + ", qty=" + event.getQuantity()
                + ", timestamp=" + event.getTimestamp());

        ack.acknowledge();
    }

    @KafkaListener(topics = "${app.kafka.topic.order-events}-dlt", groupId = "${spring.kafka.consumer.group-id}-dlt")
    public void onDltMessage(OrderEvent event) {
        System.out.println("DLT consumed event: orderId=" + event.getOrderId()
        + ", type=" + event.getType()
        + ", qty=" + event.getQuantity());
    }
}
