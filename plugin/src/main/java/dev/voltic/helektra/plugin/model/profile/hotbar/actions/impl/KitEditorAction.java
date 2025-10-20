package dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl;

import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.entity.Player;

@Singleton
public class KitEditorAction implements HotbarAction {
  private final ProfileStateManager stateManager;

  @Inject
  public KitEditorAction(ProfileStateManager stateManager) {
    this.stateManager = stateManager;
  }

  @Override
  public String id() {
    return "KIT_EDITOR";
  }

  @Override
  public void execute(Player player) {
    stateManager.setState(player, ProfileState.KIT_EDITOR);
  }
}
