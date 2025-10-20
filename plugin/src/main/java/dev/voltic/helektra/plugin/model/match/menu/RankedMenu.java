package dev.voltic.helektra.plugin.model.match.menu;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.kit.QueueType;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;

public class RankedMenu extends BaseQueueKitMenu {

  @Inject
  public RankedMenu(
    MenuConfigHelper menuConfig,
    IKitService kitService,
    IQueueService queueService,
    IProfileService profileService,
    ProfileStateManager profileStateManager,
    QueueMenuItemFactory itemFactory
  ) {
    super(
      menuConfig,
      "ranked-kits",
      kitService,
      queueService,
      profileService,
      profileStateManager,
      itemFactory
    );
  }

  @Override
  protected QueueType queueType() {
    return QueueType.RANKED;
  }

  @Override
  protected boolean canJoin(Player player, IKit kit) {
    if (!super.canJoin(player, kit)) {
      return false;
    }
    return profileService.getCachedProfile(player.getUniqueId())
      .map(profile -> {
        if (!profile.getSettings().getPingMatchmaking().enabled()) {
          return true;
        }
        int ping = player.getPing();
        int min = profile.getSettings().getPingMatchmaking().min();
        int max = profile.getSettings().getPingMatchmaking().max();
        if (ping < min || ping > max) {
          player.sendMessage(TranslationUtils.translate("queue.ping-restriction"));
          player.closeInventory();
          return false;
        }
        return true;
      })
      .orElse(false);
  }
}
