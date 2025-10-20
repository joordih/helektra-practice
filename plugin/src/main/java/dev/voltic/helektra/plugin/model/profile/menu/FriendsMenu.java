package dev.voltic.helektra.plugin.model.profile.menu;

import dev.voltic.helektra.api.model.profile.IFriend;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.friend.FriendAddPromptService;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuConfig;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuItemFactory;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuItemFactory.FriendView;
import dev.voltic.helektra.plugin.model.profile.menu.friend.FriendMenuSelectionService;
import dev.voltic.helektra.plugin.model.profile.menu.submenus.FriendOptionsMenu;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectablePaginatedMenu;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FriendsMenu extends InjectablePaginatedMenu {

  private final Helektra helektra;
  private final FriendMenuConfig friendMenuConfig;
  private final FriendMenuItemFactory itemFactory;
  private final FriendMenuSelectionService selectionService;
  private final FriendAddPromptService addPromptService;

  private List<Integer> contentSlots = List.of();
  private ItemStack emptyItem;

  @Inject
  public FriendsMenu(
    MenuConfigHelper menuConfigHelper,
    Helektra helektra,
    FriendMenuConfig friendMenuConfig,
    FriendMenuItemFactory itemFactory,
    FriendMenuSelectionService selectionService,
    FriendAddPromptService addPromptService
  ) {
    super(menuConfigHelper, friendMenuConfig.getMenuPath());
    this.helektra = helektra;
    this.friendMenuConfig = friendMenuConfig;
    this.itemFactory = itemFactory;
    this.selectionService = selectionService;
    this.addPromptService = addPromptService;
  }

  @Override
  public void setup(Player player) {
    getInventory().clear();
    clearContent();
    selectionService.clear(player.getUniqueId());

    Optional<IProfile> profileOpt = helektra
      .getProfileService()
      .getProfile(player.getUniqueId())
      .join();
    if (profileOpt.isEmpty()) {
      String message = friendMenuConfig.getProfileMissingMessage();
      if (message.isEmpty()) {
        player.sendMessage(TranslationUtils.translate("profile.error"));
      } else {
        player.sendMessage(
          friendMenuConfig.applyPlaceholders(
            message,
            Map.of("player", player.getName())
          )
        );
      }
      return;
    }

    IProfile profile = profileOpt.get();
    List<IFriend> friends = profile.getFriends().join();
    List<FriendView> views = friends
      .stream()
      .map(itemFactory::toView)
      .collect(Collectors.toList());

    contentSlots = resolveContentSlots();
    setContentSlots(contentSlots);

    Map<String, String> basePlaceholders = buildBasePlaceholders(
      player,
      views.size()
    );

    applyStaticItems(basePlaceholders);
    applyAddFriendItem(basePlaceholders);
    emptyItem = itemFactory.buildEmptyItem(basePlaceholders);

    populateFriends(views, basePlaceholders);
    applyPagination(basePlaceholders);

    if (views.isEmpty()) {
      String message = friendMenuConfig.getEmptyMessage();
      if (!message.isEmpty()) {
        player.sendMessage(
          friendMenuConfig.applyPlaceholders(message, basePlaceholders)
        );
      }
    }
  }

  @Override
  protected void onPageChange(int page) {
    if (emptyItem == null) {
      return;
    }
    for (int slot : contentSlots) {
      if (getInventory().getItem(slot) == null) {
        setItem(slot, emptyItem.clone());
      }
    }
  }

  private List<Integer> resolveContentSlots() {
    List<Integer> slots = friendMenuConfig.getContentSlots();
    if (slots.isEmpty()) {
      return IntStream.range(0, getInventory().getSize()).boxed().toList();
    }
    return new ArrayList<>(slots);
  }

  private Map<String, String> buildBasePlaceholders(
    Player player,
    int totalFriends
  ) {
    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("player", player.getName());
    placeholders.put("player_uuid", player.getUniqueId().toString());
    placeholders.put("total", String.valueOf(totalFriends));
    placeholders.put("friend_count", String.valueOf(totalFriends));
    placeholders.put("visible_slots", String.valueOf(contentSlots.size()));
    return placeholders;
  }

  private void applyStaticItems(Map<String, String> placeholders) {
    friendMenuConfig
      .getStaticItems()
      .forEach(config -> {
        ItemStack item = itemFactory.buildItem(config, placeholders);
        if (item != null) {
          setItem(config.getPosition(), item);
        }
      });
  }

  private void applyAddFriendItem(Map<String, String> placeholders) {
    MenuItemConfig addFriend = friendMenuConfig.getAddFriendItem();
    if (!addFriend.exists()) {
      return;
    }
    ItemStack item = itemFactory.buildItem(addFriend, placeholders);
    if (item == null) {
      return;
    }
    setItem(addFriend.getPosition(), item, event -> {
      Player clicker = (Player) event.getWhoClicked();
      handleAddFriendClick(clicker);
    });
  }

  private void handleAddFriendClick(Player player) {
    player.closeInventory();
    if (!addPromptService.beginPrompt(player.getUniqueId())) {
      player.sendMessage(
        TranslationUtils.translate("friends.prompt.add.already")
      );
      return;
    }
    player.sendMessage(TranslationUtils.translate("friends.prompt.add.start"));
  }

  private void populateFriends(
    List<FriendView> views,
    Map<String, String> basePlaceholders
  ) {
    clearContent();
    int index = 1;
    for (FriendView view : views) {
      ItemStack item = itemFactory.buildFriendItem(
        view,
        basePlaceholders,
        index,
        views.size()
      );
      if (item != null) {
        FriendView friendView = view;
        addContent(item, event -> {
          Player clicker = (Player) event.getWhoClicked();
          selectionService.select(clicker.getUniqueId(), friendView);
          helektra.getMenuFactory().openMenu(FriendOptionsMenu.class, clicker);
        });
      }
      index++;
    }
  }

  private void applyPagination(Map<String, String> basePlaceholders) {
    MenuItemConfig previous = friendMenuConfig.getPreviousItem();
    if (previous.exists()) {
      previousPageItem(previous.getPosition(), targetPage -> {
        Map<String, String> placeholders = buildPaginationPlaceholders(
          basePlaceholders,
          targetPage
        );
        ItemStack item = itemFactory.buildPaginationItem(
          previous,
          placeholders
        );
        return item != null
          ? item
          : new ItemBuilder(previous.getMaterial()).build();
      });
    }

    MenuItemConfig next = friendMenuConfig.getNextItem();
    if (next.exists()) {
      nextPageItem(next.getPosition(), targetPage -> {
        Map<String, String> placeholders = buildPaginationPlaceholders(
          basePlaceholders,
          targetPage
        );
        ItemStack item = itemFactory.buildPaginationItem(next, placeholders);
        return item != null
          ? item
          : new ItemBuilder(next.getMaterial()).build();
      });
    }
  }

  private Map<String, String> buildPaginationPlaceholders(
    Map<String, String> basePlaceholders,
    int targetPage
  ) {
    Map<String, String> placeholders = new HashMap<>(basePlaceholders);
    placeholders.put("target-page", String.valueOf(targetPage));
    placeholders.put("page", String.valueOf(targetPage));
    placeholders.put("current-page", String.valueOf(currentPage()));
    int last = Math.max(1, lastPage());
    placeholders.put("last-page", String.valueOf(last));
    placeholders.put("total-pages", String.valueOf(last));
    return placeholders;
  }
}
