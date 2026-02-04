package com.techwithmona.kafkaconsumerservice.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Collection;

@Configuration
public class KafkaListenerConfig {

    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory) {

        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, Object>();

        //Tells Spring Kafka: use this consumerFactory (bootstrap servers, group id, deserializers, etc.)
        containerFactory.setConsumerFactory(consumerFactory);

        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        containerFactory.getContainerProperties().setConsumerRebalanceListener(new ConsumerAwareRebalanceListener() {

            // Called when Kafka is taking partitions away from THIS consumer.
            // Why you care: if you processed messages but didn't ack/commit yet,
            // next consumer may re-read them => duplicates.
            @Override
            public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer,
                                                        Collection<TopicPartition> partitions) {
                System.out.println("REBALANCE: partitions REVOKED (before commit): " + partitions);
            }

            // Called when Kafka assigns partitions to THIS consumer.
            // Why you care: tells you exactly which partitions this instance owns now.
            @Override
            public void onPartitionsAssigned(Consumer<?, ?> consumer,
                                             Collection<TopicPartition> partitions) {
                System.out.println("REBALANCE: partitions ASSIGNED: " + partitions);
            }
        });

        return containerFactory;
    }
}
