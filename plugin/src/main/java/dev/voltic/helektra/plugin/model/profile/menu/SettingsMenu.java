package dev.voltic.helektra.plugin.model.profile.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.entity.Player;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.LobbyTime;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.menu.submenus.PingMatchmakingMenu;
import dev.voltic.helektra.plugin.utils.BukkitUtils;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;

public class SettingsMenu extends InjectableMenu {

  private final Helektra helektra;

  @Inject
  public SettingsMenu(MenuConfigHelper menuConfig, Helektra helektra) {
    super(menuConfig, "settings");
    this.helektra = helektra;
  }

  @Override
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
    MenuItemConfig duelItem = menuConfig.getItemConfig(menuPath, "duel-requests");
    if (duelItem.exists()) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("status", profile.isAllowDuels() 
          ? duelItem.getString("placeholders.status-enabled", "&aEnabled")
          : duelItem.getString("placeholders.status-disabled", "&cDisabled"));

      setItem(duelItem.getPosition(), new ItemBuilder(duelItem.getMaterial())
          .name(duelItem.getName(profile.isAllowDuels()))
          .lore(duelItem.getLore(placeholders))
          .build(), event -> {
            profile.setAllowDuels(!profile.isAllowDuels());
            helektra.getProfileService().saveProfile(profile);
            helektra.getMenuFactory().openMenu(SettingsMenu.class, player);
            player.sendMessage(profile.isAllowDuels()
                ? duelItem.getMessage("enabled")
                : duelItem.getMessage("disabled"));
          });
    }

    MenuItemConfig pingItem = menuConfig.getItemConfig(menuPath, "ping-matchmaking");
    if (pingItem.exists()) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("status", profile.isPingMatchmakingEnabled()
          ? pingItem.getString("status-enabled")
          : pingItem.getString("status-disabled"));

      setItem(pingItem.getPosition(),
          new ItemBuilder(pingItem.getMaterial(profile.isPingMatchmakingEnabled()))
              .name(pingItem.getName(profile.isPingMatchmakingEnabled()))
              .lore(pingItem.getLore(placeholders))
              .build(),
          event -> {
            if (event.isLeftClick()) {
              profile.setPingMatchmakingEnabled(!profile.isPingMatchmakingEnabled());

              helektra.getProfileService().saveProfile(profile);
              helektra.getMenuFactory().openMenu(SettingsMenu.class, player);
            }
            if (event.isRightClick()) {
              helektra.getMenuFactory().openMenu(PingMatchmakingMenu.class, player);
            }
          });
    }

    MenuItemConfig timeItem = menuConfig.getItemConfig(menuPath, "lobby-time");
    if (timeItem.exists()) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("day", profile.getLobbyTime() == LobbyTime.DAY
          ? timeItem.getString("placeholders.day-selected", "&a&l&oDAY")
          : timeItem.getString("placeholders.day-display", "&fDay"));
      placeholders.put("afternoon", profile.getLobbyTime() == LobbyTime.AFTERNOON
          ? timeItem.getString("placeholders.afternoon-selected", "&a&l&oAFTERNOON")
          : timeItem.getString("placeholders.afternoon-display", "&fAfternoon"));
      placeholders.put("night", profile.getLobbyTime() == LobbyTime.NIGHT
          ? timeItem.getString("placeholders.night-selected", "&a&l&oNIGHT")
          : timeItem.getString("placeholders.night-display", "&fNight"));

      setItem(timeItem.getPosition(), new ItemBuilder(timeItem.getMaterial())
          .name(timeItem.getName())
          .lore(timeItem.getLore(placeholders))
          .build(), event -> {
            profile.setLobbyTime(switch (profile.getLobbyTime()) {
              case DAY -> LobbyTime.AFTERNOON;
              case AFTERNOON -> LobbyTime.NIGHT;
              case NIGHT -> LobbyTime.DAY;
            });
            helektra.getProfileService().saveProfile(profile);
            BukkitUtils.setPlayerTimeSmoothly(player, profile.getLobbyTime());

            helektra.getMenuFactory().openMenu(SettingsMenu.class, player);
            player.sendMessage(timeItem.getString("message"));
          });
    }

    MenuItemConfig eventItem = menuConfig.getItemConfig(menuPath, "event-messages");
    if (eventItem.exists()) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("status", profile.isViewEventMessages()
          ? eventItem.getString("placeholders.status-enabled", "&aEnabled")
          : eventItem.getString("placeholders.status-disabled", "&cDisabled"));

      setItem(eventItem.getPosition(), new ItemBuilder(eventItem.getMaterial())
          .name(eventItem.getName(profile.isViewEventMessages()))
          .lore(eventItem.getLore(placeholders))
          .build(), event -> {
            profile.setViewEventMessages(!profile.isViewEventMessages());
            helektra.getProfileService().saveProfile(profile);
            helektra.getMenuFactory().openMenu(SettingsMenu.class, player);

            String message = profile.isViewEventMessages()
                ? eventItem.getMessage("enabled")
                : eventItem.getMessage("disabled");
            player.sendMessage(message);
          });
    }

    MenuItemConfig viewPlayersItem = menuConfig.getItemConfig(menuPath, "view-players");
    if (viewPlayersItem.exists()) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("status", profile.isViewPlayers()
          ? viewPlayersItem.getString("placeholders.status-enabled", "&aEnabled")
          : viewPlayersItem.getString("placeholders.status-disabled", "&cDisabled"));

      setItem(viewPlayersItem.getPosition(), new ItemBuilder(viewPlayersItem.getMaterial())
          .name(viewPlayersItem.getName(profile.isViewPlayers()))
          .lore(viewPlayersItem.getLore(placeholders))
          .build(), event -> {
            profile.setViewPlayers(!profile.isViewPlayers());
            helektra.getProfileService().saveProfile(profile);
            helektra.getMenuFactory().openMenu(SettingsMenu.class, player);

            String message = profile.isViewPlayers()
                ? viewPlayersItem.getMessage("enabled")
                : viewPlayersItem.getMessage("disabled");
            player.sendMessage(message);
          });
    }

    MenuItemConfig partyItem = menuConfig.getItemConfig(menuPath, "party-requests");
    if (partyItem.exists()) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("status", profile.isAllowParty()
          ? partyItem.getString("placeholders.status-enabled", "&aEnabled")
          : partyItem.getString("placeholders.status-disabled", "&cDisabled"));

      setItem(partyItem.getPosition(), new ItemBuilder(partyItem.getMaterial())
          .name(partyItem.getName(profile.isAllowParty()))
          .lore(partyItem.getLore(placeholders))
          .build(), event -> {
            profile.setAllowParty(!profile.isAllowParty());
            helektra.getProfileService().saveProfile(profile);
            helektra.getMenuFactory().openMenu(SettingsMenu.class, player);

            String message = profile.isAllowParty()
                ? partyItem.getMessage("enabled")
                : partyItem.getMessage("disabled");
            player.sendMessage(message);
          });
    }

    MenuItemConfig spectatorsItem = menuConfig.getItemConfig(menuPath, "spectators");
    if (spectatorsItem.exists()) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("status", profile.isAllowSpectators()
          ? spectatorsItem.getString("placeholders.status-enabled", "&aEnabled")
          : spectatorsItem.getString("placeholders.status-disabled", "&cDisabled"));

      setItem(spectatorsItem.getPosition(), new ItemBuilder(spectatorsItem.getMaterial())
          .name(spectatorsItem.getName(profile.isAllowSpectators()))
          .lore(spectatorsItem.getLore(placeholders))
          .build(), event -> {
            profile.setAllowSpectators(!profile.isAllowSpectators());
            helektra.getProfileService().saveProfile(profile);
            helektra.getMenuFactory().openMenu(SettingsMenu.class, player);

            String message = profile.isAllowSpectators()
                ? spectatorsItem.getMessage("enabled")
                : spectatorsItem.getMessage("disabled");
            player.sendMessage(message);
          });
    }
  }
}
