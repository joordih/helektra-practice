package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ArenaSelectionService implements IArenaSelectionService {
    private final Map<UUID, SelectionData> selections = new ConcurrentHashMap<>();

    @Override
    public void setPosition1(UUID playerId, Location location) {
        selections.computeIfAbsent(playerId, k -> new SelectionData()).pos1 = location;
    }

    @Override
    public void setPosition2(UUID playerId, Location location) {
        selections.computeIfAbsent(playerId, k -> new SelectionData()).pos2 = location;
    }

    @Override
    public void setSpawnA(UUID playerId, Location location) {
        selections.computeIfAbsent(playerId, k -> new SelectionData()).spawnA = location;
    }

    @Override
    public void setSpawnB(UUID playerId, Location location) {
        selections.computeIfAbsent(playerId, k -> new SelectionData()).spawnB = location;
    }

    @Override
    public Optional<Location> getPosition1(UUID playerId) {
        SelectionData data = selections.get(playerId);
        return data != null ? Optional.ofNullable(data.pos1) : Optional.empty();
    }

    @Override
    public Optional<Location> getPosition2(UUID playerId) {
        SelectionData data = selections.get(playerId);
        return data != null ? Optional.ofNullable(data.pos2) : Optional.empty();
    }

    @Override
    public Optional<Location> getSpawnA(UUID playerId) {
        SelectionData data = selections.get(playerId);
        return data != null ? Optional.ofNullable(data.spawnA) : Optional.empty();
    }

    @Override
    public Optional<Location> getSpawnB(UUID playerId) {
        SelectionData data = selections.get(playerId);
        return data != null ? Optional.ofNullable(data.spawnB) : Optional.empty();
    }

    @Override
    public Optional<Region> getSelectedRegion(UUID playerId) {
        SelectionData data = selections.get(playerId);
        if (data == null || data.pos1 == null || data.pos2 == null) {
            return Optional.empty();
        }

        if (!data.pos1.getWorld().equals(data.pos2.getWorld())) {
            return Optional.empty();
        }

        int minX = (int) Math.min(data.pos1.getX(), data.pos2.getX());
        int minY = (int) Math.min(data.pos1.getY(), data.pos2.getY());
        int minZ = (int) Math.min(data.pos1.getZ(), data.pos2.getZ());
        int maxX = (int) Math.max(data.pos1.getX(), data.pos2.getX());
        int maxY = (int) Math.max(data.pos1.getY(), data.pos2.getY());
        int maxZ = (int) Math.max(data.pos1.getZ(), data.pos2.getZ());

        return Optional.of(Region.builder()
            .world(data.pos1.getWorld())
            .minX(minX).minY(minY).minZ(minZ)
            .maxX(maxX).maxY(maxY).maxZ(maxZ)
            .build());
    }

    @Override
    public void clearSelection(UUID playerId) {
        selections.remove(playerId);
    }

    @Override
    public boolean hasCompleteSelection(UUID playerId) {
        SelectionData data = selections.get(playerId);
        return data != null && data.pos1 != null && data.pos2 != null 
            && data.spawnA != null && data.spawnB != null;
    }

    @Override
    public long estimateVolume(UUID playerId) {
        return getSelectedRegion(playerId)
            .map(Region::getVolume)
            .orElse(0L);
    }

    @Override
    public ValidationResult validateSelection(UUID playerId) {
        ValidationResult result = new ValidationResult();
        SelectionData data = selections.get(playerId);

        if (data == null) {
            result.addError("arena.validation.no-selection");
            return result;
        }

        if (data.pos1 == null || data.pos2 == null) {
            result.addError("arena.validation.incomplete-region");
        }

        if (data.spawnA == null || data.spawnB == null) {
            result.addError("arena.validation.incomplete-spawns");
        }

        Optional<Region> regionOpt = getSelectedRegion(playerId);
        if (regionOpt.isEmpty()) {
            return result;
        }

        Region region = regionOpt.get();
        long volume = region.getVolume();

        if (volume < 100) {
            result.addError("arena.validation.too-small");
        }

        if (volume > 10_000_000) {
            result.addError("arena.validation.too-large");
        }

        int height = region.getMaxY() - region.getMinY();
        if (height < 5) {
            result.addWarning("arena.validation.low-height");
        }

        if (data.spawnA != null && data.spawnB != null) {
            if (!region.contains((int) data.spawnA.getX(), (int) data.spawnA.getY(), (int) data.spawnA.getZ())) {
                result.addError("arena.validation.spawn-a-outside");
            }
            if (!region.contains((int) data.spawnB.getX(), (int) data.spawnB.getY(), (int) data.spawnB.getZ())) {
                result.addError("arena.validation.spawn-b-outside");
            }
        }

        return result;
    }

    private static class SelectionData {
        Location pos1;
        Location pos2;
        Location spawnA;
        Location spawnB;
    }
}
