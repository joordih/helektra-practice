package dev.voltic.helektra.api.model.arena.event;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import lombok.Getter;

import java.time.Instant;

@Getter
public abstract class ArenaEvent {
    private final String arenaId;
    private final ArenaInstance instance;
    private final Instant timestamp;

    protected ArenaEvent(String arenaId, ArenaInstance instance, Instant timestamp) {
        this.arenaId = arenaId;
        this.instance = instance;
        this.timestamp = timestamp;
    }
}
