package dev.voltic.helektra.plugin.model.profile.state.listeners;

import jakarta.inject.Inject;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class QueueStateBoundListener implements Listener {

  private UUID bound;

  @Inject
  public QueueStateBoundListener() {
  }

  public QueueStateBoundListener bind(UUID bound) {
    this.bound = bound;
    return this;
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    if (!matches(event.getPlayer().getUniqueId())) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlace(BlockPlaceEvent event) {
    if (!matches(event.getPlayer().getUniqueId())) {
      return;
    }
    event.setCancelled(true);
  }

  private boolean matches(UUID uuid) {
    return bound != null && bound.equals(uuid);
  }
}
