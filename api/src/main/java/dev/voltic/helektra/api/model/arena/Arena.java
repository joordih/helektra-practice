package dev.voltic.helektra.api.model.arena;

import dev.voltic.helektra.api.model.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Arena implements Model {
    private String id;
    private String name;
    private String displayName;
    private ArenaType type;
    
    @Builder.Default
    private boolean enabled = true;
    
    private Region region;
    private Location spawnA;
    private Location spawnB;
    
    @Builder.Default
    private int deathY = 0;
    
    @Builder.Default
    private int buildLimitHeight = 256;
    
    @Builder.Default
    private ArenaFlags flags = new ArenaFlags();
    
    @Builder.Default
    private PoolConfig poolConfig = new PoolConfig();
    
    @Builder.Default
    private ResetConfig resetConfig = new ResetConfig();

    public UUID getUniqueId() {
        return UUID.nameUUIDFromBytes(id.getBytes());
    }

    public boolean isInRegion(int x, int y, int z) {
        return region != null && region.contains(x, y, z);
    }

    public boolean isBelowDeathY(int y) {
        return y < deathY;
    }

    public boolean isAboveBuildLimit(int y) {
        return y > buildLimitHeight;
    }
}
