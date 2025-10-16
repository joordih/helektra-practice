package dev.voltic.helektra.plugin.model.match.commands;

import com.google.inject.Inject;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.match.menu.duel.DuelKitMenu;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Named;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = "duel")
public class DuelCommand implements CommandClass {
  private final IProfileService profileService;
  private final IKitService kitService;
  private final Helektra helektra;

  @Inject
  public DuelCommand(IProfileService profileService, IKitService kitService, Helektra helektra) {
    this.profileService = profileService;
    this.kitService = kitService;
    this.helektra = helektra;
  }

  @Command(names = "")
  public void duelPlayer(@Sender Player sender, @Named("player") String targetName) {
    Player target = Bukkit.getPlayer(targetName);

    if (target == null || !target.isOnline()) {
      sender.sendMessage(TranslationUtils.translate("duel.player-not-found", "player", targetName));
      return;
    }

    if (target.getUniqueId().equals(sender.getUniqueId())) {
      sender.sendMessage(TranslationUtils.translate("duel.cannot-duel-self"));
      return;
    }

    profileService.getCachedProfile(target.getUniqueId()).ifPresentOrElse(profile -> {
      if (!profile.getSettings().isAllowDuels()) {
        sender.sendMessage(TranslationUtils.translate("duel.player-disabled-duels", "player", target.getName()));
        return;
      }

      DuelKitMenu kitMenu = new DuelKitMenu(
          helektra.getInjector().getInstance(MenuConfigHelper.class),
          kitService,
          helektra,
          target.getUniqueId());
      kitMenu.setup(sender);
      kitMenu.open(sender);
    }, () -> {
      sender.sendMessage(TranslationUtils.translate("duel.target-profile-not-loaded"));
    });
  }
}
