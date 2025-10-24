package dev.voltic.helektra.plugin.model.profile.friend;

import dev.voltic.helektra.api.model.profile.IFriend;
import dev.voltic.helektra.api.model.profile.IFriendService;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class FriendInteractionService {

  private final IProfileService profileService;
  private final IFriendService friendService;

  @Inject
  public FriendInteractionService(
    IProfileService profileService,
    IFriendService friendService
  ) {
    this.profileService = profileService;
    this.friendService = friendService;
  }

  public boolean addFriend(Player player, String targetName) {
    if (player.getName().equalsIgnoreCase(targetName)) {
      player.sendMessage(TranslationUtils.translate("friends.request.self"));
      return false;
    }

    Optional<IProfile> ownerProfile = resolveProfile(player.getUniqueId());
    if (ownerProfile.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return false;
    }

    Optional<IProfile> targetProfile = resolveProfile(targetName);
    if (targetProfile.isEmpty()) {
      player.sendMessage(
        TranslationUtils.translate(
          "friends.request.not-found",
          "player",
          targetName
        )
      );
      return false;
    }

    IProfile target = targetProfile.get();
    UUID senderId = player.getUniqueId();
    UUID receiverId = target.getUniqueId();

    if (friendService.areFriends(senderId, receiverId).join()) {
      player.sendMessage(
        TranslationUtils.translate(
          "friends.request.already-friends",
          "player",
          target.getName()
        )
      );
      return false;
    }

    List<IFriend> outgoingRequests = friendService.getOutgoingRequests(senderId).join();
    if (outgoingRequests.stream().anyMatch(f -> f.getUniqueId().equals(receiverId))) {
      player.sendMessage(
        TranslationUtils.translate(
          "friends.request.already-sent",
          "player",
          target.getName()
        )
      );
      return false;
    }

    friendService.sendFriendRequest(senderId, receiverId, target.getName()).join();

    player.sendMessage(
      TranslationUtils.translate(
        "friends.request.sent",
        "player",
        target.getName()
      )
    );

    Player targetPlayer = Bukkit.getPlayer(receiverId);
    if (targetPlayer != null) {
      targetPlayer.sendMessage(
        TranslationUtils.translate(
          "friends.request.received",
          "player",
          player.getName()
        )
      );
    }

    return true;
  }

  public boolean acceptFriendRequest(Player player, UUID senderId) {
    Optional<IProfile> receiverProfile = resolveProfile(player.getUniqueId());
    if (receiverProfile.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return false;
    }

    List<IFriend> incomingRequests = friendService.getIncomingRequests(player.getUniqueId()).join();
    Optional<IFriend> requestOpt = incomingRequests.stream()
      .filter(f -> f.getUniqueId().equals(senderId))
      .findFirst();

    if (requestOpt.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("friends.request.not-found-accept"));
      return false;
    }

    IFriend request = requestOpt.get();
    friendService.acceptFriendRequest(player.getUniqueId(), senderId).join();

    player.sendMessage(
      TranslationUtils.translate(
        "friends.request.accepted",
        "player",
        request.getName()
      )
    );

    Player senderPlayer = Bukkit.getPlayer(senderId);
    if (senderPlayer != null) {
      senderPlayer.sendMessage(
        TranslationUtils.translate(
          "friends.request.accepted-notify",
          "player",
          player.getName()
        )
      );
    }

    return true;
  }

  public boolean denyFriendRequest(Player player, UUID senderId) {
    Optional<IProfile> receiverProfile = resolveProfile(player.getUniqueId());
    if (receiverProfile.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return false;
    }

    List<IFriend> incomingRequests = friendService.getIncomingRequests(player.getUniqueId()).join();
    Optional<IFriend> requestOpt = incomingRequests.stream()
      .filter(f -> f.getUniqueId().equals(senderId))
      .findFirst();

    if (requestOpt.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("friends.request.not-found-deny"));
      return false;
    }

    IFriend request = requestOpt.get();
    friendService.denyFriendRequest(player.getUniqueId(), senderId).join();

    player.sendMessage(
      TranslationUtils.translate(
        "friends.request.denied",
        "player",
        request.getName()
      )
    );

    return true;
  }

  public boolean cancelFriendRequest(Player player, UUID receiverId) {
    Optional<IProfile> senderProfile = resolveProfile(player.getUniqueId());
    if (senderProfile.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return false;
    }

    List<IFriend> outgoingRequests = friendService.getOutgoingRequests(player.getUniqueId()).join();
    Optional<IFriend> requestOpt = outgoingRequests.stream()
      .filter(f -> f.getUniqueId().equals(receiverId))
      .findFirst();

    if (requestOpt.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("friends.request.not-found-cancel"));
      return false;
    }

    IFriend request = requestOpt.get();
    friendService.cancelFriendRequest(player.getUniqueId(), receiverId).join();

    player.sendMessage(
      TranslationUtils.translate(
        "friends.request.canceled",
        "player",
        request.getName()
      )
    );

    return true;
  }

  public boolean removeFriend(Player player, String targetName) {
    Optional<IProfile> ownerProfile = resolveProfile(player.getUniqueId());
    if (ownerProfile.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return false;
    }

    Optional<IProfile> targetProfile = resolveProfile(targetName);
    if (targetProfile.isEmpty()) {
      player.sendMessage(
        TranslationUtils.translate(
          "friends.command.remove.not-found",
          "player",
          targetName
        )
      );
      return false;
    }

    IProfile target = targetProfile.get();
    UUID ownerId = player.getUniqueId();
    UUID targetId = target.getUniqueId();

    if (!friendService.areFriends(ownerId, targetId).join()) {
      player.sendMessage(
        TranslationUtils.translate(
          "friends.command.remove.not-found",
          "player",
          target.getName()
        )
      );
      return false;
    }

    friendService.removeFriend(ownerId, targetId).join();
    if (friendService.areFriends(targetId, ownerId).join()) {
      friendService.removeFriend(targetId, ownerId).join();
    }

    player.sendMessage(
      TranslationUtils.translate(
        "friends.command.remove.success",
        "player",
        target.getName()
      )
    );

    Player targetPlayer = Bukkit.getPlayer(targetId);
    if (targetPlayer != null) {
      targetPlayer.sendMessage(
        TranslationUtils.translate(
          "friends.command.remove.notify",
          "player",
          player.getName()
        )
      );
    }

    return true;
  }

  public List<IFriend> getSortedFriends(UUID ownerId) {
    return friendService
      .getFriendsOf(ownerId)
      .join()
      .stream()
      .sorted(Comparator.comparing(IFriend::getName, String::compareToIgnoreCase))
      .collect(Collectors.toList());
  }

  public Optional<IFriend> findFriend(UUID ownerId, String targetName) {
    return friendService
      .getFriendsOf(ownerId)
      .join()
      .stream()
      .filter(friend -> friend.getName().equalsIgnoreCase(targetName))
      .findFirst();
  }

  public String statusLabel(IFriend.Status status) {
    return TranslationUtils.translate(
      "friends.status." + status.name().toLowerCase()
    );
  }

  public String onlineLabel(boolean online) {
    return TranslationUtils.translate(online ? "friends.online.yes" : "friends.online.no");
  }

  public Optional<IProfile> resolveProfile(UUID uuid) {
    Optional<IProfile> cached = profileService.getCachedProfile(uuid);
    if (cached.isPresent()) {
      return cached;
    }
    return profileService.getProfile(uuid).join();
  }

  public Optional<IProfile> resolveProfile(String name) {
    return profileService.getProfileByName(name).join();
  }
}
