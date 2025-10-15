package dev.voltic.helektra.api.model.arena;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IArenaPoolService {
    CompletableFuture<ArenaInstance> acquire(String arenaId);
    CompletableFuture<Void> release(ArenaInstance instance);
    int getAvailableCount(String arenaId);
    int getInUseCount(String arenaId);
    int getResettingCount(String arenaId);
    List<ArenaInstance> getAllInstances(String arenaId);
    void scalePool(String arenaId, int targetSize);
    void clearPool(String arenaId);
}
