package dev.voltic.helektra.plugin.model.cosmetic.listener;

import dev.voltic.helektra.api.model.cosmetic.CosmeticType;
import dev.voltic.helektra.plugin.Helektra;
import jakarta.inject.Inject;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ArrowTrailListener implements Listener {

  private final Helektra plugin;
  private final CosmeticEffectHelper effectHelper;

  @Inject
  public ArrowTrailListener(Helektra plugin, CosmeticEffectHelper effectHelper) {
    this.plugin = plugin;
    this.effectHelper = effectHelper;
  }

  @EventHandler
  public void onProjectileLaunch(ProjectileLaunchEvent event) {
    if (!(event.getEntity() instanceof Arrow arrow)) return;
    if (!(arrow.getShooter() instanceof Player player)) return;

    startTrailTask(player, arrow);
  }

  private void startTrailTask(Player player, Arrow arrow) {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!arrow.isValid() && !arrow.isInBlock() && !arrow.isOnGround()) return;
        new BukkitRunnable() {
          @Override
          public void run() {
            if (!player.isOnline() || arrow.isDead() || arrow.isOnGround() || !arrow.isValid()) {
              cancel();
              return;
            }
            effectHelper.applyTrailEffect(player, CosmeticType.ARROW_TRAIL, arrow.getLocation());
          }
        }.runTaskTimer(plugin, 0L, 1L);
      }
    }.runTaskLater(plugin, 1L);
  }
}
