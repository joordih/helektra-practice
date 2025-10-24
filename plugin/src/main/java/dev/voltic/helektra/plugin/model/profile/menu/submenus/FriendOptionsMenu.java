package dev.voltic.helektra.plugin.model.profile.menu.submenus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import dev.voltic.helektra.api.model.profile.IFriendService;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.menu.FriendsMenu;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuItemFactory.FriendView;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuSelectionService;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.DynamicMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;

public class FriendOptionsMenu extends DynamicMenu {

  private static final String MENU_PATH = "friend-options";
  private static final String KEY_FILLER = "filler";
  private static final String KEY_INFO = "info";
  private static final String KEY_REMOVE = "remove";
  private static final String KEY_INVITE = "invite";
  private static final String KEY_VIEW_STATS = "view-stats";
  private static final String KEY_BACK = "back";

  private final Helektra plugin;
  private final IFriendService friendService;
  private final FriendMenuSelectionService selectionService;
  private final IProfileService profileService;

  @Inject
  public FriendOptionsMenu(
    MenuConfigHelper configHelper,
    Helektra plugin,
    IFriendService friendService,
    IProfileService profileService,
    FriendMenuSelectionService selectionService
  ) {
    super(configHelper, MENU_PATH);
    this.plugin = plugin;
    this.friendService = friendService;
    this.profileService = profileService;
    this.selectionService = selectionService;
  }

  @Override
  public void setup(Player player) {
    java.util.Optional<FriendView> selection = selectionService.getSelection(player.getUniqueId());
    if (selection.isEmpty()) {
      String message = getMenuConfigHelper().getString(MENU_PATH + ".messages.no-selection", "");
      if (!message.isEmpty()) {
        player.sendMessage(ColorUtils.translate(message));
      }
      plugin.getMenuFactory().openMenu(FriendsMenu.class, player);
      return;
    }
    render(player, selection.get());
  }

  private void render(Player player, FriendView friend) {
    clear();
    Map<String, MenuItemConfig> itemConfigs = getAllItemConfigs();

    fillFiller(itemConfigs.get(KEY_FILLER));
    setupInfoItem(itemConfigs, friend);
    setupRemoveButton(itemConfigs, player, friend);
    setupInviteButton(itemConfigs, player, friend);
    setupViewStatsButton(itemConfigs, friend);
    setupBackButton(itemConfigs, player);
    
    open(player);
  }

  private void setupInfoItem(Map<String, MenuItemConfig> itemConfigs, FriendView friend) {
    MenuItemConfig config = itemConfigs.get(KEY_INFO);
    if (config == null || !config.exists()) {
      return;
    }

    String friendName = getFriendName(friend.uniqueId(), friend.name());
    boolean online = Bukkit.getPlayer(friend.uniqueId()) != null;
    String statusLabel = TranslationUtils.translate("friends.status." + friend.status().name().toLowerCase());
    String onlineColor = online ? "&a" : "&c";
    String onlineText = online ? "Online" : "Offline";

    Map<String, String> placeholders = Map.of(
      "friend", friendName,
      "status", statusLabel,
      "online", onlineColor + onlineText
    );

    String name = applyPlaceholders(config.getName(), placeholders);
    List<String> lore = applyPlaceholders(config.getLore(), placeholders);

    ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();
    if (skull != null && skull.getItemMeta() instanceof SkullMeta skullMeta) {
      skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(friend.uniqueId()));
      skull.setItemMeta(skullMeta);
    }

    ItemStack item = new ItemBuilder(skull != null ? skull : XMaterial.PLAYER_HEAD.parseItem())
      .name(name)
      .lore(lore)
      .build();

