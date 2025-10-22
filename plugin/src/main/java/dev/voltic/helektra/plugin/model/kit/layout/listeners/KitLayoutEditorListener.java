package dev.voltic.helektra.plugin.model.kit.layout.listeners;

import dev.voltic.helektra.plugin.model.profile.state.ProfileStateListenerRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@SuppressWarnings("unused")
public class KitLayoutEditorListener implements Listener {

  private final ProfileStateListenerRegistry listenerRegistry;

  public KitLayoutEditorStateBoundListener createBoundListener(UUID playerId) {
    return new KitLayoutEditorStateBoundListener(playerId);
  }

  public class KitLayoutEditorStateBoundListener implements Listener {

    private final UUID playerId;

    public KitLayoutEditorStateBoundListener(UUID playerId) {
      this.playerId = playerId;
    }

    public KitLayoutEditorStateBoundListener bind(UUID playerId) {
      return new KitLayoutEditorStateBoundListener(playerId);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
      if (!(event.getEntity() instanceof Player player)) {
        return;
      }
      if (!player.getUniqueId().equals(playerId)) {
        return;
      }
      event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
      if (!(event.getEntity() instanceof Player player)) {
        return;
      }
      if (!player.getUniqueId().equals(playerId)) {
        return;
      }
      event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
      if (!event.getPlayer().getUniqueId().equals(playerId)) {
        return;
      }
      event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMove(PlayerMoveEvent event) {
      if (!event.getPlayer().getUniqueId().equals(playerId)) {
        return;
      }
      if (
        event.getFrom().getBlockX() != event.getTo().getBlockX() ||
        event.getFrom().getBlockY() != event.getTo().getBlockY() ||
        event.getFrom().getBlockZ() != event.getTo().getBlockZ()
      ) {
        event.setCancelled(true);
      }
    }
  }
}
