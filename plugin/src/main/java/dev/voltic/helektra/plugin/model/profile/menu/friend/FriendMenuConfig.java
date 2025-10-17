package dev.voltic.helektra.plugin.model.profile.menu.friend;

import dev.voltic.helektra.api.model.profile.IFriend;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Singleton
public class FriendMenuConfig {

  private static final String MENU_PATH = "friends";
  private static final Set<String> RESERVED_KEYS = Set.of(
    "friend-entry",
    "empty-slot",
    "pagination-previous",
    "pagination-next",
    "add-friend"
  );

  private final MenuConfigHelper menuConfig;

  @Inject
  public FriendMenuConfig(MenuConfigHelper menuConfig) {
    this.menuConfig = menuConfig;
  }

  public String getMenuPath() {
    return MENU_PATH;
  }

  public List<Integer> getContentSlots() {
    int start = menuConfig.getInt(MENU_PATH + ".content.start-slot", 10);
    int end = menuConfig.getInt(MENU_PATH + ".content.end-slot", 16);
    if (end < start) {
      int temp = start;
      start = end;
      end = temp;
    }
    return IntStream.rangeClosed(start, end)
      .boxed()
      .collect(Collectors.toList());
  }

  public MenuItemConfig getFriendItem() {
    return menuConfig.getItemConfig(MENU_PATH, "friend-entry");
  }

  public MenuItemConfig getEmptyItem() {
    return menuConfig.getItemConfig(MENU_PATH, "empty-slot");
  }

  public MenuItemConfig getAddFriendItem() {
    return menuConfig.getItemConfig(MENU_PATH, "add-friend");
  }

  public MenuItemConfig getPreviousItem() {
    return menuConfig.getItemConfig(MENU_PATH, "pagination-previous");
  }

  public MenuItemConfig getNextItem() {
    return menuConfig.getItemConfig(MENU_PATH, "pagination-next");
  }

  public List<MenuItemConfig> getStaticItems() {
    Set<String> keys = new HashSet<>(menuConfig.getItemKeys(MENU_PATH));
    keys.removeAll(RESERVED_KEYS);
    return keys
      .stream()
      .map(key -> menuConfig.getItemConfig(MENU_PATH, key))
      .filter(MenuItemConfig::exists)
      .collect(Collectors.toList());
  }

  public String getStatusLabel(IFriend.Status status) {
    return getFriendItem().getString(
      "placeholders.status." + status.name().toLowerCase(),
      status.name()
    );
  }

  public String getOnlineLabel(boolean online) {
    return getFriendItem().getString(
      "placeholders." + (online ? "online" : "offline"),
      online ? "&aOnline" : "&cOffline"
    );
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

  public String getProfileMissingMessage() {
    return menuConfig.getString(
      MENU_PATH + ".messages.profile-missing",
      "&cUnable to load your profile."
    );
  }

  public String getEmptyMessage() {
    return menuConfig.getString(MENU_PATH + ".messages.empty", "");
  }

  public String getItemCommand(String key) {
    return menuConfig.getString(MENU_PATH + ".items." + key + ".command", "");
  }
}
