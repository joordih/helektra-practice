package dev.voltic.helektra.plugin.model.match;

import dev.voltic.helektra.api.model.match.IMatch;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class MatchRepository {

  private final Map<String, IMatch> matches = new HashMap<>();

  public void save(IMatch match) {
    matches.put(match.getMatchId(), match);
  }

  public Optional<IMatch> findById(String matchId) {
    return Optional.ofNullable(matches.get(matchId));
  }

  public Optional<IMatch> findByParticipant(UUID uniqueId) {
    return matches
      .values()
      .stream()
      .filter(match -> !match.hasEnded())
      .filter(match ->
        match
          .getParticipants()
          .stream()
          .anyMatch(p -> p.getUniqueId().equals(uniqueId))
      )
      .findFirst();
  }

  public List<IMatch> findAll() {
    return new ArrayList<>(matches.values());
  }

  public List<IMatch> findActive() {
    return matches
      .values()
      .stream()
      .filter(match -> !match.hasEnded())
      .toList();
  }

  public void delete(String matchId) {
    matches.remove(matchId);
  }

  public void deleteAll() {
    matches.clear();
  }
}
