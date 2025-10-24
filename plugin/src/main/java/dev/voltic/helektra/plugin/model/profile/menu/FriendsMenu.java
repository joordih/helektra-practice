package dev.voltic.helektra.plugin.model.profile.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import dev.voltic.helektra.api.model.profile.IFriend;
import dev.voltic.helektra.api.model.profile.IFriendService;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.friend.FriendAddPromptService;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuItemFactory.FriendView;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuSelectionService;
import dev.voltic.helektra.plugin.model.profile.menu.submenus.FriendOptionsMenu;
import dev.voltic.helektra.plugin.model.profile.menu.submenus.FriendRequestsMenu;
import dev.voltic.helektra.plugin.model.profile.menu.submenus.SentRequestsMenu;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.DynamicMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;

public class FriendsMenu extends DynamicMenu {

  private static final String MENU_PATH = "friends";
  private static final String KEY_FILLER = "filler";
  private static final String KEY_INCOMING_REQUESTS = "incoming-requests";
  private static final String KEY_SENT_REQUESTS = "sent-requests";
  private static final String KEY_ADD_FRIEND = "add-friend";

  private final Helektra plugin;
  private final IFriendService friendService;
  private final FriendAddPromptService addPromptService;
  private final IProfileService profileService;
  private final FriendMenuSelectionService selectionService;

  @Inject
  public FriendsMenu(
    MenuConfigHelper configHelper,
    Helektra plugin,
    IFriendService friendService,
    FriendAddPromptService addPromptService,
    IProfileService profileService,
    FriendMenuSelectionService selectionService
  ) {
    super(configHelper, MENU_PATH);
    this.plugin = plugin;
    this.friendService = friendService;
    this.addPromptService = addPromptService;
    this.profileService = profileService;
    this.selectionService = selectionService;
  }

  @Override
  public void setup(Player player) {
    Optional<IProfile> optionalProfile = plugin.getProfileService().getProfile(player.getUniqueId()).join();
    if (optionalProfile.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return;
    }
    render(player);
  }

  private void render(Player player) {
    clear();
    initializePagination();

    Map<String, MenuItemConfig> itemConfigs = getAllItemConfigs();
    List<IFriend> acceptedFriends = friendService.getAcceptedFriends(player.getUniqueId()).join();

    for (IFriend friend : acceptedFriends) {
      ItemStack friendItem = buildFriendItem(player, friend);
      addDynamicItemWithHandler(friendItem, e -> handleFriendClick(player, friend, e));
    }

    setupIncomingRequestsButton(player, itemConfigs);
    setupSentRequestsButton(player, itemConfigs);
    setupAddFriendButton(player, itemConfigs);
    
    fillFiller(itemConfigs.get(KEY_FILLER));
    completeRefresh();
    setupPaginationItems(player);
  }

  private void setupIncomingRequestsButton(Player player, Map<String, MenuItemConfig> itemConfigs) {
    MenuItemConfig config = itemConfigs.get(KEY_INCOMING_REQUESTS);
    if (config == null || !config.exists()) {
      return;
    }

    List<IFriend> incomingRequests = friendService.getIncomingRequests(player.getUniqueId()).join();
    int requestCount = incomingRequests.size();

    Map<String, String> placeholders = Map.of(
      "count", String.valueOf(requestCount),
      "requestCount", String.valueOf(requestCount)
    );

    String name = applyPlaceholders(config.getName(), placeholders);
    List<String> lore = applyPlaceholders(config.getLore(), placeholders);

    ItemStack item = new ItemBuilder(config.getMaterial())
      .name(name)
      .lore(lore)
      .build();

    setStaticItemWithHandler(config.getPrimarySlot(), item, e -> {
      e.setCancelled(true);
      plugin.getMenuFactory().openMenu(FriendRequestsMenu.class, player);
    });
  }

