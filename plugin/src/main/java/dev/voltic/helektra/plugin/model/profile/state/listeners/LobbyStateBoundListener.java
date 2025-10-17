package dev.voltic.helektra.plugin.model.profile.state.listeners;

import jakarta.inject.Inject;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class LobbyStateBoundListener implements Listener {

  private UUID bound;

  @Inject
  public LobbyStateBoundListener() {}

  public LobbyStateBoundListener bind(UUID bound) {
    this.bound = bound;
    return this;
  }

  @EventHandler
  public void onPlayerDrop(PlayerDropItemEvent event) {
    if (!matches(event.getPlayer().getUniqueId())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onInventoryDragEvent(InventoryDragEvent event) {
    if (!matches(event.getWhoClicked().getUniqueId())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;
    var player = (Player) event.getWhoClicked();
    var clickedInventory = event.getClickedInventory();
    if (
      !matches(player.getUniqueId()) ||
      clickedInventory == null ||
      !clickedInventory.equals(player.getInventory())
    ) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerBreakBlock(BlockBreakEvent event) {
    if (!matches(event.getPlayer().getUniqueId())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerPlaceBlock(BlockPlaceEvent event) {
    if (!matches(event.getPlayer().getUniqueId())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerHungerChange(FoodLevelChangeEvent event) {
    if (!matches(event.getEntity().getUniqueId())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!matches(player.getUniqueId())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerAttack(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player damager)) return;
    if (!matches(damager.getUniqueId())) return;
    event.setCancelled(true);
  }

  private boolean matches(UUID uuid) {
    return bound != null && bound.equals(uuid);
  }
}
