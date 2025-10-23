package dev.voltic.helektra.plugin.utils.sound;

import com.google.inject.Singleton;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.SoundConfigHelper;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;

@Singleton
public final class MenuSoundUtils {

  private static SoundConfigHelper soundConfigHelper;
  private static MenuConfigHelper menuConfigHelper;

  @Inject
  public MenuSoundUtils(
    SoundConfigHelper soundConfigHelper,
    MenuConfigHelper menuConfigHelper
  ) {
    MenuSoundUtils.soundConfigHelper = soundConfigHelper;
    MenuSoundUtils.menuConfigHelper = menuConfigHelper;
  }

  public static void playMenuSound(
    Player player,
    String menuPath,
    String type
  ) {
    if (player == null) {
      return;
    }
    ConfiguredSound sound = resolveMenuSound(menuPath, type);
    if (sound != null) {
      sound.play(player);
    }
  }

  public static void playItemSound(
    Player player,
    MenuItemConfig itemConfig,
    String menuPath
  ) {
    if (player == null) {
      return;
    }
    ConfiguredSound sound = itemConfig != null ? itemConfig.getSound() : null;
    if (sound == null) {
      sound = resolveMenuSound(menuPath, "default-click");
    }
    if (sound != null) {
      sound.play(player);
    }
  }

  public static void playErrorSound(Player player) {
    if (player == null || soundConfigHelper == null) {
      return;
    }
    ConfiguredSound sound = soundConfigHelper.getGlobalMenuSound("error");
    if (sound == null) {
      sound = soundConfigHelper.getDefaultMenuSound("error");
    }
    if (sound != null) {
      sound.play(player);
    }
  }

  public static void playOpenSound(Player player, String menuPath) {
    playMenuSound(player, menuPath, "open");
  }

  public static void playCloseSound(Player player, String menuPath) {
    playMenuSound(player, menuPath, "close");
  }

  private static ConfiguredSound resolveMenuSound(
    String menuPath,
    String type
  ) {
    ConfiguredSound sound = null;
    if (menuConfigHelper != null && menuPath != null) {
      sound = menuConfigHelper.getMenuSound(menuPath, type);
    }
    if (sound == null && soundConfigHelper != null) {
      String key = normalizeGlobalKey(type);
      sound = soundConfigHelper.getGlobalMenuSound(key);
      if (sound == null) {
        sound = soundConfigHelper.getDefaultMenuSound(key);
      }
    }
    return sound;
  }

  private static String normalizeGlobalKey(String type) {
    if ("default-click".equalsIgnoreCase(type)) {
      return "click";
    }
    return type;
  }
}
