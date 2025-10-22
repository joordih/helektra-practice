package dev.voltic.helektra.plugin.model.kit.layout.commands;

import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.kit.layout.menu.PlayerKitLayoutEditorMenu;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.MenuFactory;
import jakarta.inject.Inject;
import java.util.Optional;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = { "layout", "kitlayout" })
public class LayoutCommand implements CommandClass {

  private final IKitService kitService;
  private final IProfileService profileService;
  private final ProfileStateManager stateManager;
  private final MenuFactory menuFactory;

  @Inject
  public LayoutCommand(
    IKitService kitService,
    IProfileService profileService,
    ProfileStateManager stateManager,
    MenuFactory menuFactory
  ) {
    this.kitService = kitService;
    this.profileService = profileService;
    this.stateManager = stateManager;
    this.menuFactory = menuFactory;
  }

  @Command(names = { "edit" })
  public void editCommand(@Sender Player sender, String kitName) {
    Optional<IProfile> profileOpt = profileService.getCachedProfile(sender.getUniqueId());
    if (profileOpt.isEmpty()) {
      sender.sendMessage(TranslationUtils.translate("profile.error"));
      return;
    }

    IProfile profile = profileOpt.get();
    ProfileState currentState = profile.getProfileState();

    if (currentState != ProfileState.LOBBY && currentState != ProfileState.KIT_EDITOR) {
      sender.sendMessage(TranslationUtils.translate("kit.layout.invalid-state"));
      return;
    }

    if (kitService.getKit(kitName).isEmpty()) {
      sender.sendMessage(TranslationUtils.translate("kit.not-found", "kit", kitName));
      return;
    }

    stateManager.setState(sender, ProfileState.KIT_EDITOR);

    try {
      PlayerKitLayoutEditorMenu menu = menuFactory.getInstance(PlayerKitLayoutEditorMenu.class);
      menu.setupForKit(sender, kitName, currentState);
    } catch (Exception e) {
      sender.sendMessage(TranslationUtils.translate("error.generic"));
      e.printStackTrace();
      stateManager.setState(sender, currentState);
    }
  }
}
