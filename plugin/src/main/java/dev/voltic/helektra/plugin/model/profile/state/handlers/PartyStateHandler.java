package dev.voltic.helektra.plugin.model.profile.state.handlers;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.state.ProfileHotbarService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateHandler;
import dev.voltic.helektra.plugin.utils.sound.PlayerSoundUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.entity.Player;

@Singleton
public class PartyStateHandler implements ProfileStateHandler {

  private final ProfileHotbarService hotbarService;

  @Inject
  public PartyStateHandler(ProfileHotbarService hotbarService) {
    this.hotbarService = hotbarService;
  }

  @Override
  public ProfileState target() {
    return ProfileState.IN_PARTY;
  }

  @Override
  public void onEnter(Player player, IProfile profile) {
    hotbarService.apply(player, ProfileState.IN_PARTY);
    PlayerSoundUtils.playPartyCreateSound(player);
  }

  @Override
  public void onExit(Player player, IProfile profile) {
    PlayerSoundUtils.playPartyDisbandSound(player);
  }
}
