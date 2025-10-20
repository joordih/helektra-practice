package dev.voltic.helektra.plugin.model.arena.listeners;

import dev.voltic.helektra.plugin.model.arena.ArenaService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

@Singleton
public class ArenaWorldListener implements Listener {

  private final ArenaService arenaService;

  @Inject
  public ArenaWorldListener(ArenaService arenaService) {
    this.arenaService = arenaService;
  }

  @EventHandler
  public void onWorldLoad(WorldLoadEvent event) {
    arenaService.handleWorldLoaded(event.getWorld().getName());
  }
}
