package dev.voltic.helektra.plugin.model.arena.reset;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.*;
import dev.voltic.helektra.api.repository.IArenaJournalRepository;
import dev.voltic.helektra.plugin.model.arena.event.BukkitArenaResetCompletedEvent;
import dev.voltic.helektra.plugin.model.arena.event.BukkitArenaResetStartedEvent;
import dev.voltic.helektra.plugin.model.arena.reset.strategies.*;
import org.bukkit.Bukkit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@SuppressWarnings("unused")
public class ArenaResetService implements IArenaResetService {
  private final Map<UUID, ResetTask> activeTasks = new ConcurrentHashMap<>();
  private final IArenaService arenaService;
  private final IArenaJournalRepository journalRepository;
  private final IPhysicsGuardService physicsGuard;
  private final ISchedulerService schedulerService;
  private final IMetricsService metricsService;

  private final JournalResetStrategy journalStrategy;
  private final SectionResetStrategy sectionStrategy;
  private final ChunkSwapResetStrategy chunkSwapStrategy;
  private final HybridResetStrategy hybridStrategy;

  @Inject
  public ArenaResetService(IArenaService arenaService,
      IArenaJournalRepository journalRepository,
      IPhysicsGuardService physicsGuard,
      ISchedulerService schedulerService,
      IMetricsService metricsService,
      JournalResetStrategy journalStrategy,
      SectionResetStrategy sectionStrategy,
      ChunkSwapResetStrategy chunkSwapStrategy,
      HybridResetStrategy hybridStrategy) {
    this.arenaService = arenaService;
    this.journalRepository = journalRepository;
    this.physicsGuard = physicsGuard;
    this.schedulerService = schedulerService;
    this.metricsService = metricsService;
    this.journalStrategy = journalStrategy;
    this.sectionStrategy = sectionStrategy;
    this.chunkSwapStrategy = chunkSwapStrategy;
    this.hybridStrategy = hybridStrategy;
  }

  @Override
  public CompletableFuture<Void> resetInstance(ArenaInstance instance) {
    Arena arena = arenaService.getArena(instance.getArenaId()).orElse(null);
    if (arena == null) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("Arena not found: " + instance.getArenaId()));
    }

    long startTime = System.currentTimeMillis();
    instance.setResetStartedAt(Instant.now());

    ResetStrategy strategy = arena.getResetConfig().getStrategy();
    long estimatedBlocks = instance.getTotalBlocksModified();

    Bukkit.getPluginManager().callEvent(
        new BukkitArenaResetStartedEvent(arena.getId(), instance, strategy, estimatedBlocks));

    physicsGuard.suspend(instance.getInstanceRegion());

    ResetTask task = new ResetTask(instance, strategy, startTime);
    activeTasks.put(instance.getInstanceId(), task);

    CompletableFuture<Void> resetFuture = executeStrategy(strategy, instance, arena);

    return resetFuture
        .thenCompose(v -> journalRepository.clearJournal(instance.getInstanceId().toString()))
        .whenComplete((v, ex) -> {
          physicsGuard.resume(instance.getInstanceRegion());
          activeTasks.remove(instance.getInstanceId());

          long duration = System.currentTimeMillis() - startTime;
          instance.setLastResetDurationMs(duration);

          boolean success = ex == null;
          long blocksReset = instance.getTotalBlocksModified();
          instance.setTotalBlocksModified(0);

          Bukkit.getPluginManager().callEvent(
              new BukkitArenaResetCompletedEvent(arena.getId(), instance, strategy, duration, blocksReset, success));

          if (success) {
            metricsService.recordReset(arena.getId(), strategy, duration, blocksReset);
          }
        });
  }

  @Override
  public CompletableFuture<Void> resetChunk(ArenaInstance instance, int chunkX, int chunkZ) {
    Arena arena = arenaService.getArena(instance.getArenaId()).orElse(null);
    if (arena == null) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("Arena not found: " + instance.getArenaId()));
    }

    return chunkSwapStrategy.resetChunk(instance, arena, chunkX, chunkZ);
  }

  @Override
  public void cancelReset(ArenaInstance instance) {
    ResetTask task = activeTasks.remove(instance.getInstanceId());
    if (task != null) {
      task.cancelled = true;
    }
  }

  @Override
  public boolean isResetting(ArenaInstance instance) {
    return activeTasks.containsKey(instance.getInstanceId());
  }

  @Override
  public double getResetProgress(ArenaInstance instance) {
    ResetTask task = activeTasks.get(instance.getInstanceId());
    return task != null ? task.progress : 0.0;
  }

  private CompletableFuture<Void> executeStrategy(ResetStrategy strategy,
      ArenaInstance instance,
      Arena arena) {
    return switch (strategy) {
      case JOURNAL_ONLY -> journalStrategy.reset(instance, arena);
      case SECTION_REWRITE -> sectionStrategy.reset(instance, arena);
      case CHUNK_SWAP -> chunkSwapStrategy.reset(instance, arena);
      case HYBRID -> hybridStrategy.reset(instance, arena);
      case FULL_REGENERATE -> chunkSwapStrategy.reset(instance, arena);
    };
  }

  private static class ResetTask {
    final ArenaInstance instance;
    final ResetStrategy strategy;
    final long startTime;
    volatile double progress;
    volatile boolean cancelled;

    ResetTask(ArenaInstance instance, ResetStrategy strategy, long startTime) {
      this.instance = instance;
      this.strategy = strategy;
      this.startTime = startTime;
      this.progress = 0.0;
      this.cancelled = false;
    }
  }
}
