package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.IArenaPoolService;
import dev.voltic.helektra.api.model.arena.IArenaResetService;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.arena.IArenaSnapshotService;
import dev.voltic.helektra.api.model.arena.IArenaTemplateService;
import dev.voltic.helektra.api.model.arena.PoolScaleObserver;
import dev.voltic.helektra.plugin.model.arena.event.BukkitArenaInstanceAssignedEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;

@Singleton
@SuppressWarnings("unused")
public class ArenaPoolService implements IArenaPoolService {

    private final Map<String, ArenaPool> pools = new ConcurrentHashMap<>();
    private final IArenaTemplateService templateService;
    private final IArenaResetService resetService;
    private final IArenaService arenaService;
    private final IArenaSnapshotService snapshotService;

    @Inject
    public ArenaPoolService(
        IArenaTemplateService templateService,
        IArenaResetService resetService,
        IArenaService arenaService,
        IArenaSnapshotService snapshotService
    ) {
        this.templateService = templateService;
        this.resetService = resetService;
        this.arenaService = arenaService;
        this.snapshotService = snapshotService;
    }

    @Override
    public CompletableFuture<ArenaInstance> acquire(String arenaId) {
        long startTime = System.currentTimeMillis();

        return getOrCreatePool(arenaId).thenCompose(pool -> {
            ArenaInstance instance = pool.pollAvailable();

            if (instance != null) {
                instance.setState(ArenaInstance.ArenaInstanceState.IN_USE);
                instance.setLastUsedAt(Instant.now());
                instance.setUsageCount(instance.getUsageCount() + 1);

                long waitTime = System.currentTimeMillis() - startTime;
                Bukkit.getPluginManager().callEvent(
                    new BukkitArenaInstanceAssignedEvent(
                        arenaId,
                        instance,
                        null,
                        waitTime
                    )
                );

                return CompletableFuture.completedFuture(instance);
            }

            return templateService
                .cloneFromTemplate(arenaId)
                .thenApply(newInstance -> {
                    newInstance.setState(
                        ArenaInstance.ArenaInstanceState.IN_USE
                    );
                    newInstance.setLastUsedAt(Instant.now());
                    newInstance.setUsageCount(1);
                    pool.registerInUse(newInstance);

                    long waitTime = System.currentTimeMillis() - startTime;
                    Bukkit.getPluginManager().callEvent(
                        new BukkitArenaInstanceAssignedEvent(
                            arenaId,
                            newInstance,
                            null,
                            waitTime
                        )
                    );

                    return newInstance;
                });
        });
    }

    @Override
    public CompletableFuture<Void> release(ArenaInstance instance) {
        ArenaPool pool = pools.get(instance.getArenaId());
        if (pool == null) {
            return CompletableFuture.completedFuture(null);
        }

        pool.markResetting(instance);
        instance.setState(ArenaInstance.ArenaInstanceState.RESETTING);

        return resetService
            .resetInstance(instance)
            .thenAccept(v -> {
                instance.setState(ArenaInstance.ArenaInstanceState.AVAILABLE);
                pool.returnInstance(instance);
            })
            .exceptionally(ex -> {
                String message = ex.getMessage() != null
                    ? ex.getMessage()
                    : ex.toString();
                Bukkit.getLogger().severe(
                    "Failed to reset arena instance " +
                        instance.getInstanceId() +
                        " for arena " +
                        instance.getArenaId() +
                        ": " +
                        message
                );
                instance.setState(ArenaInstance.ArenaInstanceState.CORRUPTED);
                pool.markCorrupted(instance);
                snapshotService.unregister(instance.getInstanceId());
                return null;
            });
    }

    @Override
    public int getAvailableCount(String arenaId) {
        ArenaPool pool = pools.get(arenaId);
        return pool != null ? pool.getAvailableCount() : 0;
    }

    @Override
    public int getInUseCount(String arenaId) {
        ArenaPool pool = pools.get(arenaId);
        return pool != null ? pool.getInUseCount() : 0;
    }

    @Override
    public int getResettingCount(String arenaId) {
        ArenaPool pool = pools.get(arenaId);
        return pool != null ? pool.getResettingCount() : 0;
    }

    @Override
    public List<ArenaInstance> getAllInstances(String arenaId) {
        ArenaPool pool = pools.get(arenaId);
        return pool != null ? pool.getAllInstances() : Collections.emptyList();
    }

