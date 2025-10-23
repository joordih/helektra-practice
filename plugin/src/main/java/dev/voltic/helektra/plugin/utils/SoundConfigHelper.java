package dev.voltic.helektra.plugin.utils;

import com.google.inject.Singleton;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.config.FileConfig;
import dev.voltic.helektra.plugin.utils.sound.ConfiguredSound;
import jakarta.inject.Inject;
import org.bukkit.configuration.ConfigurationSection;

@Singleton
public class SoundConfigHelper {

  private final FileConfig soundsConfig;

  @Inject
  public SoundConfigHelper() {
    this.soundsConfig = Helektra.getInstance().getSoundsConfig();
  }

  public ConfiguredSound getSound(ConfigurationSection section) {
    return ConfiguredSound.fromSection(section).orElse(null);
  }

  public ConfiguredSound getSound(String path) {
    ConfigurationSection section = soundsConfig
      .getConfig()
      .getConfigurationSection(path);
    if (section != null) {
      return getSound(section);
    }
    Object value = soundsConfig.getConfig().get(path);
    if (value == null) {
      return null;
    }
    return ConfiguredSound.from(value).orElse(null);
  }

  public ConfiguredSound getGlobalMenuSound(String key) {
    return getSound("sounds.menu." + key);
  }

  public ConfiguredSound getDefaultMenuSound(String key) {
    return getSound("sounds.defaults.menu-" + key);
  }

  public ConfiguredSound getPlayerSound(String key) {
    return getSound("sounds.player." + key);
  }
}
