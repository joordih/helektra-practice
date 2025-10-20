package dev.voltic.helektra.plugin.model.profile.friend.listeners;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.friend.FriendAddPromptService;
import dev.voltic.helektra.plugin.model.profile.friend.FriendInteractionService;
import dev.voltic.helektra.plugin.model.profile.menu.FriendsMenu;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.MenuFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Singleton
public class FriendAddPromptListener implements Listener {

  private final FriendAddPromptService promptService;
  private final FriendInteractionService interactionService;
  private final MenuFactory menuFactory;
  private final Helektra helektra;

  @Inject
  public FriendAddPromptListener(
    FriendAddPromptService promptService,
    FriendInteractionService interactionService,
    MenuFactory menuFactory,
    Helektra helektra
  ) {
    this.promptService = promptService;
    this.interactionService = interactionService;
    this.menuFactory = menuFactory;
    this.helektra = helektra;
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    if (!promptService.isPrompting(player.getUniqueId())) {
      return;
    }

    event.setCancelled(true);
    String message = event.getMessage().trim();

    Bukkit.getScheduler().runTask(helektra, () -> {
      promptService.finishPrompt(player.getUniqueId());

      if (message.equalsIgnoreCase("cancel")) {
        player.sendMessage(
          TranslationUtils.translate("friends.prompt.add.cancelled")
        );
        menuFactory.openMenu(FriendsMenu.class, player);
        return;
      }

      boolean success = interactionService.addFriend(player, message);
      if (!success) {
        player.sendMessage(
          TranslationUtils.translate("friends.prompt.add.retry")
        );
      }
      menuFactory.openMenu(FriendsMenu.class, player);
    });
  }
}
