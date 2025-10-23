package dev.voltic.helektra.plugin.utils;

import dev.voltic.helektra.plugin.nms.strategy.NmsStrategies;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public final class PlayerUtils {

  public PlayerUtils() {
    throw new UnsupportedOperationException(
      "This class cannot be instantiated"
    );
  }

  public static void clearPlayer(Player player) {
    player.setHealth(20.0);
    player.setFoodLevel(20);
    player.setSaturation(12.8F);
    player.setMaximumNoDamageTicks(20);
    player.setFireTicks(0);
    player.setFallDistance(0.0F);
    player.setLevel(0);
    player.setExp(0.0F);
    player.setWalkSpeed(0.2F);
    player.setFlySpeed(0.2F);
    player.getInventory().setHeldItemSlot(0);
    player.setAllowFlight(false);
    player.getInventory().clear();
    player.getInventory().setArmorContents(null);
    player.closeInventory();
    player.setGameMode(GameMode.SURVIVAL);
    player
      .getActivePotionEffects()
      .stream()
      .map(PotionEffect::getType)
      .forEach(player::removePotionEffect);
    NmsStrategies.DATA_WATCHER.execute(player);
    player.updateInventory();
  }
}
