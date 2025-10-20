package dev.voltic.helektra.api.model.arena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    private String world;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    public long getVolume() {
        return (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    public int getChunkMinX() {
        return minX >> 4;
    }

    public int getChunkMinZ() {
        return minZ >> 4;
    }

    public int getChunkMaxX() {
        return maxX >> 4;
    }

    public int getChunkMaxZ() {
        return maxZ >> 4;
    }
}
