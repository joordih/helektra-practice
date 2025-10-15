package dev.voltic.helektra.plugin.model.profile.repository;

import dev.voltic.helektra.plugin.model.profile.Profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ProfileRepository {
    CompletableFuture<Optional<Profile>> findById(UUID uuid);
    CompletableFuture<Optional<Profile>> findByName(String name);
    CompletableFuture<List<Profile>> findAll();
    CompletableFuture<Void> save(Profile profile);
    CompletableFuture<Void> deleteById(UUID uuid);
}
