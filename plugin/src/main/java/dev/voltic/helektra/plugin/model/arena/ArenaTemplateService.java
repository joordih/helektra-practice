package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ArenaTemplateBundle;
import dev.voltic.helektra.api.model.arena.ArenaVisibilitySettings;
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
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;

@Singleton
@SuppressWarnings("deprecation")
public class ArenaTemplateService implements IArenaTemplateService {

  private final IArenaTemplateRepository templateRepository;
  private final IArenaService arenaService;
  private final WorldGateway worldGateway;
  private final IArenaSnapshotService snapshotService;
  private final ArenaVisibilitySettings visibilitySettings;
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
    IArenaSnapshotService snapshotService,
    ArenaVisibilitySettings visibilitySettings
  ) {
    this.templateRepository = templateRepository;
    this.arenaService = arenaService;
    this.worldGateway = worldGateway;
    this.snapshotService = snapshotService;
    this.visibilitySettings = visibilitySettings;
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
              "Rebuilding template for arena " + arena.getId() + ": " + message
            );
            return captureAndLoad(arena);
          }
          return CompletableFuture.completedFuture(bundle);
        })
        .thenCompose(result -> result)
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
      .thenCompose(metadata -> templateRepository.loadTemplate(arena.getId()));
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
    if (visibilitySettings.isMultiworldEnabled()) {
      return spawnInIsolatedWorld(arena, bundle, instanceId, baseRegion);
    }
    RegionAllocator allocator = allocators.computeIfAbsent(arena.getId(), key ->
      new RegionAllocator(baseRegion, visibilitySettings.getGapBlocks())
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

  private CompletableFuture<ArenaInstance> spawnInIsolatedWorld(
    Arena arena,
    ArenaTemplateBundle bundle,
    UUID instanceId,
    Region baseRegion
  ) {
    String sourceWorld = baseRegion.getWorld();
    String targetWorld = buildInstanceWorldName(arena.getId(), instanceId);
    prepareIsolatedWorld(sourceWorld, targetWorld);
    Region instanceRegion = Region.builder()
      .world(targetWorld)
      .minX(baseRegion.getMinX())
      .minY(baseRegion.getMinY())
      .minZ(baseRegion.getMinZ())
      .maxX(baseRegion.getMaxX())
      .maxY(baseRegion.getMaxY())
      .maxZ(baseRegion.getMaxZ())
      .build();
    Location instanceSpawnA = cloneLocation(arena.getSpawnA(), targetWorld);
    Location instanceSpawnB = cloneLocation(arena.getSpawnB(), targetWorld);
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

  private String buildInstanceWorldName(String arenaId, UUID instanceId) {
    return ("arena_" + arenaId + "_" + instanceId.toString().replace("-", ""));
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

  private Location cloneLocation(Location original, String worldName) {
    if (original == null) {
      return null;
    }
    return Location.builder()
      .world(worldName)
      .x(original.getX())
      .y(original.getY())
      .z(original.getZ())
      .yaw(original.getYaw())
      .pitch(original.getPitch())
      .build();
  }

  private void prepareIsolatedWorld(
    String sourceWorldName,
    String targetWorldName
  ) {
    if (targetWorldName == null || targetWorldName.isEmpty()) {
      throw new IllegalArgumentException("Target world name is required");
    }
    World existing = Bukkit.getWorld(targetWorldName);
    if (existing != null) {
      return;
    }
    World source = Bukkit.getWorld(sourceWorldName);
    if (source == null) {
      throw new IllegalArgumentException(
        "Source world not found: " + sourceWorldName
      );
    }
    WorldCreator creator = new WorldCreator(targetWorldName);
    creator.environment(source.getEnvironment());
    WorldType type = source.getWorldType();
    if (type != null) {
      creator.type(type);
    }
    creator.seed(source.getSeed());
    ChunkGenerator generator = source.getGenerator();
    if (generator != null) {
      creator.generator(generator);
    }
    World created = creator.createWorld();
    if (created != null) {
      created.setAutoSave(false);
    }
  }

  private static boolean intersects(Region a, Region b, int gap) {
    int expanded = Math.max(0, gap);
    return (
      a.getMaxX() + expanded >= b.getMinX() &&
      a.getMinX() - expanded <= b.getMaxX() &&
      a.getMaxY() + expanded >= b.getMinY() &&
      a.getMinY() - expanded <= b.getMaxY() &&
      a.getMaxZ() + expanded >= b.getMinZ() &&
      a.getMinZ() - expanded <= b.getMaxZ()
    );
  }

  private static final class RegionAllocator {

    private final Region base;
    private final int strideX;
    private final int strideZ;
    private final int gap;
    private final ConcurrentHashMap<UUID, Region> assignments =
      new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    RegionAllocator(Region base, int gap) {
      this.base = base;
      this.gap = Math.max(0, gap);
      int width = base.getMaxX() - base.getMinX() + 1;
      int depth = base.getMaxZ() - base.getMinZ() + 1;
      this.strideX = Math.max(1, width + this.gap);
      this.strideZ = Math.max(1, depth + this.gap);
    }

    Region allocate(UUID instanceId, CopyOnWriteArrayList<Region> occupied) {
      return assignments.computeIfAbsent(instanceId, key -> {
        while (true) {
          long index = sequence.getAndIncrement();
          GridPoint point = toSpiralPoint(index);
          Region candidate = translate(point);
          boolean overlaps = false;
          synchronized (occupied) {
            for (Region existing : occupied) {
              if (intersects(existing, candidate, gap)) {
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
      long layer = (long) Math.ceil((Math.sqrt(index) - 1) / 2);
      long legLength = 2 * layer + 1;
      long max = legLength * legLength;
      long leg = legLength - 1;
      if (index >= max - leg) {
        return new GridPoint((int) (layer - (max - index)), (int) -layer);
      }
      max -= leg;
      if (index >= max - leg) {
        return new GridPoint((int) -layer, (int) (-layer + (max - index)));
      }
      max -= leg;
      if (index >= max - leg) {
        return new GridPoint((int) (-layer + (max - index)), (int) layer);
      }
      return new GridPoint((int) layer, (int) (layer - (max - index - leg)));
    }
  }

  private record GridPoint(int x, int z) {}
}