    @Override
    public CompletableFuture<Void> scalePool(
        String arenaId,
        int targetSize,
        PoolScaleObserver observer
    ) {
        PoolScaleObserver progressObserver = observer != null
            ? observer
            : PoolScaleObserver.noop();
        return getOrCreatePool(arenaId).thenCompose(pool -> {
            arenaService
                .getArena(arenaId)
                .ifPresent(arena -> {
                    if (arena.getPoolConfig().getPreloaded() != targetSize) {
                        arena.getPoolConfig().setPreloaded(targetSize);
                        arenaService.saveArena(arena);
                    }
                });
            int current = pool.getTotalCount();
            int difference = targetSize - current;
            if (difference == 0) {
                progressObserver.onProgress(arenaId, 1, 1);
                return CompletableFuture.completedFuture(null);
            }
            int totalOperations = Math.abs(difference);
            AtomicInteger completed = new AtomicInteger();
            progressObserver.onProgress(arenaId, 0, totalOperations);
            if (difference > 0) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (int i = 0; i < difference; i++) {
                    CompletableFuture<Void> future = templateService
                        .cloneFromTemplate(arenaId)
                        .thenAccept(instance -> {
                            pool.addInstance(instance);
                            progressObserver.onProgress(
                                arenaId,
                                completed.incrementAndGet(),
                                totalOperations
                            );
                        });
                    futures.add(future);
                }
                return CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
                );
            }
            for (int i = 0; i < totalOperations; i++) {
                ArenaInstance removed = pool.removeAvailableInstance();
                if (removed != null) {
                    snapshotService.unregister(removed.getInstanceId());
                    progressObserver.onProgress(
                        arenaId,
                        completed.incrementAndGet(),
                        totalOperations
                    );
                } else {
                    progressObserver.onProgress(
                        arenaId,
                        completed.get(),
                        totalOperations
                    );
                    break;
                }
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public void clearPool(String arenaId) {
        ArenaPool pool = pools.remove(arenaId);
        if (pool != null) {
            pool
                .getAllInstances()
                .forEach(instance ->
                    snapshotService.unregister(instance.getInstanceId())
                );
            pool.clear();
        }
    }

    private CompletableFuture<ArenaPool> getOrCreatePool(String arenaId) {
        if (pools.containsKey(arenaId)) {
            return CompletableFuture.completedFuture(pools.get(arenaId));
        }

        return arenaService
            .getArena(arenaId)
            .map(arena -> {
                ArenaPool pool = new ArenaPool();
                pools.put(arenaId, pool);

                int preloadCount = arena.getPoolConfig().getPreloaded();
                List<CompletableFuture<Void>> preloadFutures =
                    new ArrayList<>();

                for (int i = 0; i < preloadCount; i++) {
                    CompletableFuture<Void> future = templateService
                        .cloneFromTemplate(arenaId)
                        .thenAccept(pool::addInstance);
                    preloadFutures.add(future);
                }

                return CompletableFuture.allOf(
                    preloadFutures.toArray(new CompletableFuture[0])
                ).thenApply(v -> pool);
            })
            .orElse(
                CompletableFuture.failedFuture(
                    new IllegalArgumentException("Arena not found: " + arenaId)
                )
            );
    }

    private static class ArenaPool {

        private final Queue<ArenaInstance> available =
            new ConcurrentLinkedQueue<>();
        private final Set<ArenaInstance> inUse = ConcurrentHashMap.newKeySet();
        private final Set<ArenaInstance> resetting =
            ConcurrentHashMap.newKeySet();
        private final Set<ArenaInstance> all = ConcurrentHashMap.newKeySet();

        void addInstance(ArenaInstance instance) {
            all.add(instance);
            available.offer(instance);
        }

        void registerInUse(ArenaInstance instance) {
            all.add(instance);
            inUse.add(instance);
        }

        void returnInstance(ArenaInstance instance) {
            inUse.remove(instance);
            resetting.remove(instance);
            available.offer(instance);
        }

        void markResetting(ArenaInstance instance) {
            available.remove(instance);
            inUse.remove(instance);
            resetting.add(instance);
        }

        void markCorrupted(ArenaInstance instance) {
            available.remove(instance);
            inUse.remove(instance);
            resetting.remove(instance);
            all.remove(instance);
        }

        void removeInstance(ArenaInstance instance) {
            all.remove(instance);
            available.remove(instance);
            inUse.remove(instance);
            resetting.remove(instance);
        }

        ArenaInstance pollAvailable() {
            ArenaInstance instance = available.poll();
            if (instance != null) {
                inUse.add(instance);
            }
            return instance;
        }

        int getAvailableCount() {
            return available.size();
        }

        int getInUseCount() {
            return inUse.size();
        }

        int getResettingCount() {
            return resetting.size();
        }

        int getTotalCount() {
            return all.size();
        }

        List<ArenaInstance> getAllInstances() {
            return new ArrayList<>(all);
        }

        ArenaInstance removeAvailableInstance() {
            ArenaInstance instance = available.poll();
            if (instance == null) {
                return null;
            }
            removeInstance(instance);
            return instance;
        }

        void removeExcess(int count) {
            int removed = 0;
            while (removed < count && !available.isEmpty()) {
                ArenaInstance instance = available.poll();
                if (instance != null) {
                    removeInstance(instance);
                    removed++;
                }
            }
        }

        void clear() {
            available.clear();
            inUse.clear();
            resetting.clear();
            all.clear();
        }
    }
}
