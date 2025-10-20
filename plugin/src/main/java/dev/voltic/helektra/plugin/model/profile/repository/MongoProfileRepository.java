package dev.voltic.helektra.plugin.model.profile.repository;

import com.mongodb.client.MongoDatabase;
import dev.voltic.helektra.api.repository.ObjectRepository;
import dev.voltic.helektra.plugin.model.profile.Profile;
import dev.voltic.helektra.plugin.repository.MongoObjectRepository;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MongoProfileRepository implements ProfileRepository {

    private final MongoDatabase database;
    private ObjectRepository<Profile> objectRepository;

    private ObjectRepository<Profile> getRepository() {
        if (objectRepository == null) {
            objectRepository = new MongoObjectRepository<>(database, Profile.class);
        }
        return objectRepository;
    }

    @Override
    public CompletableFuture<Optional<Profile>> findById(UUID uuid) {
        return getRepository().findById(uuid.toString());
    }

    @Override
    public CompletableFuture<Optional<Profile>> findByName(String name) {
        return getRepository().findAll()
            .thenApply(profiles -> profiles.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
            );
    }

    @Override
    public CompletableFuture<List<Profile>> findAll() {
        return getRepository().findAll();
    }

    @Override
    public CompletableFuture<Void> save(Profile profile) {
        profile.setId(profile.getUniqueId().toString());
        return getRepository().save(profile);
    }

    @Override
    public CompletableFuture<Void> deleteById(UUID uuid) {
        return getRepository().deleteById(uuid.toString());
    }
}
