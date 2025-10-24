package dev.voltic.helektra.plugin.model.profile.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.LobbyTime;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.menu.submenus.PingMatchmakingMenu;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.DynamicMenu;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;

public class SettingsMenu extends DynamicMenu {

  private static final String MENU_PATH = "settings";
  private static final String KEY_FILLER = "filler";
  private static final String KEY_LOBBY_TIME = "lobby-time";

  private final Helektra plugin;
  private final Map<String, ToggleDescriptor> toggleMap;

  @Inject
  public SettingsMenu(MenuConfigHelper configHelper, Helektra plugin) {
    super(configHelper, MENU_PATH);
    this.plugin = plugin;
    this.toggleMap = createToggleDescriptors();
  }

  @Override
  public void setup(Player player) {
    Optional<IProfile> optionalProfile = plugin.getProfileService().getProfile(player.getUniqueId()).join();
    if (optionalProfile.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return;
    }
    render(player, optionalProfile.get());
  }

  private void render(Player player, IProfile profile) {
    clear();
    initializePagination();

    Map<String, MenuItemConfig> itemConfigs = getAllItemConfigs();
    List<String> dynamicKeys = new ArrayList<>();

    for (Map.Entry<String, MenuItemConfig> entry : itemConfigs.entrySet()) {
      String key = entry.getKey();
      MenuItemConfig itemConfig = entry.getValue();
      if (!itemConfig.exists() || KEY_FILLER.equalsIgnoreCase(key) || key.startsWith("pagination")) continue;

      ItemStack item = buildItem(profile, itemConfig, key);
      if (itemConfig.hasExplicitSlot()) {
        setStaticItemWithHandler(itemConfig.getPrimarySlot(), item, e -> handleClick(player, profile, key, e));
      } else {
        dynamicKeys.add(key);
      }
    }

    for (String key : dynamicKeys) {
      MenuItemConfig itemConfig = itemConfigs.get(key);
      ItemStack item = buildItem(profile, itemConfig, key);
      addDynamicItemWithHandler(item, e -> handleClick(player, profile, key, e));
    }

    fillFiller(itemConfigs.get(KEY_FILLER));
    completeRefresh();
    setupPaginationItems(player);
  }

  private ItemStack buildItem(IProfile profile, MenuItemConfig itemConfig, String key) {
    if (KEY_LOBBY_TIME.equalsIgnoreCase(key)) return buildLobbyTimeItem(profile, itemConfig);
    ToggleDescriptor descriptor = toggleMap.get(key);
    if (descriptor != null) return buildToggleItem(profile, itemConfig, descriptor);
    return buildStatic(itemConfig);
  }

  private ItemStack buildToggleItem(IProfile profile, MenuItemConfig itemConfig, ToggleDescriptor descriptor) {
    boolean enabled = descriptor.currentValue().apply(profile);
    Map<String, String> placeholders = Map.of(
      "%description%", itemConfig.getString("description", ""),
      "%status%", enabled ? "&aYes" : "&cNo",
      "%status_yes%", statusLine(itemConfig, true, enabled),
      "%status_no%", statusLine(itemConfig, false, enabled)
    );
    return new ItemBuilder(itemConfig.getMaterial(enabled))
      .name(applyPlaceholders(itemConfig.getName(enabled), placeholders))
      .lore(applyPlaceholders(itemConfig.getLore(), placeholders))
      .build();
  }

  private ItemStack buildLobbyTimeItem(IProfile profile, MenuItemConfig itemConfig) {
    LobbyTime current = profile.getSettings().getLobbyTime();
    Map<String, String> placeholders = Map.of(
      "%description%", itemConfig.getString("description", ""),
      "%status%", resolveLobbyTimeStatus(itemConfig, current)
    );
    return new ItemBuilder(itemConfig.getMaterial())
      .name(applyPlaceholders(itemConfig.getName(), placeholders))
      .lore(applyPlaceholders(itemConfig.getLore(), placeholders))
      .build();
  }

  private void handleClick(Player player, IProfile profile, String key, InventoryClickEvent event) {
    event.setCancelled(true);
    MenuItemConfig itemConfig = getAllItemConfigs().get(key);
    if (itemConfig == null) return;

    if (KEY_LOBBY_TIME.equalsIgnoreCase(key)) {
      handleLobbyTimeClick(player, profile, itemConfig);
      return;
    }

    ToggleDescriptor descriptor = toggleMap.get(key);
    if (descriptor != null) handleToggleClick(player, profile, itemConfig, descriptor, event);
  }

  private void handleToggleClick(Player player, IProfile profile, MenuItemConfig itemConfig, ToggleDescriptor descriptor, InventoryClickEvent event) {
    if (event.isRightClick() && descriptor.rightClickAction() != null) {
      descriptor.rightClickAction().accept(player, profile);
      return;
    }
    boolean newState = descriptor.toggleAction().apply(profile);
    plugin.getProfileService().saveProfile(profile);
    sendToggleMessage(player, itemConfig, newState);
    render(player, profile);
  }

  private void handleLobbyTimeClick(Player player, IProfile profile, MenuItemConfig itemConfig) {
    LobbyTime next = nextLobbyTime(profile.getSettings().getLobbyTime());
    profile.getSettings().setLobbyTime(next);
    plugin.getProfileService().saveProfile(profile);
    sendLobbyTimeMessage(player, itemConfig, next);
    render(player, profile);
  }

