package dev.voltic.helektra.plugin.model.profile.friend.repository;

import dev.voltic.helektra.plugin.model.profile.friend.Friend;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FriendRepository {
  CompletableFuture<Optional<Friend>> findById(UUID ownerId, UUID friendId);

  CompletableFuture<List<Friend>> findAllByOwner(UUID ownerId);

  CompletableFuture<Void> save(UUID ownerId, Friend friend);

  CompletableFuture<Void> delete(UUID ownerId, UUID friendId);
}
