package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ArenaPoolSnapshot;
import dev.voltic.helektra.api.model.arena.IArenaPoolService;
import dev.voltic.helektra.api.model.arena.IArenaResetService;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.arena.IArenaSnapshotService;
import dev.voltic.helektra.api.model.arena.IArenaTemplateService;
import dev.voltic.helektra.api.model.arena.IMetricsService;
import dev.voltic.helektra.api.model.arena.PoolScaleObserver;
import dev.voltic.helektra.plugin.model.arena.event.BukkitArenaInstanceAssignedEvent;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.Bukkit;

@Singleton
public class ArenaPoolService implements IArenaPoolService {

    private final Map<String, ArenaPool> pools = new ConcurrentHashMap<>();
    private final IArenaTemplateService templateService;
    private final IArenaResetService resetService;
    private final IArenaService arenaService;
    private final IArenaSnapshotService snapshotService;
    private final IMetricsService metricsService;

    @Inject
    public ArenaPoolService(
        IArenaTemplateService templateService,
        IArenaResetService resetService,
        IArenaService arenaService,
        IArenaSnapshotService snapshotService,
        IMetricsService metricsService
    ) {
        this.templateService = templateService;
        this.resetService = resetService;
        this.arenaService = arenaService;
        this.snapshotService = snapshotService;
        this.metricsService = metricsService;
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
                        .thenAccept(pool::addInstanceAndNotify)
                        .thenRun(() ->
                            progressObserver.onProgress(
                                arenaId,
                                completed.incrementAndGet(),
                                totalOperations
                            )
                        );
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

    @Override
    public ArenaPoolSnapshot getSnapshot(String arenaId) {
        ArenaPool pool = pools.get(arenaId);
        int available = 0;
        int inUse = 0;
        int resetting = 0;
        int total = 0;
        if (pool != null) {
            PoolCounts counts = pool.counts();
            available = counts.available();
            inUse = counts.inUse();
            resetting = counts.resetting();
            total = counts.total();
        }
        double utilization = total == 0 ? 0.0 : inUse / (double) total;
        long averageReset = metricsService
            .getMetrics(arenaId)
            .getAverageResetDurationMs();
        return ArenaPoolSnapshot.builder()
            .arenaId(arenaId)
            .available(available)
            .inUse(inUse)
            .resetting(resetting)
            .utilization(utilization)
            .averageResetDurationMs(averageReset)
            .build();
    }

    private CompletableFuture<ArenaPool> getOrCreatePool(String arenaId) {
        ArenaPool existing = pools.get(arenaId);
        if (existing != null) {
            return CompletableFuture.completedFuture(existing);
        }
        ArenaPool created = new ArenaPool();
        ArenaPool previous = pools.putIfAbsent(arenaId, created);
        ArenaPool pool = previous != null ? previous : created;
        if (previous != null) {
            return CompletableFuture.completedFuture(pool);
        }
        return arenaService
            .getArena(arenaId)
            .map(arena -> {
                int preloadCount = arena.getPoolConfig().getPreloaded();
                List<CompletableFuture<Void>> preloadFutures =
                    new ArrayList<>();
                for (int i = 0; i < preloadCount; i++) {
                    CompletableFuture<Void> future = templateService
                        .cloneFromTemplate(arenaId)
                        .thenAccept(pool::addInstanceAndNotify);
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

    private static final class ArenaPool {

        private final ArrayDeque<ArenaInstance> available = new ArrayDeque<>();
        private final Set<ArenaInstance> inUse = ConcurrentHashMap.newKeySet();
        private final Set<ArenaInstance> resetting =
            ConcurrentHashMap.newKeySet();
        private final Set<ArenaInstance> all = ConcurrentHashMap.newKeySet();
        private final ReentrantLock lock = new ReentrantLock();

        ArenaInstance pollAvailable() {
            lock.lock();
            try {
                ArenaInstance instance = available.pollFirst();
                if (instance != null) {
                    inUse.add(instance);
                }
                return instance;
            } finally {
                lock.unlock();
            }
        }

        void addInstanceAndNotify(ArenaInstance instance) {
            lock.lock();
            try {
                all.add(instance);
                available.add(instance);
            } finally {
                lock.unlock();
            }
        }

        void registerInUse(ArenaInstance instance) {
            lock.lock();
            try {
                all.add(instance);
                inUse.add(instance);
            } finally {
                lock.unlock();
            }
        }

        void returnInstance(ArenaInstance instance) {
            lock.lock();
            try {
                resetting.remove(instance);
                inUse.remove(instance);
                all.add(instance);
                available.addLast(instance);
            } finally {
                lock.unlock();
            }
        }

        void markResetting(ArenaInstance instance) {
            lock.lock();
            try {
                available.remove(instance);
                if (inUse.remove(instance)) {
                    resetting.add(instance);
                }
            } finally {
                lock.unlock();
            }
        }

        void markCorrupted(ArenaInstance instance) {
            lock.lock();
            try {
                available.remove(instance);
                inUse.remove(instance);
                resetting.remove(instance);
                all.remove(instance);
            } finally {
                lock.unlock();
            }
        }

        ArenaInstance removeAvailableInstance() {
            lock.lock();
            try {
                ArenaInstance instance = available.pollFirst();
                if (instance != null) {
                    all.remove(instance);
                }
                return instance;
            } finally {
                lock.unlock();
            }
        }

        int getAvailableCount() {
            lock.lock();
            try {
                return available.size();
            } finally {
                lock.unlock();
            }
        }

        int getInUseCount() {
            lock.lock();
            try {
                return inUse.size();
            } finally {
                lock.unlock();
            }
        }

        int getResettingCount() {
            lock.lock();
            try {
                return resetting.size();
            } finally {
                lock.unlock();
            }
        }

        int getTotalCount() {
            lock.lock();
            try {
                return all.size();
            } finally {
                lock.unlock();
            }
        }

        List<ArenaInstance> getAllInstances() {
            lock.lock();
            try {
                return new ArrayList<>(all);
            } finally {
                lock.unlock();
            }
        }

        void clear() {
            lock.lock();
            try {
                available.clear();
                inUse.clear();
                resetting.clear();
                all.clear();
            } finally {
                lock.unlock();
            }
        }

        PoolCounts counts() {
            lock.lock();
            try {
                return new PoolCounts(
                    available.size(),
                    inUse.size(),
                    resetting.size(),
                    all.size()
                );
            } finally {
                lock.unlock();
            }
        }
    }

    private record PoolCounts(
        int available,
        int inUse,
        int resetting,
        int total
    ) {}
}