    setStaticItemWithHandler(config.getPrimarySlot(), item, e -> e.setCancelled(true));
  }

  private void setupRemoveButton(Map<String, MenuItemConfig> itemConfigs, Player player, FriendView friend) {
    MenuItemConfig config = itemConfigs.get(KEY_REMOVE);
    if (config == null || !config.exists()) {
      return;
    }

    String friendName = getFriendName(friend.uniqueId(), friend.name());
    Map<String, String> placeholders = Map.of("friend", friendName);
    String name = applyPlaceholders(config.getName(), placeholders);
    List<String> lore = applyPlaceholders(config.getLore(), placeholders);

    ItemStack item = new ItemBuilder(config.getMaterial())
      .name(name)
      .lore(lore)
      .build();

    setStaticItemWithHandler(config.getPrimarySlot(), item, e -> {
      e.setCancelled(true);
      handleRemove(player, friend, config);
    });
  }

  private void setupInviteButton(Map<String, MenuItemConfig> itemConfigs, Player player, FriendView friend) {
    MenuItemConfig config = itemConfigs.get(KEY_INVITE);
    if (config == null || !config.exists()) {
      return;
    }

    String friendName = getFriendName(friend.uniqueId(), friend.name());
    Map<String, String> placeholders = Map.of("friend", friendName);
    String name = applyPlaceholders(config.getName(), placeholders);
    List<String> lore = applyPlaceholders(config.getLore(), placeholders);

    ItemStack item = new ItemBuilder(config.getMaterial())
      .name(name)
      .lore(lore)
      .build();

    setStaticItemWithHandler(config.getPrimarySlot(), item, e -> {
      e.setCancelled(true);
      handleInvite(player, friend, config);
    });
  }

  private void setupViewStatsButton(Map<String, MenuItemConfig> itemConfigs, FriendView friend) {
    MenuItemConfig config = itemConfigs.get(KEY_VIEW_STATS);
    if (config == null || !config.exists()) {
      return;
    }

    String friendName = getFriendName(friend.uniqueId(), friend.name());
    Map<String, String> placeholders = Map.of("friend", friendName);
    String name = applyPlaceholders(config.getName(), placeholders);
    List<String> lore = applyPlaceholders(config.getLore(), placeholders);

    ItemStack item = new ItemBuilder(config.getMaterial())
      .name(name)
      .lore(lore)
      .build();

    setStaticItemWithHandler(config.getPrimarySlot(), item, e -> e.setCancelled(true));
  }

  private void setupBackButton(Map<String, MenuItemConfig> itemConfigs, Player player) {
    MenuItemConfig config = itemConfigs.get(KEY_BACK);
    if (config == null || !config.exists()) {
      return;
    }

    ItemStack item = buildStatic(config);
    setStaticItemWithHandler(config.getPrimarySlot(), item, e -> {
      e.setCancelled(true);
      selectionService.clear(player.getUniqueId());
      plugin.getMenuFactory().openMenu(FriendsMenu.class, player);
    });
  }

  private void handleRemove(Player player, FriendView friend, MenuItemConfig config) {
    player.closeInventory();
    friendService.removeFriend(player.getUniqueId(), friend.uniqueId()).join();
    selectionService.clear(player.getUniqueId());

    String friendName = getFriendName(friend.uniqueId(), friend.name());
    String message = config.getRawString("messages.success", "");
    if (!message.isEmpty()) {
      Map<String, String> placeholders = Map.of("friend", friendName);
      player.sendMessage(applyPlaceholders(message, placeholders));
    }

    plugin.getMenuFactory().openMenu(FriendsMenu.class, player);
  }

  private void handleInvite(Player player, FriendView friend, MenuItemConfig config) {
    player.closeInventory();

    String friendName = getFriendName(friend.uniqueId(), friend.name());
    String command = config.getRawString("command", "");
    if (!command.isEmpty()) {
      command = command.replace("{friend}", friendName);
      player.performCommand(command);
    }

    String message = config.getRawString("messages.success", "");
    if (!message.isEmpty()) {
      Map<String, String> placeholders = Map.of("friend", friendName);
      player.sendMessage(applyPlaceholders(message, placeholders));
    }
  }

  private String getFriendName(java.util.UUID friendId, String fallback) {
    String friendName = fallback;
    if (friendName == null || friendName.isEmpty()) {
      IProfile friendProfile = profileService.getProfile(friendId).join().orElse(null);
      if (friendProfile != null) {
        friendName = friendProfile.getName();
      } else {
        friendName = "Unknown";
      }
    }
    return friendName;
  }

  private ItemStack buildStatic(MenuItemConfig itemConfig) {
    return new ItemBuilder(itemConfig.getMaterial())
      .name(itemConfig.getName())
      .lore(itemConfig.getLore())
      .build();
  }

  private String applyPlaceholders(String text, Map<String, String> placeholders) {
    String result = text;
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      result = result.replace("{" + entry.getKey() + "}", entry.getValue());
    }
    return ColorUtils.translate(result);
  }

  private List<String> applyPlaceholders(List<String> lines, Map<String, String> placeholders) {
    List<String> result = new ArrayList<>();
    for (String line : lines) {
      result.add(applyPlaceholders(line, placeholders));
    }
    return result;
  }
}
