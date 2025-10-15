package dev.voltic.helektra.plugin.model.match.event;

import dev.voltic.helektra.api.model.match.IMatch;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class MatchEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected final IMatch match;

    public MatchEvent(IMatch match) {
        this.match = match;
    }

    public IMatch getMatch() {
        return match;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
