package dev.voltic.helektra.api.model.arena;

import java.util.List;

public interface IMetricsService {
    ArenaMetrics getMetrics(String arenaId);
    void recordMatch(String arenaId, long durationMs);
    void recordReset(String arenaId, ResetStrategy strategy, long durationMs, long blocksModified);
    void recordTps(String arenaId, double tpsBefore, double tpsAfter);
    void recordQueueDepth(String arenaId, int depth);
    List<ArenaMetrics> getAllMetrics();
    void resetMetrics(String arenaId);
}
