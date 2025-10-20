package dev.voltic.helektra.api.repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IArenaJournalRepository {
    CompletableFuture<Void> recordBlockChange(String instanceId, int x, int y, int z, String blockData);
    CompletableFuture<List<BlockChange>> getChanges(String instanceId, int chunkX, int chunkZ);
    CompletableFuture<Void> clearJournal(String instanceId);
    CompletableFuture<Integer> getChangeCount(String instanceId, int chunkX, int chunkZ);

    interface BlockChange {
        int getX();
        int getY();
        int getZ();
        String getBlockData();
        long getTimestamp();
    }
}
