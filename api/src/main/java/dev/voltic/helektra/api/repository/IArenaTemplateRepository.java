package dev.voltic.helektra.api.repository;

import dev.voltic.helektra.api.model.arena.ArenaTemplateBundle;
import dev.voltic.helektra.api.model.arena.ArenaTemplateMetadata;
import dev.voltic.helektra.api.model.arena.Region;
import java.util.concurrent.CompletableFuture;

public interface IArenaTemplateRepository {
    CompletableFuture<ArenaTemplateMetadata> saveTemplate(
        String arenaId,
        Region region
    );

    CompletableFuture<ArenaTemplateBundle> loadTemplate(String arenaId);

    CompletableFuture<Void> deleteTemplate(String arenaId);

    CompletableFuture<Boolean> exists(String arenaId);

    CompletableFuture<Long> getSize(String arenaId);
}
