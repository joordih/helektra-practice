package dev.voltic.helektra.plugin.utils.sound;

import com.google.inject.Singleton;
import dev.voltic.helektra.plugin.utils.SoundConfigHelper;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;

@Singleton
public final class PlayerSoundUtils {

  private static SoundConfigHelper soundConfigHelper;

  @Inject
  public PlayerSoundUtils(SoundConfigHelper soundConfigHelper) {
    PlayerSoundUtils.soundConfigHelper = soundConfigHelper;
  }

  public static void playKitEditorOpenSound(Player player) {
    play(player, "kit-editor-open");
  }

  public static void playPartyCreateSound(Player player) {
    play(player, "party-create");
  }

  public static void playPartyDisbandSound(Player player) {
    play(player, "party-disband");
  }

  public static void playQueueJoinSound(Player player) {
    play(player, "queue-join");
  }

  public static void playQueueLeaveSound(Player player) {
    play(player, "queue-leave");
  }

  private static void play(Player player, String key) {
    if (player == null || soundConfigHelper == null) {
      return;
    }
    ConfiguredSound sound = soundConfigHelper.getPlayerSound(key);
    if (sound != null) {
      sound.play(player);
    }
  }
}
