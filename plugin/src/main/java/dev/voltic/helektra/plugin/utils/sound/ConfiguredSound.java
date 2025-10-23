package dev.voltic.helektra.plugin.utils.sound;

import dev.voltic.helektra.plugin.utils.xseries.XSound;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class ConfiguredSound {

  private final XSound sound;
  private final float volume;
  private final float pitch;

  private ConfiguredSound(XSound sound, float volume, float pitch) {
    this.sound = sound;
    this.volume = volume;
    this.pitch = pitch;
  }

  public void play(Player player) {
    sound.play(player, volume, pitch);
  }

  public static Optional<ConfiguredSound> from(Object source) {
    if (source instanceof ConfigurationSection section) {
      return fromSection(section);
    }
    if (source instanceof String value) {
      return fromString(value);
    }
    return Optional.empty();
  }

  public static Optional<ConfiguredSound> fromSection(ConfigurationSection section) {
    if (section == null) {
      return Optional.empty();
    }
    if (!section.getBoolean("enabled", true)) {
      return Optional.empty();
    }
    String name = section.getString("sound", "");
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    Optional<XSound> parsed = XSound.of(name.trim());
    if (parsed.isEmpty()) {
      return Optional.empty();
    }
    float volume = (float) section.getDouble("volume", 1.0);
    float pitch = (float) section.getDouble("pitch", 1.0);
    if (volume < 0) {
      volume = 0;
    }
    if (pitch < 0) {
      pitch = 0;
    }
    return Optional.of(new ConfiguredSound(parsed.get(), volume, pitch));
  }

  public static Optional<ConfiguredSound> fromString(String value) {
    if (value == null) {
      return Optional.empty();
    }
    String name = value.trim();
    if (name.isEmpty()) {
      return Optional.empty();
    }
    Optional<XSound> parsed = XSound.of(name);
    if (parsed.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new ConfiguredSound(parsed.get(), 1.0f, 1.0f));
  }
}
