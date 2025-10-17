package dev.voltic.helektra.plugin.model.profile.friend.repository;

import com.mongodb.client.MongoDatabase;
import dev.voltic.helektra.api.repository.ObjectRepository;
import dev.voltic.helektra.plugin.model.profile.friend.Friend;
import dev.voltic.helektra.plugin.repository.MongoObjectRepository;
import jakarta.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MongoFriendRepository implements FriendRepository {

  private final MongoDatabase database;
  private ObjectRepository<Friend> objectRepository;

  private ObjectRepository<Friend> getRepository() {
    if (objectRepository == null) {
      objectRepository = new MongoObjectRepository<>(database, Friend.class);
    }
    return objectRepository;
  }

  @Override
  public CompletableFuture<Optional<Friend>> findById(
    UUID ownerId,
    UUID friendId
  ) {
    return getRepository()
      .findAll()
      .thenApply(friends ->
        friends
          .stream()
          .filter(f -> f.getUniqueId().equals(friendId))
          .findFirst()
      );
  }

  @Override
  public CompletableFuture<List<Friend>> findAllByOwner(UUID ownerId) {
    return getRepository()
      .findAll()
      .thenApply(friends ->
        friends
          .stream()
          .filter(f -> f.getId().startsWith(ownerId.toString() + ":"))
          .toList()
      );
  }

  @Override
  public CompletableFuture<Void> save(UUID ownerId, Friend friend) {
    friend.setId(ownerId + ":" + friend.getUniqueId());
    return getRepository().save(friend);
  }

  @Override
  public CompletableFuture<Void> delete(UUID ownerId, UUID friendId) {
    String compositeId = ownerId + ":" + friendId;
    return getRepository().deleteById(compositeId);
  }
}
