package com.techwithmona.kafkaconsumerservice.consumer;

import com.techwithmona.kafkaconsumerservice.dto.FailedEventEntity;
import com.techwithmona.kafkaconsumerservice.dto.OrderEvent;
import com.techwithmona.kafkaconsumerservice.replay.ReplayManager;
import com.techwithmona.kafkaconsumerservice.repository.FailedEventRepository;
import com.techwithmona.kafkaconsumerservice.validation.OrderEventValidator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class OrderEventsListener implements ConsumerSeekAware {

    private final OrderEventValidator orderEventValidator;
    private final FailedEventRepository failedEventRepository;
    private final ReplayManager replayManager;

    public OrderEventsListener(OrderEventValidator orderEventValidator,
                               FailedEventRepository failedEventRepository,
                               ReplayManager replayManager) {
        this.orderEventValidator = orderEventValidator;
        this.failedEventRepository = failedEventRepository;
        this.replayManager = replayManager;
    }

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 2000, multiplier = 2.0),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = "${app.kafka.topic.order-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(OrderEvent event, Acknowledgment ack) {
        orderEventValidator.validate(event);

        System.out.println("Consumed event: orderId=" + event.getOrderId()
                + ", type=" + event.getType()
                + ", qty=" + event.getQuantity()
                + ", timestamp=" + event.getTimestamp());

        ack.acknowledge();
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        replayManager.onPartitionsAssigned(assignments);
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        replayManager.onPartitionsRevoked(partitions);
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        replayManager.applyPendingSeeks(callback);
    }

    @KafkaListener(topics = "${app.kafka.topic.order-events}-dlt", groupId = "${spring.kafka.consumer.group-id}-dlt")
    public void onDltMessage(OrderEvent event,
                             ConsumerRecord<String, OrderEvent> record,
                             @org.springframework.messaging.handler.annotation.Header(name = "kafka_exception-message", required = false) String errorMessage) {

        System.out.println("DLT consumed event: orderId=" + event.getOrderId());

        FailedEventEntity failedEventEntity = new FailedEventEntity();
        failedEventEntity.setTopic(record.topic());
        failedEventEntity.setPartition(record.partition());
        failedEventEntity.setOffset(record.offset());

        failedEventEntity.setOrderId(event.getOrderId());
        failedEventEntity.setType(event.getType());
        failedEventEntity.setQuantity(event.getQuantity());
        failedEventEntity.setTimestamp(event.getTimestamp());

        failedEventEntity.setErrorMessage(errorMessage);

        failedEventRepository.save(failedEventEntity);
    }
}