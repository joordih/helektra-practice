package dev.voltic.helektra.plugin.model.arena.world;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.google.inject.Inject;

import dev.voltic.helektra.api.model.arena.Region;

public class WorldGateway {

    private static final int CHUNKS_PER_TICK = 2;
    private static final int BLOCKS_PER_TICK = 2048;

    private final JavaPlugin plugin;

    @Inject
    public WorldGateway(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public byte[] captureRegion(Region region) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("captureRegion must be called on the main thread");
        }

        World world = Bukkit.getWorld(region.getWorld());
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + region.getWorld());
        }

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        try (GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut);
             DataOutputStream dataOut = new DataOutputStream(gzipOut)) {

            for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
                for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                    for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        dataOut.writeUTF(block.getType().name());
                        dataOut.writeUTF(block.getBlockData().getAsString());
                    }
                }
            }

            dataOut.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to capture region", e);
        }

        return byteOut.toByteArray();
    }

    public CompletableFuture<Void> pasteRegion(Region region, byte[] templateData) {
        World world = Bukkit.getWorld(region.getWorld());
        if (world == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("World not found: " + region.getWorld()));
        }

        PasteSession session = new PasteSession(world, region, templateData);
        session.start();
        return session.getCompletionFuture();
    }

    public CompletableFuture<Void> applySectionData(String worldName, Region region, byte[] sectionData) {
        Region targetRegion = region;

        if (worldName != null && (region.getWorld() == null || !worldName.equals(region.getWorld()))) {
            targetRegion = Region.builder()
                .world(worldName)
                .minX(region.getMinX())
                .minY(region.getMinY())
                .minZ(region.getMinZ())
                .maxX(region.getMaxX())
                .maxY(region.getMaxY())
                .maxZ(region.getMaxZ())
                .build();
        }

        return pasteRegion(targetRegion, sectionData);
    }

    private final class PasteSession implements Runnable {
        private final World world;
        private final Region region;
        private final byte[] templateData;
        private final CompletableFuture<Void> completion = new CompletableFuture<>();
        private final Iterator<int[]> chunkIterator;

        private DataInputStream dataInput;
        private BukkitTask task;
        private boolean chunksLoaded;

        private final int minX;
        private final int minY;
        private final int minZ;
        private final int maxX;
        private final int maxY;
        private final int maxZ;
        private final long totalBlocks;

        private long processedBlocks;
        private int currentX;
        private int currentY;
        private int currentZ;

        PasteSession(World world, Region region, byte[] templateData) {
            this.world = world;
            this.region = region;
            this.templateData = templateData;

            List<int[]> chunks = new ArrayList<>();
            for (int chunkX = region.getChunkMinX(); chunkX <= region.getChunkMaxX(); chunkX++) {
                for (int chunkZ = region.getChunkMinZ(); chunkZ <= region.getChunkMaxZ(); chunkZ++) {
                    chunks.add(new int[] {chunkX, chunkZ});
                }
            }
            this.chunkIterator = chunks.iterator();

            this.minX = region.getMinX();
            this.minY = region.getMinY();
            this.minZ = region.getMinZ();
            this.maxX = region.getMaxX();
            this.maxY = region.getMaxY();
            this.maxZ = region.getMaxZ();
            this.totalBlocks = Math.max(0L, region.getVolume());

            this.currentX = minX;
            this.currentY = minY;
            this.currentZ = minZ;
        }

        CompletableFuture<Void> getCompletionFuture() {
            return completion;
        }

        void start() {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (completion.isDone()) {
                    return;
                }
                this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 1L, 1L);
            });
        }

        @Override
        public void run() {
            if (completion.isDone()) {
                cancelTask();
                return;
            }

            try {
                if (!chunksLoaded) {
                    loadChunksTick();
                } else {
                    processBlocksTick();
                }
            } catch (Throwable t) {
                fail(t);
            }
        }

        private void loadChunksTick() {
            int loadedThisTick = 0;

            while (chunkIterator.hasNext() && loadedThisTick < CHUNKS_PER_TICK) {
                int[] coords = chunkIterator.next();
                world.getChunkAt(coords[0], coords[1]);
                loadedThisTick++;
            }

            if (!chunkIterator.hasNext()) {
                chunksLoaded = true;
                try {
                    this.dataInput = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(templateData)));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to decode template data", e);
                }
            }
        }

        private void processBlocksTick() throws IOException {
            if (totalBlocks == 0) {
                complete();
                return;
            }

            int processedThisTick = 0;

            while (processedBlocks < totalBlocks && processedThisTick < BLOCKS_PER_TICK) {
                String materialName;
                String blockDataString;

                try {
                    materialName = dataInput.readUTF();
                    blockDataString = dataInput.readUTF();
                } catch (EOFException eof) {
                    complete();
                    return;
                }

                applyBlock(materialName, blockDataString);
                advancePosition();
                processedThisTick++;
            }

            if (processedBlocks >= totalBlocks) {
                complete();
            }
        }

        private void applyBlock(String materialName, String blockDataString) {
            Block block = world.getBlockAt(currentX, currentY, currentZ);
            Material material = Material.getMaterial(materialName);

            if (material == null) {
                block.setType(Material.AIR, false);
                return;
            }

            try {
                BlockData blockData = Bukkit.createBlockData(blockDataString);
                block.setBlockData(blockData, false);
            } catch (Exception e) {
                block.setType(material, false);
            }
        }

        private void advancePosition() {
            processedBlocks++;
            if (processedBlocks >= totalBlocks) {
                return;
            }

            currentZ++;
            if (currentZ > maxZ) {
                currentZ = minZ;
                currentY++;
                if (currentY > maxY) {
                    currentY = minY;
                    currentX++;
                }
            }
        }

        private void complete() {
            cleanup();
            completion.complete(null);
        }

        private void fail(Throwable t) {
            cleanup();
            completion.completeExceptionally(t);
        }

        private void cleanup() {
            if (task != null) {
                task.cancel();
            }

            if (dataInput != null) {
                try {
                    dataInput.close();
                } catch (IOException ignored) {
                    // no-op
                }
            }
        }

        private void cancelTask() {
            if (task != null) {
                task.cancel();
            }
        }
    }

    public void revertBlock(String worldName, int x, int y, int z, String blockDataString) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Block block = world.getBlockAt(x, y, z);

        try {
            BlockData blockData = Bukkit.createBlockData(blockDataString);
            block.setBlockData(blockData, false);
        } catch (Exception e) {
            Material material = resolveMaterial(blockDataString);
            if (material != null) {
                block.setType(material, false);
            } else {
                block.setType(Material.AIR, false);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void regenerateChunk(String worldName, int chunkX, int chunkZ) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        try {
            world.regenerateChunk(chunkX, chunkZ);
        } catch (Exception e) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                        Block block = chunk.getBlock(x, y, z);
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }
    }

    public String getBlockData(String worldName, int x, int y, int z) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return "minecraft:air";

        Block block = world.getBlockAt(x, y, z);
        return block.getBlockData().getAsString();
    }

    public void setBlock(String worldName, int x, int y, int z, String blockDataString) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Block block = world.getBlockAt(x, y, z);

        try {
            BlockData blockData = Bukkit.createBlockData(blockDataString);
            block.setBlockData(blockData, false);
        } catch (Exception e) {
            Material material = resolveMaterial(blockDataString);
            if (material != null) {
                block.setType(material, false);
            }
        }
    }

    private Material resolveMaterial(String value) {
        Material material = Material.matchMaterial(value);
        if (material == null) {
            material = Material.getMaterial(value.toUpperCase());
        }
        if (material == null && value.contains(":")) {
            String withoutNamespace = value.substring(value.indexOf(':') + 1);
            material = Material.matchMaterial(withoutNamespace);
            if (material == null) {
                material = Material.getMaterial(withoutNamespace.toUpperCase());
            }
        }
        return material;
    }

    public void clearEntities(Region region) {
        World world = Bukkit.getWorld(region.getWorld());
        if (world == null) return;

        world.getEntities().stream()
            .filter(entity -> {
                org.bukkit.Location loc = entity.getLocation();
                return region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            })
            .filter(entity -> !(entity instanceof org.bukkit.entity.Player))
            .forEach(org.bukkit.entity.Entity::remove);
    }
}