  private void sendToggleMessage(Player player, MenuItemConfig itemConfig, boolean enabled) {
    String messageKey = itemConfig.getRawString(enabled ? "messages.enabled" : "messages.disabled", "");
    if (!messageKey.isBlank()) player.sendMessage(TranslationUtils.translate(messageKey));
  }

  private void sendLobbyTimeMessage(Player player, MenuItemConfig itemConfig, LobbyTime time) {
    String messageKey = itemConfig.getRawString("messages.updated", "");
    if (!messageKey.isBlank()) player.sendMessage(TranslationUtils.translate(messageKey, "time", time.name()));
  }

  private LobbyTime nextLobbyTime(LobbyTime current) {
    return switch (current) {
      case DAY -> LobbyTime.AFTERNOON;
      case AFTERNOON -> LobbyTime.NIGHT;
      case NIGHT -> LobbyTime.DAY;
    };
  }

  private String statusLine(MenuItemConfig itemConfig, boolean yes, boolean enabled) {
    String yesText = itemConfig.getString("placeholders.yes", "Yes");
    String noText = itemConfig.getString("placeholders.no", "No");
    String selectedYesPrefix = itemConfig.getString("placeholders.selected-yes-prefix", "&e&l█ &a&l");
    String selectedNoPrefix = itemConfig.getString("placeholders.selected-no-prefix", "&e&l█ &c&l");
    String unselectedPrefix = itemConfig.getString("placeholders.unselected-prefix", "&e&l▌ &7");

    boolean selected = enabled == yes;
    String text = yes ? yesText : noText;
    String prefix = selected ? (yes ? selectedYesPrefix : selectedNoPrefix) : unselectedPrefix;

    return ColorUtils.translate("§r" + prefix + ColorUtils.stripColor(text));
  }

  private List<String> applyPlaceholders(List<String> lines, Map<String, String> placeholders) {
    return lines.stream().map(line -> applyPlaceholders(line, placeholders)).toList();
  }

  private String applyPlaceholders(String line, Map<String, String> placeholders) {
    String result = line;
    for (Map.Entry<String, String> entry : placeholders.entrySet()) result = result.replace(entry.getKey(), entry.getValue());
    return ColorUtils.translate(result);
  }

  private String resolveLobbyTimeStatus(MenuItemConfig itemConfig, LobbyTime time) {
    return itemConfig.getString("states." + time.name().toLowerCase(), "&e" + time.name());
  }

  private ItemStack buildStatic(MenuItemConfig itemConfig) {
    return new ItemBuilder(itemConfig.getMaterial()).name(itemConfig.getName()).lore(itemConfig.getLore()).build();
  }

  private Map<String, ToggleDescriptor> createToggleDescriptors() {
    return Map.of(
      "duel-requests", new ToggleDescriptor(
        p -> p.getSettings().isAllowDuels(),
        p -> toggleBoolean(p.getSettings()::isAllowDuels, p.getSettings()::setAllowDuels)
      ),
      "party-requests", new ToggleDescriptor(
        p -> p.getSettings().isAllowParty(),
        p -> toggleBoolean(p.getSettings()::isAllowParty, p.getSettings()::setAllowParty)
      ),
      "spectators", new ToggleDescriptor(
        p -> p.getSettings().isAllowSpectators(),
        p -> toggleBoolean(p.getSettings()::isAllowSpectators, p.getSettings()::setAllowSpectators)
      ),
      "view-players", new ToggleDescriptor(
        p -> p.getSettings().isViewPlayers(),
        p -> toggleBoolean(p.getSettings()::isViewPlayers, p.getSettings()::setViewPlayers)
      ),
      "event-messages", new ToggleDescriptor(
        p -> p.getSettings().isViewEventMessages(),
        p -> toggleBoolean(p.getSettings()::isViewEventMessages, p.getSettings()::setViewEventMessages)
      ),
      "kit-mode", new ToggleDescriptor(
        p -> p.getSettings().isKitMode(),
        p -> toggleBoolean(p.getSettings()::isKitMode, p.getSettings()::setKitMode)
      ),
      "ping-matchmaking", new ToggleDescriptor(
        p -> p.getSettings().getPingMatchmaking().enabled(),
        p -> toggleBoolean(p.getSettings().getPingMatchmaking()::enabled, p.getSettings().getPingMatchmaking()::setEnabled),
        (pl, pr) -> plugin.getMenuFactory().openMenu(PingMatchmakingMenu.class, pl)
      )
    );
  }

  private boolean toggleBoolean(Supplier<Boolean> getter, Consumer<Boolean> setter) {
    boolean newValue = !getter.get();
    setter.accept(newValue);
    return newValue;
  }

  private record ToggleDescriptor(
    Function<IProfile, Boolean> currentValue,
    Function<IProfile, Boolean> toggleAction,
    BiConsumer<Player, IProfile> rightClickAction
  ) {
    public ToggleDescriptor(Function<IProfile, Boolean> currentValue, Function<IProfile, Boolean> toggleAction) {
      this(currentValue, toggleAction, null);
    }
  }
}