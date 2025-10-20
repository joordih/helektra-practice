package dev.voltic.helektra.plugin.model.match.listeners;

import dev.voltic.helektra.plugin.model.match.SpectatorService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Singleton
public class SpectatorSessionListener implements Listener {

  private final SpectatorService spectatorService;

  @Inject
  public SpectatorSessionListener(SpectatorService spectatorService) {
    this.spectatorService = spectatorService;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    handle(event.getPlayer());
  }

  @EventHandler
  public void onKick(PlayerKickEvent event) {
    handle(event.getPlayer());
  }

  private void handle(Player player) {
    if (spectatorService.isSpectating(player.getUniqueId())) {
      spectatorService.leaveSpectator(player);
    }
  }
}
