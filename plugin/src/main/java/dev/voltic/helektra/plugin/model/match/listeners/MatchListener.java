package dev.voltic.helektra.plugin.model.match.listeners;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import jakarta.inject.Inject;

public class MatchListener implements Listener {
  private final IMatchService matchService;
  private final IProfileService profileService;
  private final ProfileStateManager profileStateManager;

  @Inject
  public MatchListener(IMatchService matchService, IProfileService profileService,
      ProfileStateManager profileStateManager) {
    this.matchService = matchService;
    this.profileService = profileService;
    this.profileStateManager = profileStateManager;
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    Optional<IProfile> profileOpt = profileService.getCachedProfile(player.getUniqueId());
    Optional<IMatch> matchOpt = matchService.getMatchByParticipant(player.getUniqueId());

    if (!profileOpt.isPresent()) {
      return;
    }

    if (profileOpt.get().getProfileState().equals(ProfileState.IN_QUEUE)) {
      profileStateManager.setState(player, ProfileState.LOBBY);
    }

    if (matchOpt.isPresent()) {
      IMatch match = matchOpt.get();

      if (match.isSpectator(player.getUniqueId())) {
        match.removeSpectator(player.getUniqueId());
        matchService.saveMatch(match);
      }
    }
  }
}
