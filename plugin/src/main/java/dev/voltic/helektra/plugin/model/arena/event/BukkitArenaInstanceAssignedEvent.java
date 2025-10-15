package dev.voltic.helektra.plugin.model.arena.event;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BukkitArenaInstanceAssignedEvent extends BukkitArenaEvent {
    private final UUID playerId;
    private final long waitTimeMs;

    public BukkitArenaInstanceAssignedEvent(String arenaId, ArenaInstance instance, UUID playerId, long waitTimeMs) {
        super(arenaId, instance, Instant.now());
        this.playerId = playerId;
        this.waitTimeMs = waitTimeMs;
    }
}
