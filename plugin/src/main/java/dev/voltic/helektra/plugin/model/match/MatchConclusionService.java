package dev.voltic.helektra.plugin.model.match;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.IArenaVisibilityService;
import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.match.Participant;
import dev.voltic.helektra.plugin.model.profile.LobbyService;
import dev.voltic.helektra.plugin.nms.strategy.NmsStrategies;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
@SuppressWarnings("deprecation")
public class MatchConclusionService {

  private final JavaPlugin plugin;
  private final MatchSettings settings;
  private final SpectatorService spectatorService;
  private final MatchArenaTracker arenaTracker;
  private final IMatchService matchService;
  private final LobbyService lobbyService;
  private final IArenaVisibilityService visibilityService;
  private final Set<String> concludingMatches = ConcurrentHashMap.newKeySet();

  @Inject
  public MatchConclusionService(
    JavaPlugin plugin,
    MatchSettings settings,
    SpectatorService spectatorService,
    MatchArenaTracker arenaTracker,
    IMatchService matchService,
    LobbyService lobbyService,
    IArenaVisibilityService visibilityService
  ) {
    this.plugin = plugin;
    this.settings = settings;
    this.spectatorService = spectatorService;
    this.arenaTracker = arenaTracker;
    this.matchService = matchService;
    this.lobbyService = lobbyService;
    this.visibilityService = visibilityService;
  }

  public void conclude(
    IMatch match,
    Participant loser,
    Optional<Participant> winnerOpt
  ) {
    if (match == null || loser == null) {
      return;
    }
    if (!concludingMatches.add(match.getMatchId())) {
      return;
    }
    ArenaInstance instance = arenaTracker.get(match.getMatchId());
    Player loserPlayer = Bukkit.getPlayer(loser.getUniqueId());
    winnerOpt.ifPresent(winner -> sendTitles(match, loserPlayer, winner));
    if (loserPlayer != null) {
      Bukkit.getScheduler().runTask(plugin, () -> {
        if (loserPlayer.isOnline()) {
          loserPlayer.spigot().respawn();
          loserPlayer.setHealth(loserPlayer.getMaxHealth());
          loserPlayer.setFoodLevel(20);
          loserPlayer.setSaturation(20);
          spectatorService.enterMatchSpectator(
            match,
            instance,
            loserPlayer,
            true
          );
        }
      });
    }
    int delayTicks = Math.max(0, settings.getEndDelaySeconds()) * 20;
    Bukkit.getScheduler().runTaskLater(
      plugin,
      () -> finalizeMatch(match, winnerOpt),
      delayTicks
    );
  }

  private void sendTitles(
    IMatch match,
    Player loserPlayer,
    Participant winner
  ) {
    Player winnerPlayer = Bukkit.getPlayer(winner.getUniqueId());
    String loserName = resolveName(match, loserPlayer, winner.getUniqueId());
    String winnerName = resolveName(match, winnerPlayer, winner.getUniqueId());
    if (winnerPlayer != null && winnerPlayer.isOnline()) {
      String title = settings
        .getWinTitle()
        .getTitle()
        .replace("%opponent%", loserName);
      String subtitle = settings
        .getWinTitle()
        .getSubtitle()
        .replace("%opponent%", loserName);
      NmsStrategies.TITLE.execute(
        winnerPlayer,
        title,
        subtitle,
        settings.getWinTitle().getFadeInTicks(),
        settings.getWinTitle().getStayTicks(),
        settings.getWinTitle().getFadeOutTicks()
      );
    }
    if (loserPlayer != null && loserPlayer.isOnline()) {
      String title = settings
        .getLoseTitle()
        .getTitle()
        .replace("%opponent%", winnerName);
      String subtitle = settings
        .getLoseTitle()
        .getSubtitle()
        .replace("%opponent%", winnerName);
      NmsStrategies.TITLE.execute(
        loserPlayer,
        title,
        subtitle,
        settings.getLoseTitle().getFadeInTicks(),
        settings.getLoseTitle().getStayTicks(),
        settings.getLoseTitle().getFadeOutTicks()
      );
    }
  }

  private String resolveName(IMatch match, Player player, UUID fallbackId) {
    if (player != null) {
      return player.getName();
    }
    String name = match
      .getParticipant(fallbackId)
      .map(Participant::getName)
      .orElse(Bukkit.getOfflinePlayer(fallbackId).getName());
    if (name == null || name.isEmpty()) {
      return "Unknown";
    }
    return name;
  }

  private void finalizeMatch(IMatch match, Optional<Participant> winnerOpt) {
    String matchId = match.getMatchId();
    Set<UUID> release = new HashSet<>();
    release.addAll(spectatorService.concludeMatch(match));
    match
      .getParticipants()
      .forEach(participant -> {
        UUID playerId = participant.getUniqueId();
        release.add(playerId);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
          lobbyService.send(player);
        }
      });
    if (!release.isEmpty()) {
      visibilityService.releasePlayers(release);
    }
    try {
      matchService.endMatch(matchId, winnerOpt);
    } finally {
      concludingMatches.remove(matchId);
    }
  }
}
