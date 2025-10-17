package dev.voltic.helektra.plugin.model.profile.menu.submenus;

import dev.voltic.helektra.api.model.profile.IFriend;
import dev.voltic.helektra.api.model.profile.IFriendService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.menu.FriendsMenu;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuConfig;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuItemFactory;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuItemFactory.FriendView;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuSelectionService;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendOptionsMenuConfig;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class FriendOptionsMenu extends InjectableMenu {

  private final FriendOptionsMenuConfig optionsConfig;
  private final FriendMenuConfig friendMenuConfig;
  private final FriendMenuItemFactory itemFactory;
  private final FriendMenuSelectionService selectionService;
  private final IFriendService friendService;
  private final Helektra helektra;

  @Inject
  public FriendOptionsMenu(
    MenuConfigHelper menuConfig,
    FriendOptionsMenuConfig optionsConfig,
    FriendMenuConfig friendMenuConfig,
    FriendMenuItemFactory itemFactory,
    FriendMenuSelectionService selectionService,
    IFriendService friendService,
    Helektra helektra
  ) {
    super(menuConfig, optionsConfig.getMenuPath());
    this.optionsConfig = optionsConfig;
    this.friendMenuConfig = friendMenuConfig;
    this.itemFactory = itemFactory;
    this.selectionService = selectionService;
    this.friendService = friendService;
    this.helektra = helektra;
  }

  @Override
  public void setup(Player player) {
    Optional<FriendView> selectionOpt = selectionService.getSelection(
      player.getUniqueId()
    );
    if (selectionOpt.isEmpty()) {
      String message = optionsConfig.getMessage("no-selection", "");
      if (!message.isEmpty()) {
        player.sendMessage(
          optionsConfig.applyPlaceholders(
            message,
            Map.of("player", player.getName())
          )
        );
      }
      helektra.getMenuFactory().openMenu(FriendsMenu.class, player);
      return;
    }

    FriendView selection = selectionOpt.get();
    Map<String, String> placeholders = buildPlaceholders(player, selection);

    optionsConfig
      .getStaticItems()
      .forEach(config -> setConfiguredItem(config, placeholders, null));

    setActionItem("remove", placeholders, selection);
    setActionItem("invite", placeholders, selection);
    setActionItem("block", placeholders, selection);
    setActionItem("back", placeholders, selection);
  }

  private Map<String, String> buildPlaceholders(
    Player player,
    FriendView selection
  ) {
    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("player", player.getName());
    placeholders.put("player_uuid", player.getUniqueId().toString());
    placeholders.put("friend", selection.name());
    placeholders.put("friend_name", selection.name());
    placeholders.put("friend_uuid", selection.uniqueId().toString());
    placeholders.put(
      "status",
      friendMenuConfig.getStatusLabel(selection.status())
    );
    placeholders.put(
      "online",
      friendMenuConfig.getOnlineLabel(selection.online())
    );
    return placeholders;
  }

  private void setActionItem(
    String key,
    Map<String, String> placeholders,
    FriendView selection
  ) {
    MenuItemConfig config = optionsConfig.getItem(key);
    if (!config.exists()) {
      return;
    }

    setConfiguredItem(config, placeholders, event -> {
      Player clicker = (Player) event.getWhoClicked();
      switch (key) {
        case "remove" -> handleRemove(clicker, selection, config, placeholders);
        case "invite" -> handleInvite(clicker, config, placeholders);
        case "block" -> handleBlock(clicker, selection, config, placeholders);
        case "back" -> handleBack(clicker);
        default -> {}
      }
    });
  }

  private void setConfiguredItem(
    MenuItemConfig config,
    Map<String, String> placeholders,
    Consumer<InventoryClickEvent> handler
  ) {
    var item = itemFactory.buildItem(config, placeholders);
    if (item == null) {
      return;
    }
    if (handler == null) {
      setItem(config.getPosition(), item);
    } else {
      setItem(config.getPosition(), item, handler);
    }
  }

  private void handleRemove(
    Player player,
    FriendView selection,
    MenuItemConfig config,
    Map<String, String> placeholders
  ) {
    player.closeInventory();
    helektra
      .getFriendService()
      .removeFriend(player.getUniqueId(), selection.uniqueId())
      .join();
    selectionService.clear(player.getUniqueId());
    sendMessage(config, "success", player, placeholders);
    helektra.getMenuFactory().openMenu(FriendsMenu.class, player);
  }

  private void handleInvite(
    Player player,
    MenuItemConfig config,
    Map<String, String> placeholders
  ) {
    player.closeInventory();
    String command = optionsConfig.applyPlaceholders(
      optionsConfig.getCommand("invite"),
      placeholders
    );
    if (!command.isBlank()) {
      player.performCommand(command);
    }
    sendMessage(config, "success", player, placeholders);
  }

  private void handleBlock(
    Player player,
    FriendView selection,
    MenuItemConfig config,
    Map<String, String> placeholders
  ) {
    player.closeInventory();
    friendService
      .updateFriendStatus(
        player.getUniqueId(),
        selection.uniqueId(),
        IFriend.Status.BLOCKED
      )
      .join();
    selectionService.clear(player.getUniqueId());
    placeholders.put(
      "status",
      friendMenuConfig.getStatusLabel(IFriend.Status.BLOCKED)
    );
    sendMessage(config, "success", player, placeholders);
    helektra.getMenuFactory().openMenu(FriendsMenu.class, player);
  }

  private void handleBack(Player player) {
    player.closeInventory();
    helektra.getMenuFactory().openMenu(FriendsMenu.class, player);
  }

  private void sendMessage(
    MenuItemConfig config,
    String key,
    Player player,
    Map<String, String> placeholders
  ) {
    String message = config.getMessage(key);
    if (message.isEmpty()) {
      return;
    }
    player.sendMessage(optionsConfig.applyPlaceholders(message, placeholders));
  }
}
