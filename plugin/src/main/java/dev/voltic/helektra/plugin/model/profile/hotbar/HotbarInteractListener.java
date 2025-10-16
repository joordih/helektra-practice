package dev.voltic.helektra.plugin.model.profile.hotbar;

import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.state.ProfileHotbarService;
import jakarta.inject.Inject;
import java.util.Optional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class HotbarInteractListener implements Listener {
  private final IProfileService profileService;
  private final ProfileHotbarService hotbarService;
  private final HotbarActionExecutor executor;

  @Inject
  public HotbarInteractListener(IProfileService profileService, ProfileHotbarService hotbarService,
      HotbarActionExecutor executor) {
    this.profileService = profileService;
    this.hotbarService = hotbarService;
    this.executor = executor;
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (!isInteraction(event.getAction()))
      return;

    var player = event.getPlayer();
    Optional<ProfileState> stateOpt = profileService
        .getCachedProfile(player.getUniqueId())
        .map(profile -> profile.getProfileState());

    if (stateOpt.isEmpty())
      return;

    int slot = player.getInventory().getHeldItemSlot();
    Optional<String> action = hotbarService.resolveAction(stateOpt.get(), slot);
    if (action.isEmpty())
      return;

    event.setCancelled(true);
    executor.execute(player, action.get());
  }

  private boolean isInteraction(Action action) {
    return action == Action.RIGHT_CLICK_AIR
        || action == Action.RIGHT_CLICK_BLOCK
        || action == Action.LEFT_CLICK_AIR
        || action == Action.LEFT_CLICK_BLOCK;
  }
}
