package dev.voltic.helektra.plugin.model.kit.layout.menu;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import dev.voltic.helektra.plugin.utils.menu.MenuFactory;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KitLayoutSelectorMenu extends InjectableMenu {

  private final IKitService kitService;
  private final IProfileService profileService;
  private final ProfileStateManager stateManager;
  private final MenuFactory menuFactory;
  private final JavaPlugin plugin;

  @Inject
  public KitLayoutSelectorMenu(
    MenuConfigHelper menuConfig,
    IKitService kitService,
    IProfileService profileService,
    ProfileStateManager stateManager,
    MenuFactory menuFactory,
    JavaPlugin plugin
  ) {
    super(menuConfig, "kit-layout-selector");
    this.kitService = kitService;
    this.profileService = profileService;
    this.stateManager = stateManager;
    this.menuFactory = menuFactory;
    this.plugin = plugin;
  }

  @Override
  public void setup(Player player) {
    Optional<IProfile> profileOpt = profileService.getCachedProfile(
      player.getUniqueId()
    );
    if (profileOpt.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      player.closeInventory();
      return;
    }

    IProfile profile = profileOpt.get();
    List<IKit> kits = kitService.getAllKits();

    if (kits.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("kit.list-empty"));
      player.closeInventory();
      return;
    }

    int slot = 0;
    for (IKit kit : kits) {
      if (slot >= getInventory().getSize()) {
        break;
      }

      final String kitName = kit.getName();
      final String kitDisplayName = kit.getDisplayName();

      setItem(
        slot,
        new ItemBuilder(kit.getIcon().clone())
          .name(
            menuConfig.getString(
              menuPath + ".kit-name-format",
              "&e{kit}"
            ).replace("{kit}", kitDisplayName)
          )
          .lore(
            menuConfig.getStringList(menuPath + ".kit-lore")
          )
          .build(),
        event -> {
          event.setCancelled(true);
          player.closeInventory();
          
          if (kitService.getKit(kitName).isEmpty()) {
            player.sendMessage(
              TranslationUtils.translate("kit.not-found", "kit", kitName)
            );
            return;
          }

          ProfileState currentState = profile.getProfileState();
          
          if (currentState != ProfileState.LOBBY && currentState != ProfileState.KIT_EDITOR) {
            player.sendMessage(TranslationUtils.translate("kit.layout.invalid-state"));
            return;
          }

          stateManager.setState(player, ProfileState.KIT_EDITOR);
          player.sendMessage(
            TranslationUtils.translate("kit.layout.opened", "kit", kitDisplayName)
          );

          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
              PlayerKitLayoutEditorMenu editorMenu = menuFactory.getInstance(
                PlayerKitLayoutEditorMenu.class
              );
              editorMenu.setupForKit(player, kitName, currentState);
            } catch (Exception e) {
              player.sendMessage(TranslationUtils.translate("error.generic"));
              e.printStackTrace();
              stateManager.setState(player, currentState);
            }
          }, 1L);
        }
      );
      slot++;
    }
  }
}
