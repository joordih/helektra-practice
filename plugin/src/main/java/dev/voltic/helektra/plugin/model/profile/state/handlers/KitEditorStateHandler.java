package dev.voltic.helektra.plugin.model.profile.state.handlers;

import org.bukkit.entity.Player;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.state.ProfileHotbarService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class KitEditorStateHandler implements ProfileStateHandler {

  private final ProfileHotbarService hotbarService;

  @Inject
  public KitEditorStateHandler(ProfileHotbarService hotbarService) {
    this.hotbarService = hotbarService;
  }

  @Override
  public ProfileState target() {
    return ProfileState.KIT_EDITOR;
  }

  @Override
  public void onEnter(Player player, IProfile profile) {
    hotbarService.apply(player, ProfileState.KIT_EDITOR);
  }

  @Override
  public void onExit(Player player, IProfile profile) {
  }
}
