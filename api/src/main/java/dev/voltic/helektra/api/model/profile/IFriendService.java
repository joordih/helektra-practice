package dev.voltic.helektra.api.model.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IFriendService {
  CompletableFuture<Optional<IFriend>> getFriend(UUID uuid);

  CompletableFuture<List<IFriend>> getFriendsOf(UUID ownerId);

  CompletableFuture<Void> addFriend(UUID ownerId, IFriend friend);

  CompletableFuture<Void> removeFriend(UUID ownerId, UUID friendId);

  CompletableFuture<Boolean> areFriends(UUID a, UUID b);

  CompletableFuture<Void> loadFriends(UUID ownerId);

  void cacheFriend(UUID ownerId, IFriend friend);

  void uncacheFriend(UUID ownerId, UUID friendId);

  Optional<IFriend> getCachedFriend(UUID ownerId, UUID friendId);

  CompletableFuture<Void> updateFriendStatus(
    UUID ownerId,
    UUID friendId,
    IFriend.Status status
  );

  void clearCache();
}
