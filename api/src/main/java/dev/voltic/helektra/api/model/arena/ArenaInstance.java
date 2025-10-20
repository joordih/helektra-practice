package dev.voltic.helektra.api.model.arena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArenaInstance {
    private UUID instanceId;
    private String arenaId;
    private ArenaInstanceState state;
    private Region instanceRegion;
    private Location instanceSpawnA;
    private Location instanceSpawnB;
    private Instant createdAt;
    private Instant lastUsedAt;
    private Instant resetStartedAt;
    private int usageCount;
    private long totalBlocksModified;
    private long lastResetDurationMs;

    public enum ArenaInstanceState {
        AVAILABLE,
        IN_USE,
        RESETTING,
        CORRUPTED
    }

    public boolean isAvailable() {
        return state == ArenaInstanceState.AVAILABLE;
    }

    public boolean needsReset() {
        return state == ArenaInstanceState.IN_USE;
    }
}
