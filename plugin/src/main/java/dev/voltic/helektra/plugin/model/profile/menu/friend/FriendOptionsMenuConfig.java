package dev.voltic.helektra.plugin.model.profile.menu.friend;

import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class FriendOptionsMenuConfig {

  private static final String MENU_PATH = "friend-options";
  private static final Set<String> ACTION_KEYS = Set.of(
    "remove",
    "invite",
    "block",
    "back"
  );

  private final MenuConfigHelper menuConfig;

  @Inject
  public FriendOptionsMenuConfig(MenuConfigHelper menuConfig) {
    this.menuConfig = menuConfig;
  }

  public String getMenuPath() {
    return MENU_PATH;
  }

  public MenuItemConfig getItem(String key) {
    return menuConfig.getItemConfig(MENU_PATH, key);
  }

  public List<MenuItemConfig> getStaticItems() {
    Set<String> keys = new HashSet<>(menuConfig.getItemKeys(MENU_PATH));
    keys.removeAll(ACTION_KEYS);
    return keys
      .stream()
      .map(key -> menuConfig.getItemConfig(MENU_PATH, key))
      .filter(MenuItemConfig::exists)
      .collect(Collectors.toList());
  }

  public String getMessage(String key, String defaultValue) {
    return menuConfig.getString(MENU_PATH + ".messages." + key, defaultValue);
  }

  public String applyPlaceholders(
    String value,
    Map<String, String> placeholders
  ) {
    return menuConfig.replacePlaceholders(value, placeholders);
  }

  public List<String> applyPlaceholders(
    List<String> values,
    Map<String, String> placeholders
  ) {
    return menuConfig.replacePlaceholders(values, placeholders);
  }

  public String getCommand(String itemKey) {
    return menuConfig.getString(
      MENU_PATH + ".items." + itemKey + ".command",
      ""
    );
  }

  public Set<String> getActionKeys() {
    return ACTION_KEYS;
  }
}
