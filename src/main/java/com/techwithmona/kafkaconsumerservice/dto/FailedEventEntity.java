package com.techwithmona.kafkaconsumerservice.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name="failed_events")
public class FailedEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String topic;

    @Column(name = "kafka_partition")
    private Integer partition;

    @Column(name = "kafka_offset")
    private Long offset;


    private String orderId;
    private OrderEventType type;
    private Integer quantity;
    private String timestamp;

    private String errorMessage;

    private Instant createdAt = Instant.now();

    public FailedEventEntity() {
    }
}
