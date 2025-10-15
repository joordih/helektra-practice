package dev.voltic.helektra.plugin.model.scoreboard.wrapper;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import dev.voltic.helektra.api.model.scoreboard.IScoreboardService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.scoreboard.HelektraScoreboardService;
import jakarta.inject.Inject;

public class ScoreboardUpdater {

  private final Helektra plugin;
  private final IScoreboardService scoreboardService;
  private BukkitTask task;

  @Inject
  public ScoreboardUpdater(Helektra plugin, IScoreboardService scoreboardService) {
    this.plugin = plugin;
    this.scoreboardService = scoreboardService;
  }

  public void start() {
    if (task != null) {
      task.cancel();
    }

    int updateInterval = 20;
    if (scoreboardService instanceof HelektraScoreboardService helektraService) {
      updateInterval = helektraService.getConfig().getUpdateInterval();
    }

    task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      scoreboardService.updateAll();
    }, 0L, updateInterval);
  }

  public void stop() {
    if (task != null) {
      task.cancel();
      task = null;
    }
  }
}
