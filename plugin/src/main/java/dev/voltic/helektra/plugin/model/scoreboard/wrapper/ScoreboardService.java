package dev.voltic.helektra.plugin.model.scoreboard.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import dev.voltic.helektra.api.model.scoreboard.IScoreboard;
import dev.voltic.helektra.api.model.scoreboard.IScoreboardProvider;
import dev.voltic.helektra.api.model.scoreboard.IScoreboardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ScoreboardService implements IScoreboardService {

  private final Map<UUID, IScoreboard> scoreboards;
  private final List<IScoreboardProvider> providers;

  @Inject
  public ScoreboardService() {
    this.scoreboards = new ConcurrentHashMap<>();
    this.providers = new ArrayList<>();
  }

  @Override
  public IScoreboard createScoreboard(Player player) {
    removeScoreboard(player.getUniqueId());

    IScoreboard scoreboard = new FastBoardAdapter(player);
    scoreboards.put(player.getUniqueId(), scoreboard);

    return scoreboard;
  }

  @Override
  public Optional<IScoreboard> getScoreboard(UUID uuid) {
    return Optional.ofNullable(scoreboards.get(uuid));
  }

  @Override
  public Optional<IScoreboard> getScoreboard(Player player) {
    return getScoreboard(player.getUniqueId());
  }

  @Override
  public void removeScoreboard(UUID uuid) {
    IScoreboard scoreboard = scoreboards.remove(uuid);
    if (scoreboard != null && !scoreboard.isDeleted()) {
      scoreboard.delete();
    }
  }

  @Override
  public void removeScoreboard(Player player) {
    removeScoreboard(player.getUniqueId());
  }

  @Override
  public void updateAll() {
    scoreboards.values().forEach(scoreboard -> {
      if (!scoreboard.isDeleted() && scoreboard.getPlayer().isOnline()) {
        updateScoreboard(scoreboard);
      }
    });
  }

  @Override
  public boolean hasScoreboard(UUID uuid) {
    return scoreboards.containsKey(uuid);
  }

  @Override
  public boolean hasScoreboard(Player player) {
    return hasScoreboard(player.getUniqueId());
  }

  public void registerProvider(IScoreboardProvider provider) {
    providers.add(provider);
  }

  public void unregisterProvider(IScoreboardProvider provider) {
    providers.remove(provider);
  }

  protected void updateScoreboard(IScoreboard scoreboard) {
    Player player = scoreboard.getPlayer();

    for (IScoreboardProvider provider : providers) {
      if (provider.shouldDisplay(player)) {
        scoreboard.updateTitle(provider.getTitle(player));
        scoreboard.updateLines(provider.getLines(player));
        return;
      }
    }
  }
}
