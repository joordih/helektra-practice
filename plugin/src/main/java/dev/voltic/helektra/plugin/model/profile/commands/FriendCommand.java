package dev.voltic.helektra.plugin.model.profile.commands;

import dev.voltic.helektra.api.model.profile.IFriend;
import dev.voltic.helektra.plugin.model.profile.friend.FriendInteractionService;
import dev.voltic.helektra.plugin.model.profile.menu.FriendsMenu;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.MenuFactory;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Named;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = { "friend", "friends" })
public class FriendCommand implements CommandClass {

  private final MenuFactory menuFactory;
  private final FriendInteractionService interactionService;

  @Inject
  public FriendCommand(
    MenuFactory menuFactory,
    FriendInteractionService interactionService
  ) {
    this.menuFactory = menuFactory;
    this.interactionService = interactionService;
  }

  @Command(names = "")
  public void listFriends(@Sender Player player) {
    player.sendMessage(
      TranslationUtils.translate("friends.command.help.header")
    );
    List<String> lines = TranslationUtils.translateList(
      "friends.command.help.lines"
    );
    lines.forEach(player::sendMessage);
  }

  @Command(names = "manage")
  public void manageFriends(@Sender Player player) {
    menuFactory.openMenu(FriendsMenu.class, player);
  }

  @Command(names = "list")
  public void showFriendList(@Sender Player player) {
    if (interactionService.resolveProfile(player.getUniqueId()).isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return;
    }

    List<IFriend> friends = interactionService.getSortedFriends(
      player.getUniqueId()
    );

    if (friends.isEmpty()) {
      player.sendMessage(
        TranslationUtils.translate("friends.command.list.empty")
      );
      return;
    }

    player.sendMessage(
      TranslationUtils.translate(
        "friends.command.list.header",
        "count",
        friends.size()
      )
    );
    friends.forEach(friend -> player.sendMessage(formatFriendEntry(friend)));
  }

  @Command(names = "add")
  public void addFriend(
    @Sender Player player,
    @Named("player") String targetName
  ) {
    interactionService.addFriend(player, targetName);
  }

  @Command(names = "remove")
  public void removeFriend(
    @Sender Player player,
    @Named("player") String targetName
  ) {
    interactionService.removeFriend(player, targetName);
  }

  @Command(names = "info")
  public void friendInfo(
    @Sender Player player,
    @Named("player") String targetName
  ) {
    if (interactionService.resolveProfile(player.getUniqueId()).isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return;
    }

    Optional<IFriend> friendOpt = interactionService.findFriend(
      player.getUniqueId(),
      targetName
    );

    if (friendOpt.isEmpty()) {
      player.sendMessage(
        TranslationUtils.translate(
          "friends.command.info.not-found",
          "player",
          targetName
        )
      );
      return;
    }

    IFriend friend = friendOpt.get();
    boolean online = Bukkit.getPlayer(friend.getUniqueId()) != null;

    player.sendMessage(
      TranslationUtils.translate(
        "friends.command.info.header",
        "name",
        friend.getName()
      )
    );
    player.sendMessage(
      TranslationUtils.translate(
        "friends.command.info.status",
        "value",
        interactionService.statusLabel(friend.getStatus())
      )
    );
    player.sendMessage(
      TranslationUtils.translate(
        "friends.command.info.online",
        "value",
        interactionService.onlineLabel(online)
      )
    );
  }

  private String formatFriendEntry(IFriend friend) {
    boolean online = Bukkit.getPlayer(friend.getUniqueId()) != null;
    return TranslationUtils.translate(
      "friends.command.list.entry",
      "name",
      friend.getName(),
      "status",
      interactionService.statusLabel(friend.getStatus()),
      "online",
      interactionService.onlineLabel(online)
    );
  }
}
