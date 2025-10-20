package dev.voltic.helektra.plugin.model.arena.reset;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.IArenaResetService;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.arena.IArenaSnapshotService;
import dev.voltic.helektra.api.model.arena.IMetricsService;
import dev.voltic.helektra.api.model.arena.IPhysicsGuardService;
import dev.voltic.helektra.api.model.arena.ResetStrategy;
import dev.voltic.helektra.plugin.model.arena.event.BukkitArenaResetCompletedEvent;
import dev.voltic.helektra.plugin.model.arena.event.BukkitArenaResetStartedEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;

@Singleton
public class ArenaResetService implements IArenaResetService {

    private final Map<UUID, ResetTask> activeTasks = new ConcurrentHashMap<>();
    private final IArenaService arenaService;
    private final IArenaSnapshotService snapshotService;
    private final IPhysicsGuardService physicsGuard;
    private final IMetricsService metricsService;

    @Inject
    public ArenaResetService(
        IArenaService arenaService,
        IArenaSnapshotService snapshotService,
        IPhysicsGuardService physicsGuard,
        IMetricsService metricsService
    ) {
        this.arenaService = arenaService;
        this.snapshotService = snapshotService;
        this.physicsGuard = physicsGuard;
        this.metricsService = metricsService;
    }

    @Override
    public CompletableFuture<Void> resetInstance(ArenaInstance instance) {
        Arena arena = arenaService.getArena(instance.getArenaId()).orElse(null);
        if (arena == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException(
                    "Arena not found: " + instance.getArenaId()
                )
            );
        }

        ResetStrategy strategy = arena.getResetConfig().getStrategy();
        long startTime = System.currentTimeMillis();
        long modifiedBlocks = snapshotService.getModifiedCount(
            instance.getInstanceId()
        );
        instance.setResetStartedAt(Instant.now());

        Bukkit.getPluginManager().callEvent(
            new BukkitArenaResetStartedEvent(
                arena.getId(),
                instance,
                strategy,
                modifiedBlocks
            )
        );

        physicsGuard.suspend(instance.getInstanceRegion());

        CompletableFuture<Void> rollback = snapshotService.rollback(
            instance.getInstanceId()
        );
        ResetTask task = new ResetTask(rollback);
        activeTasks.put(instance.getInstanceId(), task);

        return rollback.whenComplete((ignored, error) -> {
            physicsGuard.resume(instance.getInstanceRegion());
            activeTasks.remove(instance.getInstanceId());

            long duration = System.currentTimeMillis() - startTime;
            instance.setLastResetDurationMs(duration);
            boolean success = error == null;
            if (success) {
                instance.setTotalBlocksModified(0);
                metricsService.recordReset(
                    arena.getId(),
                    strategy,
                    duration,
                    modifiedBlocks
                );
            } else {
                snapshotService.markCorrupted(instance.getInstanceId(), error);
            }
            instance.setResetStartedAt(null);
            Bukkit.getPluginManager().callEvent(
                new BukkitArenaResetCompletedEvent(
                    arena.getId(),
                    instance,
                    strategy,
                    duration,
                    modifiedBlocks,
                    success
                )
            );
        });
    }

    @Override
    public CompletableFuture<Void> resetChunk(
        ArenaInstance instance,
        int chunkX,
        int chunkZ
    ) {
        return resetInstance(instance);
    }

    @Override
    public void cancelReset(ArenaInstance instance) {
        ResetTask task = activeTasks.remove(instance.getInstanceId());
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public boolean isResetting(ArenaInstance instance) {
        return activeTasks.containsKey(instance.getInstanceId());
    }

    @Override
    public double getResetProgress(ArenaInstance instance) {
        ResetTask task = activeTasks.get(instance.getInstanceId());
        if (task == null) {
            return 0.0;
        }
        return task.isCompleted() ? 1.0 : 0.0;
    }

    private static final class ResetTask {

        private final CompletableFuture<Void> future;

        ResetTask(CompletableFuture<Void> future) {
            this.future = future;
        }

        void cancel() {
            future.cancel(true);
        }

        boolean isCompleted() {
            return future.isDone();
        }
    }
}
