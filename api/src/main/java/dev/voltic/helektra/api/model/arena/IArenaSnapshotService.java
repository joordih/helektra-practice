package dev.voltic.helektra.api.model.arena;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IArenaSnapshotService {
  void registerContext(
    Arena arena,
    ArenaInstance instance,
    ArenaTemplateBundle template,
    Region instanceRegion
  );

  Optional<ArenaInstanceContext> resolveContext(
    String worldName,
    int x,
    int y,
    int z
  );

  void recordPreChange(UUID instanceId, int x, int y, int z, String blockData);

  long getModifiedCount(UUID instanceId);

  CompletableFuture<Void> rollback(UUID instanceId);

  CompletableFuture<Void> rollback(ArenaInstanceContext context);

  void markCorrupted(UUID instanceId, Throwable cause);

  void unregister(UUID instanceId);

  boolean isRegistered(UUID instanceId);
}
