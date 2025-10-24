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

  CompletableFuture<Void> sendFriendRequest(UUID senderId, UUID receiverId, String receiverName);

  CompletableFuture<Void> acceptFriendRequest(UUID receiverId, UUID senderId);

  CompletableFuture<Void> denyFriendRequest(UUID receiverId, UUID senderId);

  CompletableFuture<Void> cancelFriendRequest(UUID senderId, UUID receiverId);

  CompletableFuture<List<IFriend>> getIncomingRequests(UUID playerId);

  CompletableFuture<List<IFriend>> getOutgoingRequests(UUID playerId);

  CompletableFuture<List<IFriend>> getAcceptedFriends(UUID playerId);

  void clearCache();
}
