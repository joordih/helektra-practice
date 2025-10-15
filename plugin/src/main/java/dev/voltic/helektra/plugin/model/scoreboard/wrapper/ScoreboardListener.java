package dev.voltic.helektra.plugin.model.scoreboard.wrapper;

import dev.voltic.helektra.api.model.scoreboard.IScoreboardService;
import jakarta.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListener implements Listener {

  private final IScoreboardService scoreboardService;

  @Inject
  public ScoreboardListener(IScoreboardService scoreboardService) {
    this.scoreboardService = scoreboardService;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    scoreboardService.createScoreboard(event.getPlayer());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    scoreboardService.removeScoreboard(event.getPlayer());
  }
}
