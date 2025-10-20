package dev.voltic.helektra.plugin.model.arena.snapshot;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ArenaInstanceContext;
import dev.voltic.helektra.api.model.arena.ArenaTemplateBundle;
import dev.voltic.helektra.api.model.arena.IArenaSnapshotService;
import dev.voltic.helektra.api.model.arena.ISchedulerService;
import dev.voltic.helektra.api.model.arena.Region;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class ArenaSnapshotService implements IArenaSnapshotService {

    private final ConcurrentHashMap<UUID, SnapshotState> snapshots =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<
        String,
        CopyOnWriteArrayList<SnapshotState>
    > worldIndex = new ConcurrentHashMap<>();
    private final WorldGateway worldGateway;
    private final ISchedulerService schedulerService;

    @Inject
    public ArenaSnapshotService(
        WorldGateway worldGateway,
        ISchedulerService schedulerService
    ) {
        this.worldGateway = worldGateway;
        this.schedulerService = schedulerService;
    }

    @Override
    public void registerContext(
        Arena arena,
        ArenaInstance instance,
        ArenaTemplateBundle template,
        Region instanceRegion
    ) {
        if (
            arena == null ||
            instance == null ||
            template == null ||
            instanceRegion == null ||
            instanceRegion.getWorld() == null
        ) {
            return;
        }
        ArenaInstanceContext context = ArenaInstanceContext.builder()
            .arenaId(arena.getId())
            .instanceId(instance.getInstanceId())
            .region(instanceRegion)
            .spawnA(instance.getInstanceSpawnA())
            .spawnB(instance.getInstanceSpawnB())
            .templateMetadata(template.getMetadata())
            .build();
        SnapshotState state = new SnapshotState(context, template, instance);
        snapshots.put(instance.getInstanceId(), state);
        worldIndex
            .computeIfAbsent(instanceRegion.getWorld(), key ->
                new CopyOnWriteArrayList<>()
            )
            .add(state);
    }

    @Override
    public Optional<ArenaInstanceContext> resolveContext(
        String worldName,
        int x,
        int y,
        int z
    ) {
        if (worldName == null) {
            return Optional.empty();
        }
        List<SnapshotState> states = worldIndex.get(worldName);
        if (states == null || states.isEmpty()) {
            return Optional.empty();
        }
        for (SnapshotState state : states) {
            if (state.contains(x, y, z)) {
                return Optional.of(state.context());
            }
        }
        return Optional.empty();
    }

    @Override
    public void recordPreChange(
        UUID instanceId,
        int x,
        int y,
        int z,
        String blockData
    ) {
        if (instanceId == null || blockData == null) {
            return;
        }
        SnapshotState state = snapshots.get(instanceId);
        if (state == null || state.corrupted()) {
            return;
        }
        state.record(x, y, z, blockData);
    }

    @Override
    public long getModifiedCount(UUID instanceId) {
        SnapshotState state = snapshots.get(instanceId);
        return state != null ? state.modifiedCount() : 0L;
    }

    @Override
    public CompletableFuture<Void> rollback(UUID instanceId) {
        SnapshotState state = snapshots.get(instanceId);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return state.rollback(worldGateway, schedulerService);
    }

    @Override
    public CompletableFuture<Void> rollback(ArenaInstanceContext context) {
        if (context == null) {
            return CompletableFuture.completedFuture(null);
        }
        return rollback(context.getInstanceId());
    }

    @Override
    public void markCorrupted(UUID instanceId, Throwable cause) {
        SnapshotState state = snapshots.get(instanceId);
        if (state == null) {
            return;
        }
        state.markCorrupted();
    }

    @Override
    public void unregister(UUID instanceId) {
        SnapshotState state = snapshots.remove(instanceId);
        if (state == null) {
            return;
        }
        String worldName = state.context().getRegion().getWorld();
        CopyOnWriteArrayList<SnapshotState> states = worldIndex.get(worldName);
        if (states != null) {
            states.remove(state);
            if (states.isEmpty()) {
                worldIndex.remove(worldName);
            }
        }
    }

    @Override
    public boolean isRegistered(UUID instanceId) {
        return snapshots.containsKey(instanceId);
    }

    private static long asKey(int x, int y, int z) {
        long key = ((long) (x & 0x3FFFFFF)) << 38;
        key |= ((long) (z & 0x3FFFFFF)) << 12;
        key |= (long) (y & 0xFFFL);
        return key;
    }

    private static final class SnapshotState {

        private final ArenaInstanceContext context;
        private final ArenaTemplateBundle template;
        private final ArenaInstance instance;
        private final ConcurrentHashMap<Long, String> capturedBlocks =
            new ConcurrentHashMap<>();
        private final AtomicLong modifiedCount = new AtomicLong();
        private volatile CompletableFuture<Void> activeReset;
        private volatile boolean corrupted;

        SnapshotState(
            ArenaInstanceContext context,
            ArenaTemplateBundle template,
            ArenaInstance instance
        ) {
            this.context = context;
            this.template = template;
            this.instance = instance;
        }

        ArenaInstanceContext context() {
            return context;
        }

        long modifiedCount() {
            return modifiedCount.get();
        }

        boolean corrupted() {
            return corrupted;
        }

        boolean contains(int x, int y, int z) {
            Region region = context.getRegion();
            return region != null && region.contains(x, y, z);
        }

        void record(int x, int y, int z, String blockData) {
            long key = asKey(x, y, z);
            String existing = capturedBlocks.putIfAbsent(key, blockData);
            if (existing == null) {
                long count = modifiedCount.incrementAndGet();
                instance.setTotalBlocksModified(count);
            }
        }

        void markCorrupted() {
            corrupted = true;
        }

        CompletableFuture<Void> rollback(
            WorldGateway worldGateway,
            ISchedulerService schedulerService
        ) {
            synchronized (this) {
                if (activeReset != null) {
                    return activeReset;
                }
                CompletableFuture<Void> pasteFuture = worldGateway.pasteRegion(
                    context.getRegion(),
                    template.getData()
                );
                CompletableFuture<Void> resetFuture = pasteFuture
                    .thenCompose(v ->
                        schedulerService.runSync(() -> {
                            worldGateway.clearEntities(context.getRegion());
                            worldGateway.clearLiquids(context.getRegion());
                        })
                    )
                    .thenRun(this::clear)
                    .whenComplete((v, ex) -> {
                        if (ex != null) {
                            corrupted = true;
                        }
                        synchronized (this) {
                            activeReset = null;
                        }
                    });
                activeReset = resetFuture;
                return resetFuture;
            }
        }

        private void clear() {
            capturedBlocks.clear();
            modifiedCount.set(0L);
            corrupted = false;
            instance.setTotalBlocksModified(0);
        }
    }
}
