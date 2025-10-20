package dev.voltic.helektra.api.model.arena.event;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ResetStrategy;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ArenaResetStartedEvent extends ArenaEvent {
    private final ResetStrategy strategy;
    private final long estimatedBlockCount;

    public ArenaResetStartedEvent(String arenaId, ArenaInstance instance, ResetStrategy strategy, long estimatedBlockCount) {
        super(arenaId, instance, Instant.now());
        this.strategy = strategy;
        this.estimatedBlockCount = estimatedBlockCount;
    }
}
