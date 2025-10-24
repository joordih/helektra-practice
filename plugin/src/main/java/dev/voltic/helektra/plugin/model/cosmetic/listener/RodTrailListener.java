package dev.voltic.helektra.plugin.model.cosmetic.listener;

import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.scheduler.BukkitRunnable;

import dev.voltic.helektra.api.model.cosmetic.CosmeticType;
import dev.voltic.helektra.plugin.Helektra;
import jakarta.inject.Inject;

public class RodTrailListener implements Listener {

  private final Helektra plugin;
  private final CosmeticEffectHelper effectHelper;

  @Inject
  public RodTrailListener(Helektra plugin, CosmeticEffectHelper effectHelper) {
    this.plugin = plugin;
    this.effectHelper = effectHelper;
  }

  @EventHandler
  public void onPlayerFish(PlayerFishEvent event) {
    Player player = event.getPlayer();
    FishHook hook = event.getHook();

    if (event.getState() == PlayerFishEvent.State.FISHING) {
      startTrailTask(player, hook);
    }
  }

  private void startTrailTask(Player player, FishHook hook) {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!player.isOnline() || !hook.isValid() || hook.isDead()) {
          cancel();
          return;
        }

        effectHelper.applyTrailEffect(player, CosmeticType.ROD_TRAIL, hook.getLocation());
      }
    }.runTaskTimer(plugin, 0L, 2L);
  }
}
