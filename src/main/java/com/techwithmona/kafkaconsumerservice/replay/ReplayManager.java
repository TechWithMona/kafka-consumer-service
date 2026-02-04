package com.techwithmona.kafkaconsumerservice.replay;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReplayManager {

    @Value("${kafka.replay.enabled:true}")
    private boolean replayEnabled;

    // Partitions currently owned by THIS instance
    private final Set<TopicPartition> ownedPartitions = ConcurrentHashMap.newKeySet();

    // Pending seek requests (topic-partition -> offset)
    private final Map<TopicPartition, Long> pendingSeeks = new ConcurrentHashMap<>();

    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments) {
        ownedPartitions.clear();
        ownedPartitions.addAll(assignments.keySet());
    }

    public void onPartitionsRevoked(Iterable<TopicPartition> partitions) {
        for (TopicPartition tp : partitions) {
            ownedPartitions.remove(tp);
            pendingSeeks.remove(tp);
        }
    }

    // Controller calls this: just ENQUEUE the request
    public void requestSeek(String topic, int partition, long offset) {
        if (!replayEnabled) {
            throw new IllegalStateException("Replay is disabled in this environment.");
        }
        TopicPartition tp = new TopicPartition(topic, partition);
        pendingSeeks.put(tp, offset);
    }

    // Listener calls this from consumer thread (safe)
    public void applyPendingSeeks(ConsumerSeekAware.ConsumerSeekCallback callback) {
        for (Map.Entry<TopicPartition, Long> entry : pendingSeeks.entrySet()) {
            TopicPartition tp = entry.getKey();
            Long offset = entry.getValue();

            if (ownedPartitions.contains(tp)) {
                callback.seek(tp.topic(), tp.partition(), offset);
                pendingSeeks.remove(tp); // remove after success
            }
        }
    }
}