package dev.voltic.helektra.api.model.arena.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArenaTpsDegradedEvent {
    private double currentTps;
    private double threshold;
    private Instant timestamp;

    public ArenaTpsDegradedEvent(double currentTps, double threshold) {
        this.currentTps = currentTps;
        this.threshold = threshold;
        this.timestamp = Instant.now();
    }
}
