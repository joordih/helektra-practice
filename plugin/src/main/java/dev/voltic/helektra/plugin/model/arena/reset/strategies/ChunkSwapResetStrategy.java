package dev.voltic.helektra.plugin.model.arena.reset.strategies;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ISchedulerService;
import dev.voltic.helektra.api.model.arena.Region;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class ChunkSwapResetStrategy implements ResetStrategyBase {
    private final WorldGateway worldGateway;
    private final ISchedulerService schedulerService;

    @Inject
    public ChunkSwapResetStrategy(WorldGateway worldGateway,
                                 ISchedulerService schedulerService) {
        this.worldGateway = worldGateway;
        this.schedulerService = schedulerService;
    }

    @Override
    public CompletableFuture<Void> reset(ArenaInstance instance, Arena arena) {
        Region region = instance.getInstanceRegion();
        List<CompletableFuture<Void>> chunkFutures = new ArrayList<>();

        for (int chunkX = region.getChunkMinX(); chunkX <= region.getChunkMaxX(); chunkX++) {
            for (int chunkZ = region.getChunkMinZ(); chunkZ <= region.getChunkMaxZ(); chunkZ++) {
                final int cx = chunkX;
                final int cz = chunkZ;
                
                CompletableFuture<Void> future = resetChunk(instance, arena, cx, cz);
                chunkFutures.add(future);
            }
        }

        return CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0]));
    }

    public CompletableFuture<Void> resetChunk(ArenaInstance instance, Arena arena, int chunkX, int chunkZ) {
        return schedulerService.runSync(() -> {
            worldGateway.regenerateChunk(
                instance.getInstanceRegion().getWorld(),
                chunkX,
                chunkZ
            );
        });
    }
}
