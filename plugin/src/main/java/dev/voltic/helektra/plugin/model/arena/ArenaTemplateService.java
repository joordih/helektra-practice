package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ArenaTemplateBundle;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.arena.IArenaSnapshotService;
import dev.voltic.helektra.api.model.arena.IArenaTemplateService;
import dev.voltic.helektra.api.model.arena.Location;
import dev.voltic.helektra.api.model.arena.Region;
import dev.voltic.helektra.api.repository.IArenaTemplateRepository;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Bukkit;

@Singleton
public class ArenaTemplateService implements IArenaTemplateService {

    private static final int REGION_PADDING = 48;

    private final IArenaTemplateRepository templateRepository;
    private final IArenaService arenaService;
    private final WorldGateway worldGateway;
    private final IArenaSnapshotService snapshotService;
    private final ConcurrentHashMap<
        String,
        CompletableFuture<ArenaTemplateBundle>
    > templateCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RegionAllocator> allocators =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<
        String,
        CopyOnWriteArrayList<Region>
    > occupiedRegions = new ConcurrentHashMap<>();

    @Inject
    public ArenaTemplateService(
        IArenaTemplateRepository templateRepository,
        IArenaService arenaService,
        WorldGateway worldGateway,
        IArenaSnapshotService snapshotService
    ) {
        this.templateRepository = templateRepository;
        this.arenaService = arenaService;
        this.worldGateway = worldGateway;
        this.snapshotService = snapshotService;
    }

