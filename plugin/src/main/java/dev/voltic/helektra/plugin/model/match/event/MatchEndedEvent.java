package dev.voltic.helektra.plugin.model.match.event;

import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.Participant;
import lombok.Getter;

import java.util.Optional;

@Getter
public class MatchEndedEvent extends MatchEvent {
    private final Optional<Participant> winner;

    public MatchEndedEvent(IMatch match, Optional<Participant> winner) {
        super(match);
        this.winner = winner;
    }
}
