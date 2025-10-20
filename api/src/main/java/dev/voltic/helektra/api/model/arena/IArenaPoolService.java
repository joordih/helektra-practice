package dev.voltic.helektra.api.model.arena;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IArenaPoolService {
  CompletableFuture<ArenaInstance> acquire(String arenaId);
  CompletableFuture<Void> release(ArenaInstance instance);
  int getAvailableCount(String arenaId);
  int getInUseCount(String arenaId);
  int getResettingCount(String arenaId);
  List<ArenaInstance> getAllInstances(String arenaId);

  default CompletableFuture<Void> scalePool(String arenaId, int targetSize) {
    return scalePool(arenaId, targetSize, PoolScaleObserver.noop());
  }

  CompletableFuture<Void> scalePool(
    String arenaId,
    int targetSize,
    PoolScaleObserver observer
  );
  void clearPool(String arenaId);
}
