package dev.voltic.helektra.plugin.model.profile.menu.friend;

import dev.voltic.helektra.api.model.profile.IFriend;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

@Singleton
public class FriendMenuItemFactory {

  private final FriendMenuConfig menuConfig;

  @Inject
  public FriendMenuItemFactory(FriendMenuConfig menuConfig) {
    this.menuConfig = menuConfig;
  }

  public FriendView toView(IFriend friend) {
    boolean online = Bukkit.getPlayer(friend.getUniqueId()) != null;
    return new FriendView(friend.getUniqueId(), friend.getName(), friend.getStatus(), online);
  }

  public ItemStack buildFriendItem(
    FriendView view,
    Map<String, String> basePlaceholders,
    int index,
    int total
  ) {
    MenuItemConfig config = menuConfig.getFriendItem();
    if (!config.exists()) {
      return null;
    }

    Map<String, String> placeholders = new HashMap<>(basePlaceholders);
    placeholders.put("friend", view.name());
    placeholders.put("friend_name", view.name());
    placeholders.put("status", menuConfig.getStatusLabel(view.status()));
    placeholders.put("online", menuConfig.getOnlineLabel(view.online()));
    placeholders.put("friend_uuid", view.uniqueId().toString());
    placeholders.put("position", String.valueOf(index));
    placeholders.put("index", String.valueOf(index));
    placeholders.put("total", String.valueOf(total));

    return buildItem(config, placeholders);
  }

  public ItemStack buildEmptyItem(Map<String, String> basePlaceholders) {
    MenuItemConfig config = menuConfig.getEmptyItem();
    if (!config.exists()) {
      return null;
    }
    return buildItem(config, new HashMap<>(basePlaceholders));
  }

  public ItemStack buildPaginationItem(
    MenuItemConfig config,
    Map<String, String> placeholders
  ) {
    if (!config.exists()) {
      return null;
    }
    return buildItem(config, new HashMap<>(placeholders));
  }

  public ItemStack buildItem(
    MenuItemConfig config,
    Map<String, String> placeholders
  ) {
    if (!config.exists()) {
      return null;
    }

    ItemBuilder builder = new ItemBuilder(config.getMaterial());
    String name = config.getName();
    if (!name.isEmpty()) {
      builder.name(menuConfig.applyPlaceholders(name, placeholders));
    }

    List<String> lore = config.getLore(placeholders);
    if (lore.isEmpty()) {
      builder.lore(new ArrayList<>());
    } else {
      builder.lore(lore);
    }

    return builder.build();
  }

  public record FriendView(
    UUID uniqueId,
    String name,
    IFriend.Status status,
    boolean online
  ) {}
}
