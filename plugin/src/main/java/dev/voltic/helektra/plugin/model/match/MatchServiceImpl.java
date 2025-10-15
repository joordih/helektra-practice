package dev.voltic.helektra.plugin.model.match;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.match.MatchStatus;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.api.model.match.Participant;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MatchServiceImpl implements IMatchService {
    private final MatchRepository repository;

    @Override
    public IMatch createMatch(MatchType type, Arena arena, IKit kit, List<UUID> participantIds) {
        String matchId = generateMatchId();
        
        List<Participant> participants = new ArrayList<>();
        for (UUID uniqueId : participantIds) {
            participants.add(Participant.builder()
                .uniqueId(uniqueId)
                .kills(0)
                .deaths(0)
                .alive(true)
                .build());
        }

        Match match = Match.builder()
            .matchId(matchId)
            .matchType(type)
            .status(MatchStatus.WAITING)
            .arena(arena)
            .kit(kit)
            .participants(participants)
            .spectators(new ArrayList<>())
            .rounds(new ArrayList<>())
            .startTime(System.currentTimeMillis())
            .endTime(0)
            .build();

        repository.save(match);
        return match;
    }

    @Override
    public Optional<IMatch> getMatch(String matchId) {
        return repository.findById(matchId);
    }

    @Override
    public Optional<IMatch> getMatchByParticipant(UUID uniqueId) {
        return repository.findByParticipant(uniqueId);
    }

    @Override
    public List<IMatch> getActiveMatches() {
        return repository.findActive();
    }

    @Override
    public List<IMatch> getAllMatches() {
        return repository.findAll();
    }

    @Override
    public void endMatch(String matchId) {
        Optional<IMatch> matchOpt = repository.findById(matchId);
        if (matchOpt.isPresent()) {
            IMatch match = matchOpt.get();
            match.setStatus(MatchStatus.ENDED);
            if (match instanceof Match) {
                ((Match) match).setEndTime(System.currentTimeMillis());
            }
            repository.save(match);
        }
    }

    @Override
    public void removeMatch(String matchId) {
        repository.delete(matchId);
    }

    @Override
    public void saveMatch(IMatch match) {
        repository.save(match);
    }

    private String generateMatchId() {
        return "match_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
