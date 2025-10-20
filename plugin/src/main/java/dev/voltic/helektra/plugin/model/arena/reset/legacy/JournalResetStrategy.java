package dev.voltic.helektra.plugin.model.arena.reset.legacy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ISchedulerService;
import dev.voltic.helektra.api.repository.IArenaJournalRepository;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
@Deprecated
public class JournalResetStrategy implements ResetStrategyBase {

    private final IArenaJournalRepository journalRepository;
    private final WorldGateway worldGateway;
    private final ISchedulerService schedulerService;

    @Inject
    public JournalResetStrategy(
        IArenaJournalRepository journalRepository,
        WorldGateway worldGateway,
        ISchedulerService schedulerService
    ) {
        this.journalRepository = journalRepository;
        this.worldGateway = worldGateway;
        this.schedulerService = schedulerService;
    }

    @Override
    public CompletableFuture<Void> reset(ArenaInstance instance, Arena arena) {
        String instanceId = instance.getInstanceId().toString();
        int minChunkX = instance.getInstanceRegion().getChunkMinX();
        int maxChunkX = instance.getInstanceRegion().getChunkMaxX();
        int minChunkZ = instance.getInstanceRegion().getChunkMinZ();
        int maxChunkZ = instance.getInstanceRegion().getChunkMaxZ();

        List<CompletableFuture<Void>> chunkFutures =
            new java.util.ArrayList<>();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                final int cx = chunkX;
                final int cz = chunkZ;

                CompletableFuture<Void> chunkFuture = journalRepository
                    .getChanges(instanceId, cx, cz)
                    .thenCompose(changes -> {
                        if (changes.isEmpty()) {
                            return CompletableFuture.completedFuture(null);
                        }

                        return schedulerService.runSync(() -> {
                            for (IArenaJournalRepository.BlockChange change : changes) {
                                worldGateway.revertBlock(
                                    instance.getInstanceRegion().getWorld(),
                                    change.getX(),
                                    change.getY(),
                                    change.getZ(),
                                    change.getBlockData()
                                );
                            }
                        });
                    });

                chunkFutures.add(chunkFuture);
            }
        }

        return CompletableFuture.allOf(
            chunkFutures.toArray(new CompletableFuture[0])
        );
    }
}
