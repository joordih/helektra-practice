package dev.voltic.helektra.plugin.model.profile.listeners;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.Profile;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ProfileListeners implements Listener {

  private final IProfileService profileService;
  private final ProfileStateManager profileStateManager;

  @Inject
  public ProfileListeners(IProfileService profileService, ProfileStateManager profileStateManager) {
    this.profileService = profileService;
    this.profileStateManager = profileStateManager;
  }

  @EventHandler
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    var uuid = event.getUniqueId();
    Optional<IProfile> profileOpt = profileService.getProfile(uuid).join();
    IProfile profile = profileOpt.orElseGet(() -> new Profile(uuid.toString(), event.getName()));
    profileService.saveProfile(profile).join();
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    var uuid = player.getUniqueId();

    Optional<IProfile> cachedProfile = profileService.getCachedProfile(uuid);
    boolean fromCache = cachedProfile.isPresent();
    Optional<IProfile> profileOpt = fromCache ? cachedProfile : profileService.getProfile(uuid).join();

    if (profileOpt.isEmpty()) {
      player.kickPlayer(TranslationUtils.translate("profile.error"));
      return;
    }

    IProfile profile = profileOpt.get();
    if (profile.getProfileState() != ProfileState.LOBBY) {
      profile.setProfileState(ProfileState.LOBBY);
      profileService.saveProfile(profile).join();
    }

    if (!fromCache) {
      profileService.cacheProfile(profile);
    }

    profileStateManager.setState(player, ProfileState.LOBBY);
    player.sendMessage(TranslationUtils.translate("profile.loaded"));
  }
}
