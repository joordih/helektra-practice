package dev.voltic.helektra.api.model.arena.event;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ArenaInstanceReleasedEvent extends ArenaEvent {
    private final long usageDurationMs;
    private final long blocksModified;

    public ArenaInstanceReleasedEvent(String arenaId, ArenaInstance instance, long usageDurationMs, long blocksModified) {
        super(arenaId, instance, Instant.now());
        this.usageDurationMs = usageDurationMs;
        this.blocksModified = blocksModified;
    }
}
