package dev.voltic.helektra.api.model.arena;

import java.util.concurrent.CompletableFuture;

public interface IArenaResetService {
    CompletableFuture<Void> resetInstance(ArenaInstance instance);
    CompletableFuture<Void> resetChunk(ArenaInstance instance, int chunkX, int chunkZ);
    void cancelReset(ArenaInstance instance);
    boolean isResetting(ArenaInstance instance);
    double getResetProgress(ArenaInstance instance);
}
