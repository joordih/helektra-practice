package dev.voltic.helektra.plugin.model.profile;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.model.profile.repository.ProfileRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProfileServiceImpl implements IProfileService {

  private final ProfileRepository repository;
  private final Map<UUID, Profile> cache = new ConcurrentHashMap<>();

  @Override
  public CompletableFuture<Optional<IProfile>> getProfile(UUID uuid) {
    if (cache.containsKey(uuid)) {
      return CompletableFuture.completedFuture(Optional.of(cache.get(uuid)));
    }

    return repository.findById(uuid)
        .thenApply(optProfile -> {
          optProfile.ifPresent(profile -> cache.put(uuid, profile));
          return optProfile.map(p -> (IProfile) p);
        });
  }

  @Override
  public CompletableFuture<Optional<IProfile>> getProfileByName(String name) {
    Optional<Profile> cached = cache.values().stream()
        .filter(p -> p.getName().equalsIgnoreCase(name))
        .findFirst();

    if (cached.isPresent()) {
      return CompletableFuture.completedFuture(cached.map(p -> (IProfile) p));
    }

    return repository.findByName(name)
        .thenApply(optProfile -> {
          optProfile.ifPresent(profile -> cache.put(profile.getUniqueId(), profile));
          return optProfile.map(p -> (IProfile) p);
        });
  }

  @Override
  public CompletableFuture<Void> saveProfile(IProfile profile) {
    if (!(profile instanceof Profile)) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("Profile must be an instance of Profile class"));
    }

    Profile profileImpl = (Profile) profile;
    cache.put(profileImpl.getUniqueId(), profileImpl);

    return repository.save(profileImpl);
  }

  @Override
  public CompletableFuture<Void> deleteProfile(UUID uuid) {
    cache.remove(uuid);
    return repository.deleteById(uuid);
  }

  @Override
  public CompletableFuture<List<IProfile>> getAllProfiles() {
    return repository.findAll()
        .thenApply(profiles -> profiles.stream()
            .map(p -> (IProfile) p)
            .collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<Void> loadProfile(UUID uuid) {
    return repository.findById(uuid)
        .thenAccept(optProfile -> optProfile.ifPresent(profile -> cache.put(uuid, profile)));
  }

  @Override
  public void cacheProfile(IProfile profile) {
    if (profile instanceof Profile profileImpl) {
      cache.put(profileImpl.getUniqueId(), profileImpl);
    }
  }

  @Override
  public void uncacheProfile(UUID uuid) {
    cache.remove(uuid);
  }

  @Override
  public Optional<IProfile> getCachedProfile(UUID uuid) {
    return Optional.ofNullable(cache.get(uuid)).map(p -> (IProfile) p);
  }

  public Profile getProfileDirect(UUID uuid) {
    return cache.get(uuid);
  }

  public void clearCache() {
    cache.clear();
  }
}
