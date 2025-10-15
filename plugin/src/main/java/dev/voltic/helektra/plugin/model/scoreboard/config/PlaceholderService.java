package dev.voltic.helektra.plugin.model.scoreboard.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderService {

  private final Map<String, Function<Player, String>> customPlaceholders = new HashMap<>();
  private boolean placeholderAPIEnabled = false;

  public void registerCustomPlaceholder(String key, Function<Player, String> provider) {
    customPlaceholders.put(key, provider);
  }

  public String replacePlaceholders(String text, Player player) {
    String result = text;

    for (Map.Entry<String, Function<Player, String>> entry : customPlaceholders.entrySet()) {
      String placeholder = "{" + entry.getKey() + "}";
      if (result.contains(placeholder)) {
        String value = entry.getValue().apply(player);
        result = result.replace(placeholder, value != null ? value : "");
      }
    }

    if (placeholderAPIEnabled && isPlaceholderAPIEnabled()) {
      result = replacePlaceholderAPI(result, player);
    }

    return result;
  }

  public void setPlaceholderAPIEnabled(boolean enabled) {
    this.placeholderAPIEnabled = enabled;
  }

  private boolean isPlaceholderAPIEnabled() {
    try {
      Class.forName("me.clip.placeholderapi.PlaceholderAPI");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private String replacePlaceholderAPI(String text, Player player) {
    try {
      return PlaceholderAPI.setPlaceholders(player, text);
    } catch (Exception e) {
      return text;
    }
  }
}
