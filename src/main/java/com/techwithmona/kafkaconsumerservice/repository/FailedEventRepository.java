package com.techwithmona.kafkaconsumerservice.repository;

import com.techwithmona.kafkaconsumerservice.dto.FailedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedEventRepository extends JpaRepository<FailedEventEntity, Long> {
}
