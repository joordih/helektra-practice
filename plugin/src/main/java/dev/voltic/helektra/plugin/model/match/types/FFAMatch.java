package dev.voltic.helektra.plugin.model.match.types;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.plugin.model.match.Match;

import java.util.List;
import java.util.UUID;

public class FFAMatch extends Match {
    
    public FFAMatch(String matchId, Arena arena, IKit kit, List<UUID> participantIds) {
        super();
        this.setMatchId(matchId);
        this.setMatchType(MatchType.FFA);
        this.setArena(arena);
        this.setKit(kit);
        this.setStartTime(System.currentTimeMillis());
        this.initializeParticipants(participantIds);
    }

    private void initializeParticipants(List<UUID> participantIds) {
        participantIds.forEach(uuid -> {
            this.getParticipants().add(
                new dev.voltic.helektra.api.model.match.Participant(
                    uuid, null, 0, 0, true
                )
            );
        });
    }

    public int getAlivePlayers() {
        return (int) this.getParticipants().stream()
            .filter(p -> p.isAlive())
            .count();
    }

    public boolean hasWinner() {
        return getAlivePlayers() <= 1;
    }
}
