package dev.voltic.helektra.plugin.model.profile.menu.submenus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.entity.Player;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.menu.SettingsMenu;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;

public class PingMatchmakingMenu extends InjectableMenu {

  private static final String MENU_PATH = "ping-matchmaking";
  private final Helektra helektra;

  @Inject
  public PingMatchmakingMenu(MenuConfigHelper menuConfig, Helektra helektra) {
    super(menuConfig, MENU_PATH);
    this.helektra = helektra;
  }

  public void setup(Player player) {
    Optional<IProfile> profileOpt = helektra.getProfileService().getProfile(player.getUniqueId()).join();
    if (profileOpt.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return;
    }

    IProfile profile = profileOpt.get();
    setupItems(player, profile);
  }

  private void setupItems(Player player, IProfile profile) {
    MenuItemConfig minPingDisplay = menuConfig.getItemConfig(menuPath, "min-ping-display");
    if (minPingDisplay.exists()) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("value", String.valueOf(profile.getMinPingMatchmaking()));
      String name = menuConfig.replacePlaceholders(minPingDisplay.getName(), placeholders);
      setItem(minPingDisplay.getPosition(), new ItemBuilder(minPingDisplay.getMaterial())
          .name(name)
          .lore(minPingDisplay.getLore(placeholders))
          .build());
    }

    setupPingControl(player, profile, "min-ping-decrease-10", -10, true);
    setupPingControl(player, profile, "min-ping-decrease-1", -1, true);
    setupPingControl(player, profile, "min-ping-increase-1", 1, true);
    setupPingControl(player, profile, "min-ping-increase-10", 10, true);

    MenuItemConfig maxPingDisplay = menuConfig.getItemConfig(menuPath, "max-ping-display");
    if (maxPingDisplay.exists()) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("value", String.valueOf(profile.getMaxPingMatchmaking()));
      String name = menuConfig.replacePlaceholders(maxPingDisplay.getName(), placeholders);
      setItem(maxPingDisplay.getPosition(), new ItemBuilder(maxPingDisplay.getMaterial())
          .name(name)
          .lore(maxPingDisplay.getLore(placeholders))
          .build());
    }

    setupPingControl(player, profile, "max-ping-decrease-10", -10, false);
    setupPingControl(player, profile, "max-ping-decrease-1", -1, false);
    setupPingControl(player, profile, "max-ping-increase-1", 1, false);
    setupPingControl(player, profile, "max-ping-increase-10", 10, false);
    MenuItemConfig backButton = menuConfig.getItemConfig(menuPath, "back");
    if (backButton.exists()) {
      setItem(backButton.getPosition(), new ItemBuilder(backButton.getMaterial())
          .name(backButton.getName())
          .lore(backButton.getLore())
          .build(), e -> helektra.getMenuFactory().openMenu(SettingsMenu.class, player));
    }

    setupPreset(player, profile, "preset-low", 0, 50);
    setupPreset(player, profile, "preset-medium", 0, 150);
    setupPreset(player, profile, "preset-unlimited", 0, 500);
  }

  private void setupPingControl(Player player, IProfile profile, String itemKey, int increment, boolean isMin) {
    MenuItemConfig control = menuConfig.getItemConfig(menuPath, itemKey);
    if (!control.exists())
      return;

    setItem(control.getPosition(), new ItemBuilder(control.getMaterial())
        .name(control.getName())
        .lore(control.getLore())
        .build(), e -> {
          int newValue;
          if (isMin) {
            int currentMin = profile.getMinPingMatchmaking();
            if (increment > 0) {
              newValue = Math.min(currentMin + increment,
                  profile.getMaxPingMatchmaking() - (increment > 5 ? 10 : 1));
            } else {
              newValue = Math.max(currentMin + increment, 0);
            }
            profile.setMinPingMatchmaking(newValue);
          } else {
            int currentMax = profile.getMaxPingMatchmaking();
            if (increment > 0) {
              newValue = Math.min(currentMax + increment, 500);
            } else {
              newValue = Math.max(currentMax + increment,
                  profile.getMinPingMatchmaking() + (increment < -5 ? 10 : 1));
            }
            profile.setMaxPingMatchmaking(newValue);
          }
          helektra.getProfileService().saveProfile(profile);
          helektra.getMenuFactory().openMenu(PingMatchmakingMenu.class, player);
        });
  }

  private void setupPreset(Player player, IProfile profile, String itemKey, int minPing, int maxPing) {
    MenuItemConfig preset = menuConfig.getItemConfig(menuPath, itemKey);
    if (!preset.exists())
      return;

    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("min", String.valueOf(minPing));
    placeholders.put("max", String.valueOf(maxPing));

    setItem(preset.getPosition(), new ItemBuilder(preset.getMaterial())
        .name(preset.getName())
        .lore(preset.getLore(placeholders))
        .build(), e -> {
          profile.setMinPingMatchmaking(minPing);
          profile.setMaxPingMatchmaking(maxPing);
          helektra.getProfileService().saveProfile(profile);
          player.sendMessage(preset.getMessage("applied"));
          helektra.getMenuFactory().openMenu(PingMatchmakingMenu.class, player);
        });
  }
}
