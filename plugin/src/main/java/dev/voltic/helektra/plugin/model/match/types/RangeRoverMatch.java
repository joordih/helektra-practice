package dev.voltic.helektra.plugin.model.match.types;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.api.model.match.Participant;
import dev.voltic.helektra.plugin.model.match.Match;

import java.util.List;
import java.util.UUID;

public class RangeRoverMatch extends Match {
    private int rangeLimit;
    private long roundDuration;

    public RangeRoverMatch(String matchId, Arena arena, IKit kit, List<UUID> participantIds) {
        super();
        this.setMatchId(matchId);
        this.setMatchType(MatchType.RANGE_ROVER);
        this.setArena(arena);
        this.setKit(kit);
        this.setStartTime(System.currentTimeMillis());
        this.rangeLimit = 50;
        this.roundDuration = 300000;
        this.initializeParticipants(participantIds);
    }

    private void initializeParticipants(List<UUID> participantIds) {
        participantIds.forEach(uuid -> {
            this.getParticipants().add(
                new Participant(
                    uuid, null, 0, 0, true
                )
            );
        });
    }

    public int getRangeLimit() {
        return rangeLimit;
    }

    public void setRangeLimit(int rangeLimit) {
        this.rangeLimit = rangeLimit;
    }

    public long getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(long roundDuration) {
        this.roundDuration = roundDuration;
    }

    public int getAlivePlayers() {
        return (int) this.getParticipants().stream()
            .filter(p -> p.isAlive())
            .count();
    }

    public boolean hasWinner() {
        return getAlivePlayers() <= 1;
    }

    public long getRemainingRoundTime() {
        if (this.getCurrentRound().isEmpty()) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - this.getCurrentRound().get().getStartTime();
        return Math.max(0, roundDuration - elapsed);
    }
}
