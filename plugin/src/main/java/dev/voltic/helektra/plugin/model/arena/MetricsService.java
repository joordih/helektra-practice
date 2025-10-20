package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.ArenaMetrics;
import dev.voltic.helektra.api.model.arena.IMetricsService;
import dev.voltic.helektra.api.model.arena.ResetStrategy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class MetricsService implements IMetricsService {

    private final Map<String, ArenaMetrics> metrics = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> resetDurations =
        new ConcurrentHashMap<>();

    @Override
    public ArenaMetrics getMetrics(String arenaId) {
        return metrics.computeIfAbsent(arenaId, id ->
            ArenaMetrics.builder()
                .arenaId(id)
                .lastUpdated(Instant.now())
                .build()
        );
    }

    @Override
    public void recordMatch(String arenaId, long durationMs) {
        ArenaMetrics m = getMetrics(arenaId);
        m.setTotalMatches(m.getTotalMatches() + 1);

        long total =
            m.getAverageMatchDurationMs() * (m.getTotalMatches() - 1) +
            durationMs;
        m.setAverageMatchDurationMs(total / m.getTotalMatches());
        m.setLastUpdated(Instant.now());
    }

    @Override
    public void recordReset(
        String arenaId,
        ResetStrategy strategy,
        long durationMs,
        long blocksModified
    ) {
        ArenaMetrics m = getMetrics(arenaId);
        m.setTotalResets(m.getTotalResets() + 1);
        m.setTotalBlocksModified(m.getTotalBlocksModified() + blocksModified);

        long total =
            m.getAverageResetDurationMs() * (m.getTotalResets() - 1) +
            durationMs;
        m.setAverageResetDurationMs(total / m.getTotalResets());

        switch (strategy) {
            case JOURNAL_ONLY -> m.setJournalResets(m.getJournalResets() + 1);
            case SECTION_REWRITE -> m.setSectionResets(
                m.getSectionResets() + 1
            );
            case CHUNK_SWAP, FULL_REGENERATE -> m.setChunkSwapResets(
                m.getChunkSwapResets() + 1
            );
            case HYBRID -> {}
        }

        resetDurations
            .computeIfAbsent(arenaId, k -> new ArrayList<>())
            .add(durationMs);
        m.setP95ResetDurationMs(calculateP95(resetDurations.get(arenaId)));
        m.setLastUpdated(Instant.now());
    }

    @Override
    public void recordTps(String arenaId, double tpsBefore, double tpsAfter) {
        ArenaMetrics m = getMetrics(arenaId);
        m.setTpsBefore(tpsBefore);
        m.setTpsAfter(tpsAfter);
        m.setLastUpdated(Instant.now());
    }

    @Override
    public void recordQueueDepth(String arenaId, int depth) {
        ArenaMetrics m = getMetrics(arenaId);
        m.setCurrentQueueDepth(depth);
        m.setLastUpdated(Instant.now());
    }

    @Override
    public List<ArenaMetrics> getAllMetrics() {
        return new ArrayList<>(metrics.values());
    }

    @Override
    public void resetMetrics(String arenaId) {
        metrics.remove(arenaId);
        resetDurations.remove(arenaId);
    }

    private long calculateP95(List<Long> durations) {
        if (durations.isEmpty()) return 0;

        List<Long> sorted = new ArrayList<>(durations);
        Collections.sort(sorted);

        int index = (int) Math.ceil(sorted.size() * 0.95) - 1;
        return sorted.get(Math.max(0, index));
    }
}
