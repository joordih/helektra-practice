package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.*;
import dev.voltic.helektra.api.repository.IArenaTemplateRepository;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class ArenaTemplateService implements IArenaTemplateService {
    private final IArenaTemplateRepository templateRepository;
    private final IArenaService arenaService;
    private final WorldGateway worldGateway;
    private final ISchedulerService schedulerService;

    @Inject
    public ArenaTemplateService(IArenaTemplateRepository templateRepository,
                               IArenaService arenaService,
                               WorldGateway worldGateway,
                               ISchedulerService schedulerService) {
        this.templateRepository = templateRepository;
        this.arenaService = arenaService;
        this.worldGateway = worldGateway;
        this.schedulerService = schedulerService;
    }

    @Override
    public CompletableFuture<Void> createTemplate(Arena arena) {
        return schedulerService.runAsync(() -> {
            templateRepository.saveTemplate(arena.getId(), arena.getRegion()).join();
        });
    }

    @Override
    public CompletableFuture<ArenaInstance> cloneFromTemplate(String arenaId) {
        return arenaService.getArena(arenaId)
            .map(arena -> {
                UUID instanceId = UUID.randomUUID();
                int offsetX = calculateOffset(arena, instanceId);
                int offsetZ = calculateOffset(arena, instanceId);

                Region instanceRegion = Region.builder()
                    .world(arena.getRegion().getWorld())
                    .minX(arena.getRegion().getMinX() + offsetX)
                    .minY(arena.getRegion().getMinY())
                    .minZ(arena.getRegion().getMinZ() + offsetZ)
                    .maxX(arena.getRegion().getMaxX() + offsetX)
                    .maxY(arena.getRegion().getMaxY())
                    .maxZ(arena.getRegion().getMaxZ() + offsetZ)
                    .build();

                Location instanceSpawnA = offsetLocation(arena.getSpawnA(), offsetX, offsetZ);
                Location instanceSpawnB = offsetLocation(arena.getSpawnB(), offsetX, offsetZ);

                return templateRepository.loadTemplate(arenaId)
                    .thenCompose(templateData -> worldGateway.pasteRegion(instanceRegion, templateData))
                    .thenApply(v -> ArenaInstance.builder()
                        .instanceId(instanceId)
                        .arenaId(arenaId)
                        .state(ArenaInstance.ArenaInstanceState.AVAILABLE)
                        .instanceRegion(instanceRegion)
                        .instanceSpawnA(instanceSpawnA)
                        .instanceSpawnB(instanceSpawnB)
                        .createdAt(Instant.now())
                        .usageCount(0)
                        .totalBlocksModified(0)
                        .build()
                    );
            })
            .orElse(CompletableFuture.failedFuture(
                new IllegalArgumentException("Arena not found: " + arenaId)
            ));
    }

    @Override
    public CompletableFuture<Void> deleteTemplate(String arenaId) {
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

    private int calculateOffset(Arena arena, UUID instanceId) {
        int width = arena.getRegion().getMaxX() - arena.getRegion().getMinX() + 1;
        int padding = 100;
        int instanceIndex = Math.abs(instanceId.hashCode() % 1000);
        return (width + padding) * instanceIndex;
    }

    private Location offsetLocation(Location original, int offsetX, int offsetZ) {
        if (original == null) return null;
        return Location.builder()
            .world(original.getWorld())
            .x(original.getX() + offsetX)
            .y(original.getY())
            .z(original.getZ() + offsetZ)
            .yaw(original.getYaw())
            .pitch(original.getPitch())
            .build();
    }
}