  private void setupSentRequestsButton(Player player, Map<String, MenuItemConfig> itemConfigs) {
    MenuItemConfig config = itemConfigs.get(KEY_SENT_REQUESTS);
    if (config == null || !config.exists()) {
      return;
    }

    List<IFriend> sentRequests = friendService.getOutgoingRequests(player.getUniqueId()).join();
    int requestCount = sentRequests.size();

    Map<String, String> placeholders = Map.of(
      "count", String.valueOf(requestCount),
      "requestCount", String.valueOf(requestCount)
    );

    String name = applyPlaceholders(config.getName(), placeholders);
    List<String> lore = applyPlaceholders(config.getLore(), placeholders);

    ItemStack item = new ItemBuilder(config.getMaterial())
      .name(name)
      .lore(lore)
      .build();

    setStaticItemWithHandler(config.getPrimarySlot(), item, e -> {
      e.setCancelled(true);
      plugin.getMenuFactory().openMenu(SentRequestsMenu.class, player);
    });
  }

  private void setupAddFriendButton(Player player, Map<String, MenuItemConfig> itemConfigs) {
    MenuItemConfig config = itemConfigs.get(KEY_ADD_FRIEND);
    if (config == null || !config.exists()) {
      return;
    }

    ItemStack item = buildStatic(config);
    setStaticItemWithHandler(config.getPrimarySlot(), item, e -> {
      e.setCancelled(true);
      handleAddFriendClick(player);
    });
  }

  private void handleAddFriendClick(Player player) {
    player.closeInventory();
    if (!addPromptService.beginPrompt(player.getUniqueId())) {
      player.sendMessage(TranslationUtils.translate("friends.prompt.add.already"));
      return;
    }
    player.sendMessage(TranslationUtils.translate("friends.prompt.add.start"));
  }

  private ItemStack buildFriendItem(Player viewer, IFriend friend) {
    boolean online = Bukkit.getPlayer(friend.getUniqueId()) != null;
    
    String friendName = friend.getName();
    if (friendName == null || friendName.isEmpty()) {
      IProfile friendProfile = profileService.getProfile(friend.getUniqueId()).join().orElse(null);
      if (friendProfile != null) {
        friendName = friendProfile.getName();
      } else {
        friendName = "Unknown";
      }
    }
    
    String statusLabel = TranslationUtils.translate("friends.status." + friend.getStatus().name().toLowerCase());
    String onlineColor = online ? "&a" : "&c";
    String onlineText = online ? "Online" : "Offline";

    List<String> lore = new ArrayList<>();
    lore.add(ColorUtils.translate("&7Status: " + statusLabel));
    lore.add(ColorUtils.translate("&7Online: " + onlineColor + onlineText));
    lore.add("");
    lore.add(ColorUtils.translate("&eClick to manage"));

    ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();
    if (skull != null && skull.getItemMeta() instanceof SkullMeta skullMeta) {
      skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(friend.getUniqueId()));
      skull.setItemMeta(skullMeta);
    }

    return new ItemBuilder(skull != null ? skull : XMaterial.PLAYER_HEAD.parseItem())
      .name(ColorUtils.translate("&a" + friendName))
      .lore(lore)
      .build();
  }

  private void handleFriendClick(Player player, IFriend friend, InventoryClickEvent event) {
    event.setCancelled(true);
    boolean online = Bukkit.getPlayer(friend.getUniqueId()) != null;
    String friendName = friend.getName();
    if (friendName == null || friendName.isEmpty()) {
      IProfile friendProfile = profileService.getProfile(friend.getUniqueId()).join().orElse(null);
      if (friendProfile != null) {
        friendName = friendProfile.getName();
      } else {
        friendName = "Unknown";
      }
    }

    selectionService.select(player.getUniqueId(), new FriendView(friend.getUniqueId(), friendName, friend.getStatus(), online));
    plugin.getMenuFactory().openMenu(FriendOptionsMenu.class, player);
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
    return lines.stream()
      .map(line -> applyPlaceholders(line, placeholders))
      .toList();
  }

  
}
