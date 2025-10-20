package dev.voltic.helektra.api.model.arena;

import java.util.concurrent.CompletableFuture;

public interface IArenaTemplateService {
    CompletableFuture<Void> createTemplate(Arena arena);
    CompletableFuture<ArenaInstance> cloneFromTemplate(String arenaId);
    CompletableFuture<Void> deleteTemplate(String arenaId);
    boolean hasTemplate(String arenaId);
    long getTemplateSize(String arenaId);
}
