package dev.voltic.helektra.api.model.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IProfileService {
    CompletableFuture<Optional<IProfile>> getProfile(UUID uuid);
    CompletableFuture<Optional<IProfile>> getProfileByName(String name);
    CompletableFuture<Void> saveProfile(IProfile profile);
    CompletableFuture<Void> deleteProfile(UUID uuid);
    CompletableFuture<List<IProfile>> getAllProfiles();
    CompletableFuture<Void> loadProfile(UUID uuid);
    void cacheProfile(IProfile profile);
    void uncacheProfile(UUID uuid);
    Optional<IProfile> getCachedProfile(UUID uuid);
}
