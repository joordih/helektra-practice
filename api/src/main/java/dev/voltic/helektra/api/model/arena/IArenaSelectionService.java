package dev.voltic.helektra.api.model.arena;

import java.util.Optional;
import java.util.UUID;

public interface IArenaSelectionService {
    void setPosition1(UUID playerId, Location location);
    void setPosition2(UUID playerId, Location location);
    void setSpawnA(UUID playerId, Location location);
    void setSpawnB(UUID playerId, Location location);
    Optional<Location> getPosition1(UUID playerId);
    Optional<Location> getPosition2(UUID playerId);
    Optional<Location> getSpawnA(UUID playerId);
    Optional<Location> getSpawnB(UUID playerId);
    Optional<Region> getSelectedRegion(UUID playerId);
    void clearSelection(UUID playerId);
    boolean hasCompleteSelection(UUID playerId);
    long estimateVolume(UUID playerId);
    ValidationResult validateSelection(UUID playerId);
}
