package dev.voltic.helektra.api.model.arena.event;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ArenaInstanceAssignedEvent extends ArenaEvent {
    private final UUID playerId;
    private final long waitTimeMs;

    public ArenaInstanceAssignedEvent(String arenaId, ArenaInstance instance, UUID playerId, long waitTimeMs) {
        super(arenaId, instance, Instant.now());
        this.playerId = playerId;
        this.waitTimeMs = waitTimeMs;
    }
}
