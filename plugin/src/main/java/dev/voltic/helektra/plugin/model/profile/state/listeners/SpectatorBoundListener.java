package dev.voltic.helektra.plugin.model.profile.state.listeners;

import jakarta.inject.Inject;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpectatorBoundListener implements Listener {
  private UUID bound;

  @Inject
  public SpectatorBoundListener() {
  }

  public SpectatorBoundListener bind(UUID bound) {
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

  @EventHandler
  public void onBreak(BlockBreakEvent event) {
    if (!matches(event.getPlayer().getUniqueId())) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (!matches(event.getPlayer().getUniqueId())) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onDamage(EntityDamageEvent event) {
    if (event.getEntity() == null || event.getEntity().getUniqueId() == null) {
      return;
    }
    if (!matches(event.getEntity().getUniqueId())) {
      return;
    }
    event.setCancelled(true);
  }

  private boolean matches(UUID uuid) {
    return bound != null && bound.equals(uuid);
  }
}
