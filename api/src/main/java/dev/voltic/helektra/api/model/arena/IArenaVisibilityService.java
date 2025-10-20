package dev.voltic.helektra.api.model.arena;

import java.util.Collection;
import java.util.UUID;

public interface IArenaVisibilityService {
  void assignPlayers(ArenaInstance instance, Collection<UUID> playerIds);

  void releasePlayers(Collection<UUID> playerIds);

  void trackEntities(UUID instanceId, Collection<UUID> entityIds);

  void clearEntities(UUID instanceId);

  ArenaVisibilitySettings getSettings();
}
