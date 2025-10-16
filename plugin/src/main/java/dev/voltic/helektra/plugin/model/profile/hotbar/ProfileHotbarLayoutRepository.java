package dev.voltic.helektra.plugin.model.profile.hotbar;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.bukkit.inventory.ItemStack;

import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.config.ConfigCursor;
import dev.voltic.helektra.plugin.utils.config.FileConfig;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ProfileHotbarLayoutRepository {
  private final FileConfig hotbarConfig;
  private final HotbarItemFactory itemFactory;
  private final Logger logger;

  @Inject
  public ProfileHotbarLayoutRepository(Helektra plugin, HotbarItemFactory itemFactory) {
    this.hotbarConfig = plugin.getHotbarConfig();
    this.itemFactory = itemFactory;
    this.logger = plugin.getLogger();
  }

  public Map<ProfileState, ProfileHotbarLayout> load() {
    Map<ProfileState, ProfileHotbarLayout> layouts = new EnumMap<>(ProfileState.class);
    ConfigCursor hotbarCursor = new ConfigCursor(hotbarConfig, "hotbar");

    if (!isSection(hotbarCursor))
      return layouts;

    for (String stateKey : hotbarCursor.getKeys()) {
      Optional<ProfileState> state = resolveState(stateKey);
      if (state.isEmpty())
        continue;

      ConfigCursor itemsCursor = new ConfigCursor(hotbarConfig, resolvePath(hotbarCursor, stateKey + ".items"));
      if (!isSection(itemsCursor))
        continue;

      List<ProfileHotbarItem> items = parseItems(itemsCursor);
      if (items.isEmpty())
        continue;

      layouts.put(state.get(), new ProfileHotbarLayout(items));
    }
    return layouts;
  }

  private List<ProfileHotbarItem> parseItems(ConfigCursor itemsCursor) {
    List<ProfileHotbarItem> items = new ArrayList<>();
    if (!isSection(itemsCursor))
      return items;

    for (String key : itemsCursor.getKeys()) {
      ConfigCursor itemCursor = new ConfigCursor(hotbarConfig, resolvePath(itemsCursor, key));

      int slot = getInt(itemCursor, "id", -1);
      if (slot < 0) {
        warnInvalid(key, "Missing or invalid id");
        continue;
      }

      String action = getString(itemCursor, "action", "");
      if (action == null || action.isBlank()) {
        warnInvalid(key, "Missing action");
        continue;
      }

      String material = getString(itemCursor, "material", null);
      String name = getString(itemCursor, "name", null);
      List<String> lore = getStringList(itemCursor, "lore");

      Optional<ItemStack> itemStack = itemFactory.create(material, name, lore);
      if (itemStack.isEmpty()) {
        warnInvalid(key, "Unable to create item");
        continue;
      }

      items.add(new ProfileHotbarItem(slot, itemStack.get(), action));
    }
    return items;
  }

  private Optional<ProfileState> resolveState(String key) {
    if (key == null) {
      return Optional.empty();
    }
    return switch (key.toLowerCase(Locale.ROOT)) {
      case "lobby" -> Optional.of(ProfileState.LOBBY);
      case "in_game" -> Optional.of(ProfileState.IN_GAME);
      case "in_queue" -> Optional.of(ProfileState.IN_QUEUE);
      case "kit_editor" -> Optional.of(ProfileState.KIT_EDITOR);
      case "in_party" -> Optional.of(ProfileState.IN_PARTY);
      case "spectator" -> Optional.of(ProfileState.SPECTATOR);
      case "in_event" -> Optional.of(ProfileState.IN_EVENT);
      default -> Optional.empty();
    };
  }

  private void warnInvalid(String key, String reason) {
    if (logger != null) {
      logger.warning("[Hotbar] Skipping item '" + key + "': " + reason);
    }
  }

  private boolean isSection(ConfigCursor cursor) {
    if (cursor == null)
      return false;

    String path = cursor.getPath();
    if (path == null || path.isEmpty()) {
      return cursor.getFileConfig().getConfig().getRoot() != null;
    }

    return cursor.getFileConfig().getConfig().isConfigurationSection(path);
  }

  private String resolvePath(ConfigCursor cursor, String child) {
    String base = cursor.getPath();

    if (child == null || child.isEmpty())
      return base;
    if (base == null || base.isEmpty())
      return child;

    return base + "." + child;
  }

  private int getInt(ConfigCursor cursor, String path, int defaultValue) {
    return cursor
        .getFileConfig()
        .getConfig()
        .getInt(resolvePath(cursor, path), defaultValue);
  }

  private String getString(ConfigCursor cursor, String path, String defaultValue) {
    return cursor
        .getFileConfig()
        .getConfig()
        .getString(resolvePath(cursor, path), defaultValue);
  }

  private List<String> getStringList(ConfigCursor cursor, String path) {
    return cursor
        .getFileConfig()
        .getConfig()
        .getStringList(resolvePath(cursor, path));
  }
}
