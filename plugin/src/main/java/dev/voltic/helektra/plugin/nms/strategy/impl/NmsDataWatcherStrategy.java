package dev.voltic.helektra.plugin.nms.strategy.impl;

import dev.voltic.helektra.plugin.nms.NmsStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class NmsDataWatcherStrategy implements NmsStrategy {

  @Override
  public void execute(Player player, Object... args) {
    if (tryModernApi(player)) return;
    if (tryLegacyNms(player)) return;
  }

  private boolean tryModernApi(Player player) {
    try {
      player.setAbsorptionAmount(0.0D);
      return true;
    } catch (Throwable ignored) {
      return false;
    }
  }

  private boolean tryLegacyNms(Player player) {
    try {
      String version = Bukkit.getServer()
        .getClass()
        .getPackage()
        .getName()
        .split("\\.")[3];
      Object craftPlayer = player;
      Class<?> craftClass = craftPlayer.getClass();
      Method getHandleMethod = craftClass.getMethod("getHandle");
      Object entityPlayer = getHandleMethod.invoke(craftPlayer);

      Method getDataWatcherMethod = entityPlayer
        .getClass()
        .getMethod("getDataWatcher");
      Object dataWatcher = getDataWatcherMethod.invoke(entityPlayer);

      boolean isLegacy =
        version.startsWith("v1_8") ||
        version.startsWith("v1_9") ||
        version.startsWith("v1_10") ||
        version.startsWith("v1_11") ||
        version.startsWith("v1_12");

      if (isLegacy) {
        Method watchMethod = dataWatcher
          .getClass()
          .getMethod("watch", int.class, Object.class);
        watchMethod.invoke(dataWatcher, 9, (byte) 0);
        return true;
      }

      Field[] fields = entityPlayer.getClass().getDeclaredFields();
      Field absorptionField = null;

      for (Field field : fields) {
        if (
          field.getType().getSimpleName().contains("DataWatcherObject") &&
          field.getName().toLowerCase().contains("absorption")
        ) {
          absorptionField = field;
          break;
        }
      }

      if (absorptionField != null) {
        absorptionField.setAccessible(true);
        Object dataWatcherObject = absorptionField.get(null);
        Method setMethod = dataWatcher
          .getClass()
          .getMethod("set", dataWatcherObject.getClass(), Object.class);
        setMethod.invoke(dataWatcher, dataWatcherObject, 0.0F);
        return true;
      }

      player.setAbsorptionAmount(0.0D);
      return true;
    } catch (Throwable ignored) {
      return false;
    }
  }
}
