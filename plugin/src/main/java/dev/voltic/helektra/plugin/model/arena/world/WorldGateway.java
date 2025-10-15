package dev.voltic.helektra.plugin.model.arena.world;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import dev.voltic.helektra.api.model.arena.Region;

public class WorldGateway {
    
    public byte[] captureRegion(Region region) {
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

    public void pasteRegion(Region region, byte[] templateData) {
        World world = Bukkit.getWorld(region.getWorld());
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + region.getWorld());
        }

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(templateData);
             GZIPInputStream gzipIn = new GZIPInputStream(byteIn);
             DataInputStream dataIn = new DataInputStream(gzipIn)) {
            
            for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
                for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                    for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                        String materialName = dataIn.readUTF();
                        String blockDataString = dataIn.readUTF();
                        
                        Block block = world.getBlockAt(x, y, z);
                        Material material = Material.getMaterial(materialName);
                        
                        if (material != null) {
                            try {
                                BlockData blockData = Bukkit.createBlockData(blockDataString);
                                block.setBlockData(blockData);
                            } catch (Exception e) {
                                block.setType(material);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to paste region", e);
        }
    }

    public void applySectionData(String worldName, Region region, byte[] sectionData) {
        pasteRegion(region, sectionData);
    }

    public void revertBlock(String worldName, int x, int y, int z, String blockDataString) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Block block = world.getBlockAt(x, y, z);
        
        try {
            BlockData blockData = Bukkit.createBlockData(blockDataString);
            block.setBlockData(blockData);
        } catch (Exception e) {
            String[] parts = blockDataString.split(":");
            Material material = Material.getMaterial(parts[0]);
            if (material != null) {
                block.setType(material);
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
                        block.setType(Material.AIR);
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
            block.setBlockData(blockData);
        } catch (Exception e) {
            Material material = Material.getMaterial(blockDataString.toUpperCase());
            if (material != null) {
                block.setType(material);
            }
        }
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
