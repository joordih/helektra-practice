package dev.voltic.helektra.plugin.model.arena.event;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ResetStrategy;
import lombok.Getter;

import java.time.Instant;

@Getter
public class BukkitArenaResetCompletedEvent extends BukkitArenaEvent {
    private final ResetStrategy strategy;
    private final long durationMs;
    private final long blocksReset;
    private final boolean success;

    public BukkitArenaResetCompletedEvent(String arenaId, ArenaInstance instance, ResetStrategy strategy, 
                                          long durationMs, long blocksReset, boolean success) {
        super(arenaId, instance, Instant.now());
        this.strategy = strategy;
        this.durationMs = durationMs;
        this.blocksReset = blocksReset;
        this.success = success;
    }
}
