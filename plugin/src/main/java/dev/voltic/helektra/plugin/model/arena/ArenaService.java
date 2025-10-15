package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.*;
import dev.voltic.helektra.api.repository.IArenaRepository;
import dev.voltic.helektra.plugin.utils.config.FileConfig;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class ArenaService implements IArenaService {
    private final Map<String, Arena> arenas = new ConcurrentHashMap<>();
    private final IArenaRepository repository;
    private final IArenaPoolService poolService;
    private final IMetricsService metricsService;
    
    @Getter
    private final FileConfig arenasConfig;

    @Inject
    public ArenaService(IArenaRepository repository, 
                       IArenaPoolService poolService, 
                       IMetricsService metricsService,
                       FileConfig arenasConfig) {
        this.repository = repository;
        this.poolService = poolService;
        this.metricsService = metricsService;
        this.arenasConfig = arenasConfig;
    }

    @Override
    public Optional<Arena> getArena(String id) {
        return Optional.ofNullable(arenas.get(id));
    }

    @Override
    public List<Arena> getAllArenas() {
        return new ArrayList<>(arenas.values());
    }

    @Override
    public List<Arena> getArenasByType(ArenaType type) {
        return arenas.values().stream()
                .filter(a -> a.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<Arena> getEnabledArenas() {
        return arenas.values().stream()
                .filter(Arena::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public void saveArena(Arena arena) {
        arenas.put(arena.getId(), arena);
        repository.save(arena);
        saveToConfig(arena);
    }

    @Override
    public void deleteArena(String id) {
        arenas.remove(id);
        repository.delete(id);
        poolService.clearPool(id);
        deleteFromConfig(id);
    }

    @Override
    public void loadAll() {
        arenas.clear();
        loadFromConfig();
        repository.findAll().forEach(arena -> arenas.put(arena.getId(), arena));
    }

    @Override
    public void saveAll() {
        arenas.values().forEach(this::saveArena);
    }

    @Override
    public void reloadArenas() {
      try {
          arenasConfig.getConfig().load(arenasConfig.getFile());
          loadAll();
      } catch (IOException | InvalidConfigurationException e) {
          e.printStackTrace();
      }
    }

    @Override
    public CompletableFuture<ArenaInstance> assignArena(String arenaId) {
        return poolService.acquire(arenaId);
    }

    @Override
    public CompletableFuture<Void> releaseArena(ArenaInstance instance) {
        return poolService.release(instance);
    }

    @Override
    public ArenaMetrics getMetrics(String arenaId) {
        return metricsService.getMetrics(arenaId);
    }

    private void loadFromConfig() {
        ConfigurationSection arenasSection = arenasConfig.getConfig().getConfigurationSection("arenas");
        if (arenasSection == null) return;

        for (String id : arenasSection.getKeys(false)) {
            ConfigurationSection arenaSection = arenasSection.getConfigurationSection(id);
            if (arenaSection == null) continue;

            Arena arena = parseArena(id, arenaSection);
            arenas.put(id, arena);
        }
    }

    private Arena parseArena(String id, ConfigurationSection section) {
        Region region = parseRegion(section.getConfigurationSection("region"));
        Location spawnA = parseLocation(section.getConfigurationSection("spawnA"));
        Location spawnB = parseLocation(section.getConfigurationSection("spawnB"));
        ArenaFlags flags = parseFlags(section.getConfigurationSection("flags"));
        PoolConfig poolConfig = parsePoolConfig(section.getConfigurationSection("pool"));
        ResetConfig resetConfig = parseResetConfig(section.getConfigurationSection("reset"));

        return Arena.builder()
                .id(id)
                .name(section.getString("name", id))
                .displayName(section.getString("displayName", id))
                .type(ArenaType.valueOf(section.getString("type", "CUSTOM").toUpperCase()))
                .enabled(section.getBoolean("enabled", true))
                .deathY(section.getInt("deathY", 0))
                .buildLimitHeight(section.getInt("buildLimitHeight", 256))
                .region(region)
                .spawnA(spawnA)
                .spawnB(spawnB)
                .flags(flags)
                .poolConfig(poolConfig)
                .resetConfig(resetConfig)
                .build();
    }

    private Region parseRegion(ConfigurationSection section) {
        if (section == null) return null;
        return Region.builder()
                .world(section.getString("world"))
                .minX(section.getInt("minX"))
                .minY(section.getInt("minY"))
                .minZ(section.getInt("minZ"))
                .maxX(section.getInt("maxX"))
                .maxY(section.getInt("maxY"))
                .maxZ(section.getInt("maxZ"))
                .build();
    }

    private Location parseLocation(ConfigurationSection section) {
        if (section == null) return null;
        return Location.builder()
                .world(section.getString("world"))
                .x(section.getDouble("x"))
                .y(section.getDouble("y"))
                .z(section.getDouble("z"))
                .yaw((float) section.getDouble("yaw", 0))
                .pitch((float) section.getDouble("pitch", 0))
                .build();
    }

    private ArenaFlags parseFlags(ConfigurationSection section) {
        if (section == null) return new ArenaFlags();
        return ArenaFlags.builder()
                .allowPlace(section.getBoolean("allowPlace", false))
                .allowBreak(section.getBoolean("allowBreak", false))
                .allowLiquids(section.getBoolean("allowLiquids", false))
                .allowProjectiles(section.getBoolean("allowProjectiles", true))
                .allowPvP(section.getBoolean("allowPvP", true))
                .allowHunger(section.getBoolean("allowHunger", true))
                .allowItemDrop(section.getBoolean("allowItemDrop", false))
                .allowItemPickup(section.getBoolean("allowItemPickup", false))
                .build();
    }

    private PoolConfig parsePoolConfig(ConfigurationSection section) {
        if (section == null) return new PoolConfig();
        return PoolConfig.builder()
                .preloaded(section.getInt("preloaded", 8))
                .minIdle(section.getInt("minIdle", 4))
                .maxActive(section.getInt("maxActive", 32))
                .autoScale(section.getBoolean("autoScale", true))
                .build();
    }

    private ResetConfig parseResetConfig(ConfigurationSection section) {
        if (section == null) return new ResetConfig();
        String strategyStr = section.getString("strategy", "HYBRID");
        ResetStrategy strategy;
        try {
            strategy = ResetStrategy.valueOf(strategyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            strategy = ResetStrategy.HYBRID;
        }

        return ResetConfig.builder()
                .strategy(strategy)
                .thresholdT1(section.getDouble("thresholdT1", 0.18))
                .thresholdT2(section.getDouble("thresholdT2", 0.55))
                .perTickBudget(section.getInt("perTickBudget", 10000))
                .async(section.getBoolean("async", true))
                .backoffTpsThreshold(section.getInt("backoffTpsThreshold", 195))
                .build();
    }

    private void saveToConfig(Arena arena) {
        String path = "arenas." + arena.getId();
        arenasConfig.getConfig().set(path + ".name", arena.getName());
        arenasConfig.getConfig().set(path + ".displayName", arena.getDisplayName());
        arenasConfig.getConfig().set(path + ".type", arena.getType().name());
        arenasConfig.getConfig().set(path + ".enabled", arena.isEnabled());
        arenasConfig.getConfig().set(path + ".deathY", arena.getDeathY());
        arenasConfig.getConfig().set(path + ".buildLimitHeight", arena.getBuildLimitHeight());

        if (arena.getRegion() != null) {
            saveRegion(path + ".region", arena.getRegion());
        }
        if (arena.getSpawnA() != null) {
            saveLocation(path + ".spawnA", arena.getSpawnA());
        }
        if (arena.getSpawnB() != null) {
            saveLocation(path + ".spawnB", arena.getSpawnB());
        }
        if (arena.getFlags() != null) {
            saveFlags(path + ".flags", arena.getFlags());
        }
        if (arena.getPoolConfig() != null) {
            savePoolConfig(path + ".pool", arena.getPoolConfig());
        }
        if (arena.getResetConfig() != null) {
            saveResetConfig(path + ".reset", arena.getResetConfig());
        }

        arenasConfig.save();
    }

    private void saveRegion(String path, Region region) {
        arenasConfig.getConfig().set(path + ".world", region.getWorld());
        arenasConfig.getConfig().set(path + ".minX", region.getMinX());
        arenasConfig.getConfig().set(path + ".minY", region.getMinY());
        arenasConfig.getConfig().set(path + ".minZ", region.getMinZ());
        arenasConfig.getConfig().set(path + ".maxX", region.getMaxX());
        arenasConfig.getConfig().set(path + ".maxY", region.getMaxY());
        arenasConfig.getConfig().set(path + ".maxZ", region.getMaxZ());
    }

    private void saveLocation(String path, Location location) {
        arenasConfig.getConfig().set(path + ".world", location.getWorld());
        arenasConfig.getConfig().set(path + ".x", location.getX());
        arenasConfig.getConfig().set(path + ".y", location.getY());
        arenasConfig.getConfig().set(path + ".z", location.getZ());
        arenasConfig.getConfig().set(path + ".yaw", location.getYaw());
        arenasConfig.getConfig().set(path + ".pitch", location.getPitch());
    }

    private void saveFlags(String path, ArenaFlags flags) {
        arenasConfig.getConfig().set(path + ".allowPlace", flags.isAllowPlace());
        arenasConfig.getConfig().set(path + ".allowBreak", flags.isAllowBreak());
        arenasConfig.getConfig().set(path + ".allowLiquids", flags.isAllowLiquids());
        arenasConfig.getConfig().set(path + ".allowProjectiles", flags.isAllowProjectiles());
        arenasConfig.getConfig().set(path + ".allowPvP", flags.isAllowPvP());
        arenasConfig.getConfig().set(path + ".allowHunger", flags.isAllowHunger());
        arenasConfig.getConfig().set(path + ".allowItemDrop", flags.isAllowItemDrop());
        arenasConfig.getConfig().set(path + ".allowItemPickup", flags.isAllowItemPickup());
    }

    private void savePoolConfig(String path, PoolConfig config) {
        arenasConfig.getConfig().set(path + ".preloaded", config.getPreloaded());
        arenasConfig.getConfig().set(path + ".minIdle", config.getMinIdle());
        arenasConfig.getConfig().set(path + ".maxActive", config.getMaxActive());
        arenasConfig.getConfig().set(path + ".autoScale", config.isAutoScale());
    }

    private void saveResetConfig(String path, ResetConfig config) {
        arenasConfig.getConfig().set(path + ".strategy", config.getStrategy().name());
        arenasConfig.getConfig().set(path + ".thresholdT1", config.getThresholdT1());
        arenasConfig.getConfig().set(path + ".thresholdT2", config.getThresholdT2());
        arenasConfig.getConfig().set(path + ".perTickBudget", config.getPerTickBudget());
        arenasConfig.getConfig().set(path + ".async", config.isAsync());
        arenasConfig.getConfig().set(path + ".backoffTpsThreshold", config.getBackoffTpsThreshold());
    }

    private void deleteFromConfig(String id) {
        arenasConfig.getConfig().set("arenas." + id, null);
        arenasConfig.save();
    }
}
