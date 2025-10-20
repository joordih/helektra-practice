package dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.bukkit.entity.Player;

@Singleton
public class LeaveQueueAction implements HotbarAction {

  private final IProfileService profileService;
  private final IQueueService queueService;

  @Inject
  public LeaveQueueAction(
    IProfileService profileService,
    IQueueService queueService
  ) {
    this.profileService = profileService;
    this.queueService = queueService;
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
      queueService.leaveQueue(profile.getUniqueId());
    });
  }
}
