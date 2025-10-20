package dev.voltic.helektra.api.model.arena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArenaMetrics {
    private String arenaId;
    private long totalMatches;
    private long totalResets;
    private long averageMatchDurationMs;
    private long averageResetDurationMs;
    private long p95ResetDurationMs;
    private long totalBlocksModified;
    private int journalResets;
    private int sectionResets;
    private int chunkSwapResets;
    private double tpsBefore;
    private double tpsAfter;
    private int currentQueueDepth;
    private Instant lastUpdated;
}
