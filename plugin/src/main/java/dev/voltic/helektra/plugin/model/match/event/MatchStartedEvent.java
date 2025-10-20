package dev.voltic.helektra.plugin.model.match.event;

import dev.voltic.helektra.api.model.match.IMatch;

public class MatchStartedEvent extends MatchEvent {
    public MatchStartedEvent(IMatch match) {
        super(match);
    }
}
