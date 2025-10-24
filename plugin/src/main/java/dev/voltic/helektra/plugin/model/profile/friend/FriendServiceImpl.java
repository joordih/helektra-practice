package dev.voltic.helektra.plugin.model.profile.friend;

import dev.voltic.helektra.api.model.profile.IFriend;
import dev.voltic.helektra.api.model.profile.IFriendService;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.model.profile.friend.repository.FriendRepository;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FriendServiceImpl implements IFriendService {

  private final FriendRepository repository;
  private final Provider<IProfileService> profileServiceProvider;
  private final Map<UUID, Map<UUID, Friend>> cache = new ConcurrentHashMap<>();

  @Override
  public CompletableFuture<Optional<IFriend>> getFriend(UUID uuid) {
    UUID ownerId = extractOwnerFromComposite(uuid);
    UUID friendId = extractFriendFromComposite(uuid);
    return getCachedFriend(ownerId, friendId)
      .map(f -> CompletableFuture.completedFuture(Optional.of(f)))
      .orElseGet(() ->
        repository
          .findById(ownerId, friendId)
          .thenApply(opt -> {
            opt.ifPresent(f -> cacheFriend(ownerId, f));
            return opt.map(f -> (IFriend) f);
          })
      );
  }

  @Override
  public CompletableFuture<List<IFriend>> getFriendsOf(UUID ownerId) {
    if (cache.containsKey(ownerId)) {
      return CompletableFuture.completedFuture(
        cache
          .get(ownerId)
          .values()
          .stream()
          .map(f -> (IFriend) f)
          .collect(Collectors.toList())
      );
    }
    return repository
      .findAllByOwner(ownerId)
      .thenApply(list -> {
        list.forEach(f -> cacheFriend(ownerId, f));
        return list
          .stream()
          .map(f -> (IFriend) f)
          .collect(Collectors.toList());
      });
  }

  @Override
  public CompletableFuture<Void> addFriend(UUID ownerId, IFriend friend) {
    if (!(friend instanceof Friend friendImpl)) {
      return CompletableFuture.failedFuture(
        new IllegalArgumentException(
          "Friend must be an instance of Friend class"
        )
      );
    }
    cacheFriend(ownerId, friendImpl);
    return repository.save(ownerId, friendImpl);
  }

  @Override
  public CompletableFuture<Void> removeFriend(UUID ownerId, UUID friendId) {
    uncacheFriend(ownerId, friendId);
    return repository.delete(ownerId, friendId);
  }

  @Override
  public CompletableFuture<Boolean> areFriends(UUID a, UUID b) {
    return getFriendsOf(a).thenApply(list ->
      list.stream().anyMatch(f -> f.getUniqueId().equals(b))
    );
  }

  @Override
  public CompletableFuture<Void> loadFriends(UUID ownerId) {
    return repository
      .findAllByOwner(ownerId)
      .thenAccept(list -> list.forEach(f -> cacheFriend(ownerId, f)));
  }

  @Override
  public void cacheFriend(UUID ownerId, IFriend friend) {
    if (friend instanceof Friend f) {
      cache
        .computeIfAbsent(ownerId, k -> new ConcurrentHashMap<>())
        .put(f.getUniqueId(), f);
    }
  }

  @Override
  public void uncacheFriend(UUID ownerId, UUID friendId) {
    Map<UUID, Friend> ownerFriends = cache.get(ownerId);
    if (ownerFriends != null) ownerFriends.remove(friendId);
  }

  @Override
  public Optional<IFriend> getCachedFriend(UUID ownerId, UUID friendId) {
    return Optional.ofNullable(
      cache.getOrDefault(ownerId, Map.of()).get(friendId)
    ).map(f -> (IFriend) f);
  }

  @Override
  public CompletableFuture<Void> updateFriendStatus(
    UUID ownerId,
    UUID friendId,
    IFriend.Status status
  ) {
    Friend cached = cache.getOrDefault(ownerId, Map.of()).get(friendId);
    if (cached != null) {
      cached.setStatus(status);
      cacheFriend(ownerId, cached);
      return repository.save(ownerId, cached);
    }

    return repository
      .findById(ownerId, friendId)
      .thenCompose(opt -> {
        if (opt.isEmpty()) {
          return CompletableFuture.failedFuture(
            new IllegalStateException("Friend not found")
          );
        }
        Friend friend = opt.get();
        friend.setStatus(status);
        cacheFriend(ownerId, friend);
        return repository.save(ownerId, friend);
      });
  }

  private CompletableFuture<Void> updateFriendWithNameAndStatus(
    UUID ownerId,
    UUID friendId,
    String friendName,
    IFriend.Status status
  ) {
    Friend cached = cache.getOrDefault(ownerId, Map.of()).get(friendId);
    if (cached != null) {
      cached.setName(friendName);
      cached.setStatus(status);
      cacheFriend(ownerId, cached);
      return repository.save(ownerId, cached);
    }

    return repository
      .findById(ownerId, friendId)
      .thenCompose(opt -> {
        if (opt.isEmpty()) {
          return CompletableFuture.failedFuture(
            new IllegalStateException("Friend not found")
          );
        }
        Friend friend = opt.get();
        friend.setName(friendName);
        friend.setStatus(status);
        cacheFriend(ownerId, friend);
        return repository.save(ownerId, friend);
      });
  }

  @Override
  public CompletableFuture<Void> sendFriendRequest(UUID senderId, UUID receiverId, String receiverName) {
    Friend outgoingRequest = new Friend(
      senderId + ":" + receiverId,
      receiverName,
      IFriend.Status.PENDING
    );
    cacheFriend(senderId, outgoingRequest);

    Friend incomingRequest = new Friend(
      receiverId + ":" + senderId,
      "",
      IFriend.Status.PENDING
    );
    cacheFriend(receiverId, incomingRequest);

    return repository.save(senderId, outgoingRequest)
      .thenCompose(v -> repository.save(receiverId, incomingRequest));
  }

  @Override
  public CompletableFuture<Void> acceptFriendRequest(UUID receiverId, UUID senderId) {
    IProfileService profileService = profileServiceProvider.get();
    return profileService.getProfile(senderId)
      .thenCompose(senderProfileOpt -> {
        String senderName = senderProfileOpt.map(p -> p.getName()).orElse("");
        return profileService.getProfile(receiverId)
          .thenCompose(receiverProfileOpt -> {
            String receiverName = receiverProfileOpt.map(p -> p.getName()).orElse("");
            return updateFriendWithNameAndStatus(receiverId, senderId, senderName, IFriend.Status.ACCEPTED)
              .thenCompose(v -> updateFriendWithNameAndStatus(senderId, receiverId, receiverName, IFriend.Status.ACCEPTED));
          });
      });
  }

  @Override
  public CompletableFuture<Void> denyFriendRequest(UUID receiverId, UUID senderId) {
    uncacheFriend(receiverId, senderId);
    uncacheFriend(senderId, receiverId);
    return repository.delete(receiverId, senderId)
      .thenCompose(v -> repository.delete(senderId, receiverId));
  }

  @Override
  public CompletableFuture<Void> cancelFriendRequest(UUID senderId, UUID receiverId) {
    uncacheFriend(senderId, receiverId);
    uncacheFriend(receiverId, senderId);
    return repository.delete(senderId, receiverId)
      .thenCompose(v -> repository.delete(receiverId, senderId));
  }

  @Override
  public CompletableFuture<List<IFriend>> getIncomingRequests(UUID playerId) {
    return getFriendsOf(playerId).thenApply(friends ->
      friends.stream()
        .filter(f -> f.getStatus() == IFriend.Status.PENDING)
        .filter(f -> {
          if (f instanceof Friend friend) {
            return FriendRequestHelper.isIncomingRequest(friend, playerId);
          }
          return false;
        })
        .collect(Collectors.toList())
    );
  }

  @Override
  public CompletableFuture<List<IFriend>> getOutgoingRequests(UUID playerId) {
    return getFriendsOf(playerId).thenApply(friends ->
      friends.stream()
        .filter(f -> f.getStatus() == IFriend.Status.PENDING)
        .filter(f -> {
          if (f instanceof Friend friend) {
            return FriendRequestHelper.isOutgoingRequest(friend, playerId);
          }
          return false;
        })
        .collect(Collectors.toList())
    );
  }

  @Override
  public CompletableFuture<List<IFriend>> getAcceptedFriends(UUID playerId) {
    return getFriendsOf(playerId).thenApply(friends ->
      friends.stream()
        .filter(f -> f.getStatus() == IFriend.Status.ACCEPTED)
        .collect(Collectors.toList())
    );
  }

  @Override
  public void clearCache() {
    cache.clear();
  }

  private UUID extractOwnerFromComposite(UUID composite) {
    return composite;
  }

  private UUID extractFriendFromComposite(UUID composite) {
    return composite;
  }
}
