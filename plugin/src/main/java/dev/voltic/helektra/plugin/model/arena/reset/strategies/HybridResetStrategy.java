package dev.voltic.helektra.plugin.model.arena.reset.strategies;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.Region;
import dev.voltic.helektra.api.repository.IArenaJournalRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class HybridResetStrategy implements ResetStrategyBase {
    private final IArenaJournalRepository journalRepository;
    private final JournalResetStrategy journalStrategy;
    private final SectionResetStrategy sectionStrategy;
    private final ChunkSwapResetStrategy chunkSwapStrategy;

    @Inject
    public HybridResetStrategy(IArenaJournalRepository journalRepository,
                              JournalResetStrategy journalStrategy,
                              SectionResetStrategy sectionStrategy,
                              ChunkSwapResetStrategy chunkSwapStrategy) {
        this.journalRepository = journalRepository;
        this.journalStrategy = journalStrategy;
        this.sectionStrategy = sectionStrategy;
        this.chunkSwapStrategy = chunkSwapStrategy;
    }

    @Override
    public CompletableFuture<Void> reset(ArenaInstance instance, Arena arena) {
        Region region = instance.getInstanceRegion();
        String instanceId = instance.getInstanceId().toString();
        
        double t1 = arena.getResetConfig().getThresholdT1();
        double t2 = arena.getResetConfig().getThresholdT2();
        
        List<CompletableFuture<Void>> chunkFutures = new ArrayList<>();

        for (int chunkX = region.getChunkMinX(); chunkX <= region.getChunkMaxX(); chunkX++) {
            for (int chunkZ = region.getChunkMinZ(); chunkZ <= region.getChunkMaxZ(); chunkZ++) {
                final int cx = chunkX;
                final int cz = chunkZ;

                CompletableFuture<Void> chunkFuture = journalRepository.getChangeCount(instanceId, cx, cz)
                    .thenCompose(changeCount -> {
                        int chunkBlockCount = 16 * 16 * 16;
                        double modifiedRatio = (double) changeCount / chunkBlockCount;

                        if (modifiedRatio < t1) {
                            return journalRepository.getChanges(instanceId, cx, cz)
                                .thenCompose(changes -> {
                                    if (changes.isEmpty()) {
                                        return CompletableFuture.completedFuture(null);
                                    }
                                    return journalStrategy.reset(instance, arena);
                                });
                        } else if (modifiedRatio < t2) {
                            return sectionStrategy.reset(instance, arena);
                        } else {
                            return chunkSwapStrategy.resetChunk(instance, arena, cx, cz);
                        }
                    });

                chunkFutures.add(chunkFuture);
            }
        }

        return CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0]));
    }
}
