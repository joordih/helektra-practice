package dev.voltic.helektra.plugin.model.profile.state.handlers;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.state.ProfileHotbarService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateHandler;
import dev.voltic.helektra.plugin.utils.BukkitUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.entity.Player;

@Singleton
public class LobbyStateHandler implements ProfileStateHandler {

  private final ProfileHotbarService hotbarService;

  @Inject
  public LobbyStateHandler(ProfileHotbarService hotbarService) {
    this.hotbarService = hotbarService;
  }

  @Override
  public ProfileState target() {
    return ProfileState.LOBBY;
  }

  @Override
  public void onEnter(Player player, IProfile profile) {
    hotbarService.apply(player, ProfileState.LOBBY);
    BukkitUtils.setPlayerTimeSmoothly(player, profile.getSettings().getLobbyTime());
  }

  @Override
  public void onExit(Player player, IProfile profile) {
    BukkitUtils.resetPlayerTime(player);
  }
}
