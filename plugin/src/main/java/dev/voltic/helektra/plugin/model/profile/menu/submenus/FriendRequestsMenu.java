package dev.voltic.helektra.plugin.model.profile.menu.submenus;

import dev.voltic.helektra.api.model.profile.IFriend;
import dev.voltic.helektra.api.model.profile.IFriendService;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.friend.Friend;
import dev.voltic.helektra.plugin.model.profile.friend.FriendInteractionService;
import dev.voltic.helektra.plugin.model.profile.friend.FriendRequestHelper;
import dev.voltic.helektra.plugin.model.profile.menu.FriendsMenu;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.DynamicMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class FriendRequestsMenu extends DynamicMenu {

  private static final String MENU_PATH = "friend-requests";
  private static final String KEY_FILLER = "filler";
  private static final String KEY_BACK = "back";

  private final Helektra plugin;
  private final IFriendService friendService;
  private final FriendInteractionService interactionService;
  private final IProfileService profileService;

  @Inject
  public FriendRequestsMenu(
    MenuConfigHelper configHelper,
    Helektra plugin,
    IFriendService friendService,
    FriendInteractionService interactionService,
    IProfileService profileService
  ) {
    super(configHelper, MENU_PATH);
    this.plugin = plugin;
    this.friendService = friendService;
    this.interactionService = interactionService;
    this.profileService = profileService;
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
    List<IFriend> incomingRequests = friendService.getIncomingRequests(player.getUniqueId()).join();

    for (IFriend request : incomingRequests) {
      ItemStack requestItem = buildRequestItem(player, request);
      addDynamicItemWithHandler(requestItem, e -> handleRequestClick(player, request, e));
    }

    setupBackButton(itemConfigs, player);
    fillFiller(itemConfigs.get(KEY_FILLER));
    completeRefresh();
    setupPaginationItems(player);

    if (incomingRequests.isEmpty()) {
      String message = getMenuConfigHelper().getString(MENU_PATH + ".messages.empty", "");
      if (!message.isEmpty()) {
        player.sendMessage(ColorUtils.translate(message));
      }
    }
  }

  private void setupBackButton(Map<String, MenuItemConfig> itemConfigs, Player player) {
    MenuItemConfig config = itemConfigs.get(KEY_BACK);
    if (config == null || !config.exists()) {
      return;
    }

    ItemStack item = buildStatic(config);
    setStaticItemWithHandler(config.getPrimarySlot(), item, e -> {
      e.setCancelled(true);
      plugin.getMenuFactory().openMenu(FriendsMenu.class, player);
    });
  }

  private ItemStack buildRequestItem(Player viewer, IFriend request) {
    java.util.UUID senderId = java.util.UUID.randomUUID();
    String senderName = "Unknown";
    
    if (request instanceof Friend friend) {
      senderId = FriendRequestHelper.extractSender(friend);
      IProfile senderProfile = profileService.getProfile(senderId).join().orElse(null);
      if (senderProfile != null) {
        senderName = senderProfile.getName();
      }
    }

    boolean online = Bukkit.getPlayer(senderId) != null;
    String onlineColor = online ? "&a" : "&c";
    String onlineText = online ? "Online" : "Offline";

    List<String> lore = new ArrayList<>();
    lore.add(ColorUtils.translate("&7Status: &ePending"));
    lore.add(ColorUtils.translate("&7Online: " + onlineColor + onlineText));
    lore.add("");
    lore.add(ColorUtils.translate("&aLeft-Click to accept"));
    lore.add(ColorUtils.translate("&cRight-Click to deny"));

    ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();
    if (skull != null && skull.getItemMeta() instanceof SkullMeta skullMeta) {
      skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(senderId));
      skull.setItemMeta(skullMeta);
    }

    return new ItemBuilder(skull != null ? skull : XMaterial.PLAYER_HEAD.parseItem())
      .name(ColorUtils.translate("&e" + senderName))
      .lore(lore)
      .build();
  }

  private void handleRequestClick(Player player, IFriend request, InventoryClickEvent event) {
    event.setCancelled(true);

    if (!(request instanceof Friend friend)) {
      return;
    }

    java.util.UUID senderId = FriendRequestHelper.extractSender(friend);

    if (event.isLeftClick()) {
      interactionService.acceptFriendRequest(player, senderId);
      render(player);
    } else if (event.isRightClick()) {
      interactionService.denyFriendRequest(player, senderId);
      render(player);
    }
  }

  private ItemStack buildStatic(MenuItemConfig itemConfig) {
    return new ItemBuilder(itemConfig.getMaterial())
      .name(itemConfig.getName())
      .lore(itemConfig.getLore())
      .build();
  }
}
