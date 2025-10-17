package dev.voltic.helektra.plugin.model.profile;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.model.profile.friend.FriendServiceImpl;
import dev.voltic.helektra.plugin.model.profile.repository.ProfileRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProfileServiceImpl implements IProfileService {

  private final ProfileRepository repository;
  private final FriendServiceImpl friendService;
  private final Map<UUID, Profile> cache = new ConcurrentHashMap<>();

  @Override
  public CompletableFuture<Optional<IProfile>> getProfile(UUID uuid) {
    Profile cached = cache.get(uuid);
    if (cached != null) {
      return CompletableFuture.completedFuture(
        Optional.of(attachFriendService(cached))
      );
    }

    return repository
      .findById(uuid)
      .thenApply(optProfile ->
        optProfile.map(profile -> (IProfile) cacheProfileInternal(profile))
      );
  }

  @Override
  public CompletableFuture<Optional<IProfile>> getProfileByName(String name) {
    Optional<Profile> cached = cache
      .values()
      .stream()
      .filter(p -> p.getName().equalsIgnoreCase(name))
      .findFirst();

    if (cached.isPresent()) {
      return CompletableFuture.completedFuture(
        cached.map(this::attachFriendService)
      );
    }

    return repository
      .findByName(name)
      .thenApply(optProfile ->
        optProfile.map(profile -> (IProfile) cacheProfileInternal(profile))
      );
  }

  @Override
  public CompletableFuture<Void> saveProfile(IProfile profile) {
    if (!(profile instanceof Profile)) {
      return CompletableFuture.failedFuture(
        new IllegalArgumentException(
          "Profile must be an instance of Profile class"
        )
      );
    }

    Profile profileImpl = (Profile) profile;
    cacheProfileInternal(profileImpl);

    return repository.save(profileImpl);
  }

  @Override
  public CompletableFuture<Void> deleteProfile(UUID uuid) {
    cache.remove(uuid);
    return repository.deleteById(uuid);
  }

  @Override
  public CompletableFuture<List<IProfile>> getAllProfiles() {
    return repository
      .findAll()
      .thenApply(profiles ->
        profiles
          .stream()
          .map(this::cacheProfileInternal)
          .map(p -> (IProfile) p)
          .collect(Collectors.toList())
      );
  }

  @Override
  public CompletableFuture<Void> loadProfile(UUID uuid) {
    return repository
      .findById(uuid)
      .thenAccept(optProfile ->
        optProfile.ifPresent(profile -> cacheProfileInternal(profile))
      );
  }

  @Override
  public void cacheProfile(IProfile profile) {
    if (profile instanceof Profile profileImpl) {
      cacheProfileInternal(profileImpl);
    }
  }

  @Override
  public void uncacheProfile(UUID uuid) {
    cache.remove(uuid);
  }

  @Override
  public Optional<IProfile> getCachedProfile(UUID uuid) {
    return Optional.ofNullable(cache.get(uuid))
      .map(this::attachFriendService)
      .map(p -> (IProfile) p);
  }

  public Profile getProfileDirect(UUID uuid) {
    Profile profile = cache.get(uuid);
    return profile != null ? attachFriendService(profile) : null;
  }

  public void clearCache() {
    cache.clear();
  }

  private Profile cacheProfileInternal(Profile profile) {
    Profile attached = attachFriendService(profile);
    cache.put(attached.getUniqueId(), attached);
    return attached;
  }

  private Profile attachFriendService(Profile profile) {
    profile.setFriendService(friendService);
    return profile;
  }
}
