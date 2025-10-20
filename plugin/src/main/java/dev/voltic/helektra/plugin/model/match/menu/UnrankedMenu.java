package dev.voltic.helektra.plugin.model.match.menu;

import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.kit.QueueType;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import jakarta.inject.Inject;

public class UnrankedMenu extends BaseQueueKitMenu {

  @Inject
  public UnrankedMenu(
    MenuConfigHelper menuConfig,
    IKitService kitService,
    IQueueService queueService,
    IProfileService profileService,
    ProfileStateManager profileStateManager,
    QueueMenuItemFactory itemFactory
  ) {
    super(
      menuConfig,
      "unranked-kits",
      kitService,
      queueService,
      profileService,
      profileStateManager,
      itemFactory
    );
  }

  @Override
  protected QueueType queueType() {
    return QueueType.UNRANKED;
  }
}
