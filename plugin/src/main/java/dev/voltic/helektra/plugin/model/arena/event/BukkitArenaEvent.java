package dev.voltic.helektra.plugin.model.arena.event;

import java.time.Instant;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import lombok.Getter;

@Getter
public abstract class BukkitArenaEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final String arenaId;
    private final ArenaInstance instance;
    private final Instant timestamp;

    protected BukkitArenaEvent(String arenaId, ArenaInstance instance, Instant timestamp) {
        this.arenaId = arenaId;
        this.instance = instance;
        this.timestamp = timestamp;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
