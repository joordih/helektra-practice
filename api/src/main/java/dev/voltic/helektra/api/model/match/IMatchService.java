package dev.voltic.helektra.api.model.match;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.kit.IKit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMatchService {
    IMatch createMatch(MatchType type, Arena arena, IKit kit, List<UUID> participants);
    Optional<IMatch> getMatch(String matchId);
    Optional<IMatch> getMatchByParticipant(UUID uniqueId);
    List<IMatch> getActiveMatches();
    List<IMatch> getAllMatches();

    default void endMatch(String matchId) {
        endMatch(matchId, Optional.empty());
    }

    void endMatch(String matchId, Optional<Participant> winner);
    void removeMatch(String matchId);
    void saveMatch(IMatch match);
}
