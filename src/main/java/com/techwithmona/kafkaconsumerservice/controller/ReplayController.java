package com.techwithmona.kafkaconsumerservice.controller;


import com.techwithmona.kafkaconsumerservice.replay.ReplayManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class ReplayController {

    private final ReplayManager replayManager;

    public ReplayController(ReplayManager replayManager) {
        this.replayManager = replayManager;
    }

    @PostMapping("/replay")
    public ResponseEntity<String> replay(
            @RequestParam String topic,
            @RequestParam int partition,
            @RequestParam long offset
    ) {
        replayManager.requestSeek(topic, partition, offset);
        return ResponseEntity.accepted().body(
                "Replay queued for " + topic + "-" + partition + " to offset " + offset +
                        ". Will execute when this instance owns the partition."
        );
    }

}
