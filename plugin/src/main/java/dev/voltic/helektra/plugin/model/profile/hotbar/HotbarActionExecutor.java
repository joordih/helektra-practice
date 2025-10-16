package dev.voltic.helektra.plugin.model.profile.hotbar;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import org.bukkit.entity.Player;

import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HotbarActionExecutor {
  private final Map<String, HotbarAction> actions;

  @Inject
  public HotbarActionExecutor(Set<HotbarAction> actionSet) {
    Map<String, HotbarAction> map = Maps.newHashMap();

    for (HotbarAction action : actionSet) {
      map.put(action.id().toUpperCase(Locale.ROOT), action);
    }

    this.actions = Collections.unmodifiableMap(map);
  }

  public void execute(Player player, String action) {
    if (action == null)
      return;

    String normalized = action.trim();
    if (normalized.isEmpty())
      return;

    HotbarAction handler = actions.get(normalized.toUpperCase(Locale.ROOT));
    if (handler != null) {
      handler.execute(player);
      return;
    }

    String command = normalized.startsWith("/") ? normalized.substring(1) : normalized;
    if (!command.isEmpty()) {
      player.performCommand(command);
    }
  }
}
