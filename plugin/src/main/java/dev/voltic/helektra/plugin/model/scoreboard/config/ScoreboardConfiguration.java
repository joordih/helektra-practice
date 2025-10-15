package dev.voltic.helektra.plugin.model.scoreboard.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.utils.config.FileConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScoreboardConfiguration {

  private boolean enabled = true;
  private int updateInterval = 20;

  private Map<ProfileState, StateConfiguration> states = new HashMap<>();
  private Map<String, String> placeholders = new HashMap<>();

  public static ScoreboardConfiguration fromConfig(FileConfig config) {
    ScoreboardConfiguration scoreboardConfig = new ScoreboardConfiguration();
    FileConfiguration fileConfig = config.getConfig();

    scoreboardConfig.setEnabled(fileConfig.getBoolean("scoreboard.enabled", true));
    scoreboardConfig.setUpdateInterval(fileConfig.getInt("scoreboard.update-interval", 20));

    if (fileConfig.contains("scoreboard.states")) {
      for (ProfileState state : ProfileState.values()) {
        String path = "scoreboard.states." + state.name();
        if (fileConfig.contains(path)) {
          StateConfiguration stateConfig = new StateConfiguration();
          stateConfig.setEnabled(fileConfig.getBoolean(path + ".enabled", true));
          stateConfig.setTitle(fileConfig.getString(path + ".title", "&e&l" + state.name()));
          stateConfig.setLines(fileConfig.getStringList(path + ".lines"));
          scoreboardConfig.getStates().put(state, stateConfig);
        }
      }
    }

    if (fileConfig.contains("scoreboard.placeholders")) {
      Map<String, String> placeholders = new HashMap<>();
      for (String key : fileConfig.getConfigurationSection("scoreboard.placeholders").getKeys(false)) {
        String value = fileConfig.getString("scoreboard.placeholders." + key);
        placeholders.put(key, value);
      }
      scoreboardConfig.setPlaceholders(placeholders);
    }

    return scoreboardConfig;
  }

  @Data
  @NoArgsConstructor
  public static class StateConfiguration {
    private boolean enabled = true;
    private String title = "";
    private List<String> lines = List.of();
  }
}