    @Override
    public CompletableFuture<Void> createTemplate(Arena arena) {
        if (arena == null || arena.getRegion() == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Arena region not configured")
            );
        }
        return templateRepository
            .saveTemplate(arena.getId(), arena.getRegion())
            .thenAccept(metadata -> templateCache.remove(arena.getId()));
    }

    @Override
    public CompletableFuture<ArenaInstance> cloneFromTemplate(String arenaId) {
        return arenaService
            .getArena(arenaId)
            .map(arena ->
                ensureTemplateBundle(arena).thenCompose(bundle ->
                    spawnInstance(arena, bundle).thenApply(instance -> {
                        snapshotService.registerContext(
                            arena,
                            instance,
                            bundle,
                            instance.getInstanceRegion()
                        );
                        return instance;
                    })
                )
            )
            .orElse(
                CompletableFuture.failedFuture(
                    new IllegalArgumentException("Arena not found: " + arenaId)
                )
            );
    }

    @Override
    public CompletableFuture<Void> deleteTemplate(String arenaId) {
        templateCache.remove(arenaId);
        allocators.remove(arenaId);
        return templateRepository.deleteTemplate(arenaId);
    }

    @Override
    public boolean hasTemplate(String arenaId) {
        return templateRepository.exists(arenaId).join();
    }

    @Override
    public long getTemplateSize(String arenaId) {
        return templateRepository.getSize(arenaId).join();
    }

    private CompletableFuture<ArenaTemplateBundle> ensureTemplateBundle(
        Arena arena
    ) {
        return templateCache.compute(arena.getId(), (key, existing) -> {
            if (existing != null) {
                return existing;
            }
            CompletableFuture<ArenaTemplateBundle> future = templateRepository
                .exists(arena.getId())
                .thenCompose(exists ->
                    exists
                        ? templateRepository.loadTemplate(arena.getId())
                        : captureAndLoad(arena)
                )
                .handle((bundle, error) -> {
                    if (error != null) {
                        String message = error.getMessage() != null
                            ? error.getMessage()
                            : error.toString();
                        Bukkit.getLogger().warning(
                            "Rebuilding template for arena " +
                                arena.getId() +
                                ": " +
                                message
                        );
                        return captureAndLoad(arena);
                    }
                    return CompletableFuture.completedFuture(bundle);
                })
                .thenCompose(futureResult -> futureResult)
                .whenComplete((bundle, error) -> {
                    if (error != null) {
                        templateCache.remove(arena.getId());
                    }
                });
            return future;
        });
    }

    private CompletableFuture<ArenaTemplateBundle> captureAndLoad(Arena arena) {
        if (arena.getRegion() == null) {
            return CompletableFuture.failedFuture(
                new IllegalStateException(
                    "Arena " + arena.getId() + " has no region defined"
                )
            );
        }
        return templateRepository
            .saveTemplate(arena.getId(), arena.getRegion())
            .thenCompose(metadata ->
                templateRepository.loadTemplate(arena.getId())
            );
    }

    private CompletableFuture<ArenaInstance> spawnInstance(
        Arena arena,
        ArenaTemplateBundle bundle
    ) {
        UUID instanceId = UUID.randomUUID();
        Region baseRegion = arena.getRegion();
        if (baseRegion == null) {
            return CompletableFuture.failedFuture(
                new IllegalStateException(
                    "Arena " + arena.getId() + " has no region defined"
                )
            );
        }
        RegionAllocator allocator = allocators.computeIfAbsent(
            arena.getId(),
            key -> new RegionAllocator(baseRegion, REGION_PADDING)
        );
        CopyOnWriteArrayList<Region> occupied = occupiedRegions.computeIfAbsent(
            baseRegion.getWorld(),
            key -> new CopyOnWriteArrayList<>()
        );
        Region instanceRegion = allocator.allocate(instanceId, occupied);
        Location instanceSpawnA = offsetLocation(
            arena.getSpawnA(),
            instanceRegion,
            baseRegion
        );
        Location instanceSpawnB = offsetLocation(
            arena.getSpawnB(),
            instanceRegion,
            baseRegion
        );
        ArenaInstance instance = ArenaInstance.builder()
            .instanceId(instanceId)
            .arenaId(arena.getId())
            .state(ArenaInstance.ArenaInstanceState.AVAILABLE)
            .instanceRegion(instanceRegion)
            .instanceSpawnA(instanceSpawnA)
            .instanceSpawnB(instanceSpawnB)
            .createdAt(Instant.now())
            .usageCount(0)
            .totalBlocksModified(0)
            .build();
        return worldGateway
            .pasteRegion(instanceRegion, bundle.getData())
            .thenApply(v -> instance);
    }

    private Location offsetLocation(
        Location original,
        Region instanceRegion,
        Region baseRegion
    ) {
        if (original == null) {
            return null;
        }
        int offsetX = instanceRegion.getMinX() - baseRegion.getMinX();
        int offsetZ = instanceRegion.getMinZ() - baseRegion.getMinZ();
        return Location.builder()
            .world(instanceRegion.getWorld())
            .x(original.getX() + offsetX)
            .y(original.getY())
            .z(original.getZ() + offsetZ)
            .yaw(original.getYaw())
            .pitch(original.getPitch())
            .build();
    }

    private static boolean intersects(Region a, Region b) {
        return (
            a.getMaxX() >= b.getMinX() &&
            a.getMinX() <= b.getMaxX() &&
            a.getMaxY() >= b.getMinY() &&
            a.getMinY() <= b.getMaxY() &&
            a.getMaxZ() >= b.getMinZ() &&
            a.getMinZ() <= b.getMaxZ()
        );
    }

    private static final class RegionAllocator {

        private final Region base;
        private final int strideX;
        private final int strideZ;
        private final ConcurrentHashMap<UUID, Region> assignments =
            new ConcurrentHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        RegionAllocator(Region base, int padding) {
            this.base = base;
            int width = base.getMaxX() - base.getMinX() + 1;
            int depth = base.getMaxZ() - base.getMinZ() + 1;
            this.strideX = Math.max(1, width + padding);
            this.strideZ = Math.max(1, depth + padding);
        }

        Region allocate(
            UUID instanceId,
            CopyOnWriteArrayList<Region> occupied
        ) {
            return assignments.computeIfAbsent(instanceId, key -> {
                while (true) {
                    long index = sequence.getAndIncrement();
                    GridPoint point = toSpiralPoint(index);
                    Region candidate = translate(point);
                    boolean overlaps = false;
                    synchronized (occupied) {
                        for (Region existing : occupied) {
                            if (intersects(existing, candidate)) {
                                overlaps = true;
                                break;
                            }
                        }
                        if (!overlaps) {
                            occupied.add(candidate);
                            return candidate;
                        }
                    }
                }
            });
        }

        private Region translate(GridPoint point) {
            int offsetX = point.x() * strideX;
            int offsetZ = point.z() * strideZ;
            return Region.builder()
                .world(base.getWorld())
                .minX(base.getMinX() + offsetX)
                .minY(base.getMinY())
                .minZ(base.getMinZ() + offsetZ)
                .maxX(base.getMaxX() + offsetX)
                .maxY(base.getMaxY())
                .maxZ(base.getMaxZ() + offsetZ)
                .build();
        }

        private GridPoint toSpiralPoint(long index) {
            if (index == 0) {
                return new GridPoint(0, 0);
            }
            long k = (long) Math.ceil((Math.sqrt(index) - 1) / 2);
            long t = 2 * k + 1;
            long m = t * t;
            long tMinus = t - 1;
            if (index >= m - tMinus) {
                return new GridPoint((int) (k - (m - index)), (int) -k);
            }
            m -= tMinus;
            if (index >= m - tMinus) {
                return new GridPoint((int) -k, (int) (-k + (m - index)));
            }
            m -= tMinus;
            if (index >= m - tMinus) {
                return new GridPoint((int) (-k + (m - index)), (int) k);
            }
            return new GridPoint((int) k, (int) (k - (m - index - tMinus)));
        }
    }

    private record GridPoint(int x, int z) {}
}
