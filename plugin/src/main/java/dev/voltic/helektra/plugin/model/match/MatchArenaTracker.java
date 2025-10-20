package dev.voltic.helektra.plugin.model.match;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;

@Singleton
public class MatchArenaTracker {

  private final Map<String, ArenaInstance> assignments =
    new ConcurrentHashMap<>();
  private final IArenaService arenaService;

  @Inject
  public MatchArenaTracker(IArenaService arenaService) {
    this.arenaService = arenaService;
  }

  public void register(String matchId, ArenaInstance instance) {
    if (matchId == null || instance == null) {
      return;
    }
    assignments.put(matchId, instance);
  }

  public CompletableFuture<Void> release(String matchId) {
    ArenaInstance instance = assignments.remove(matchId);
    if (instance == null) {
      return CompletableFuture.completedFuture(null);
    }
    return arenaService
      .releaseArena(instance)
      .whenComplete((ignored, error) -> {
        if (error == null) {
          return;
        }
        Throwable cause = error instanceof CompletionException
          ? error.getCause()
          : error;
        String message = cause != null && cause.getMessage() != null
          ? cause.getMessage()
          : String.valueOf(cause);
        Bukkit.getLogger().severe(
          TranslationUtils.translate(
            "match.arena-reset-failed",
            "match",
            matchId,
            "error",
            message
          )
        );
        assignments.put(matchId, instance);
      });
  }
}
