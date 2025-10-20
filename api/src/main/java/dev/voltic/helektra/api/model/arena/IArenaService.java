package dev.voltic.helektra.api.model.arena;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IArenaService {
    Optional<Arena> getArena(String id);
    List<Arena> getAllArenas();
    List<Arena> getArenasByType(ArenaType type);
    List<Arena> getArenasByKit(String kitName);
    List<Arena> getEnabledArenas();
    void saveArena(Arena arena);
    void deleteArena(String id);
    void loadAll();
    void saveAll();
    void reloadArenas();
    CompletableFuture<ArenaInstance> assignArena(String arenaId);
    CompletableFuture<Void> releaseArena(ArenaInstance instance);
    ArenaMetrics getMetrics(String arenaId);
}
