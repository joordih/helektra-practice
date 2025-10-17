package dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class LeaveQueueAction implements HotbarAction {

  private final IProfileService profileService;
  private final ProfileStateManager profileStateManager;

  @Inject
  public LeaveQueueAction(
    IProfileService profileService,
    ProfileStateManager profileStateManager
  ) {
    this.profileService = profileService;
    this.profileStateManager = profileStateManager;
  }

  @Override
  public String id() {
    return "LEAVE_QUEUE";
  }

  @Override
  public void execute(Player player) {
    var uuid = player.getUniqueId();
    Optional<IProfile> optProfile = profileService.getCachedProfile(uuid);

    optProfile.ifPresent(profile -> {
      handleLeaveQueue(profile);
      player.sendMessage(TranslationUtils.translate("queue.leave-queue"));
    });
  }

  private void handleLeaveQueue(IProfile profile) {
    if (profile.getProfileState() == ProfileState.IN_QUEUE) {
      profileStateManager.setState(
        Bukkit.getPlayer(profile.getUniqueId()),
        ProfileState.LOBBY
      );
    }
  }
}
