package dev.voltic.helektra.plugin.repository;

import com.google.inject.Singleton;
import dev.voltic.helektra.api.repository.IArenaJournalRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class MemoryArenaJournalRepository implements IArenaJournalRepository {
    private final Map<String, List<BlockChangeImpl>> journals = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> recordBlockChange(String instanceId, int x, int y, int z, String blockData) {
        return CompletableFuture.runAsync(() -> {
            journals.computeIfAbsent(instanceId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new BlockChangeImpl(x, y, z, blockData, System.currentTimeMillis()));
        });
    }

    @Override
    public CompletableFuture<List<BlockChange>> getChanges(String instanceId, int chunkX, int chunkZ) {
        return CompletableFuture.supplyAsync(() -> {
            List<BlockChangeImpl> allChanges = journals.getOrDefault(instanceId, new ArrayList<>());
            
            int minX = chunkX << 4;
            int maxX = minX + 15;
            int minZ = chunkZ << 4;
            int maxZ = minZ + 15;
            
            return allChanges.stream()
                .filter(change -> change.x >= minX && change.x <= maxX 
                    && change.z >= minZ && change.z <= maxZ)
                .collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<Void> clearJournal(String instanceId) {
        return CompletableFuture.runAsync(() -> journals.remove(instanceId));
    }

    @Override
    public CompletableFuture<Integer> getChangeCount(String instanceId, int chunkX, int chunkZ) {
        return CompletableFuture.supplyAsync(() -> {
            List<BlockChangeImpl> allChanges = journals.getOrDefault(instanceId, new ArrayList<>());
            
            int minX = chunkX << 4;
            int maxX = minX + 15;
            int minZ = chunkZ << 4;
            int maxZ = minZ + 15;
            
            return (int) allChanges.stream()
                .filter(change -> change.x >= minX && change.x <= maxX 
                    && change.z >= minZ && change.z <= maxZ)
                .count();
        });
    }

    @Data
    @AllArgsConstructor
    private static class BlockChangeImpl implements BlockChange {
        private final int x;
        private final int y;
        private final int z;
        private final String blockData;
        private final long timestamp;
    }
}
