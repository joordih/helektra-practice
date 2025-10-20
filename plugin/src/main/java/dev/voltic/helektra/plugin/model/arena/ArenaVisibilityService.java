package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ArenaVisibilitySettings;
import dev.voltic.helektra.api.model.arena.IArenaVisibilityService;
import dev.voltic.helektra.api.model.arena.Region;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
public class ArenaVisibilityService implements IArenaVisibilityService {

    private final ArenaVisibilitySettings settings;
    private final Map<UUID, UUID> playerAssignments = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> instancePlayers =
        new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> instanceEntities =
        new ConcurrentHashMap<>();
    private final VisibilityAdapter adapter;

    @Inject
    public ArenaVisibilityService(
        ArenaVisibilitySettings settings,
        JavaPlugin plugin
    ) {
        this.settings = settings;
        this.adapter = new VisibilityAdapter(plugin);
    }

    @Override
    public void assignPlayers(
        ArenaInstance instance,
        Collection<UUID> playerIds
    ) {
        if (instance == null || playerIds == null || playerIds.isEmpty()) {
            return;
        }
        UUID instanceId = instance.getInstanceId();
        instancePlayers
            .computeIfAbsent(instanceId, key -> new CopyOnWriteArraySet<>())
            .addAll(playerIds);
        for (UUID playerId : playerIds) {
            playerAssignments.put(playerId, instanceId);
        }
        updateVisibilityFor(
            Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> playerIds.contains(player.getUniqueId()))
                .collect(Collectors.toSet())
        );
        if (settings.isHideEntities()) {
            hideExternalEntities(instance);
        }
    }

    @Override
    public void releasePlayers(Collection<UUID> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) {
            return;
        }
        for (UUID playerId : playerIds) {
            UUID assignment = playerAssignments.remove(playerId);
            if (assignment != null) {
                Set<UUID> players = instancePlayers.get(assignment);
                if (players != null) {
                    players.remove(playerId);
                    if (players.isEmpty()) {
                        instancePlayers.remove(assignment);
                    }
                }
            }
        }
        updateVisibilityFor(
            Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> playerIds.contains(player.getUniqueId()))
                .collect(Collectors.toSet())
        );
    }

    @Override
    public void trackEntities(UUID instanceId, Collection<UUID> entityIds) {
        if (instanceId == null || entityIds == null || entityIds.isEmpty()) {
            return;
        }
        instanceEntities
            .computeIfAbsent(instanceId, key -> new CopyOnWriteArraySet<>())
            .addAll(entityIds);
    }

    @Override
    public void clearEntities(UUID instanceId) {
        if (instanceId == null) {
            return;
        }
        Set<UUID> tracked = instanceEntities.remove(instanceId);
        if (tracked == null || tracked.isEmpty()) {
            return;
        }
        for (UUID entityId : tracked) {
            Entity entity = findEntity(entityId);
            if (entity == null) {
                continue;
            }
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                adapter.showEntity(viewer, entity);
            }
        }
    }

    @Override
    public ArenaVisibilitySettings getSettings() {
        return settings;
    }

    private void hideExternalEntities(ArenaInstance instance) {
        Set<Player> players = getPlayers(instance.getInstanceId());
        if (players.isEmpty()) {
            return;
        }
        RegionBounds bounds = new RegionBounds(instance.getInstanceRegion());
        for (Player viewer : players) {
            if (!viewer.isOnline()) {
                continue;
            }
            for (Entity entity : viewer.getWorld().getEntities()) {
                if (entity instanceof Player) {
                    continue;
                }
                if (
                    !bounds.contains(
                        entity.getLocation().getBlockX(),
                        entity.getLocation().getBlockY(),
                        entity.getLocation().getBlockZ()
                    )
                ) {
                    adapter.hideEntity(viewer, entity);
                }
            }
        }
    }

    private void updateVisibilityFor(Set<Player> targets) {
        if (targets.isEmpty()) {
            return;
        }
        if (!settings.isHidePlayers()) {
            for (Player player : targets) {
                if (!player.isOnline()) {
                    continue;
                }
                showAllPlayers(player);
            }
            return;
        }
        Set<Player> online = new HashSet<>(Bukkit.getOnlinePlayers());
        for (Player viewer : targets) {
            if (!viewer.isOnline()) {
                continue;
            }
            UUID viewerAssignment = playerAssignments.get(viewer.getUniqueId());
            for (Player candidate : online) {
                if (!candidate.isOnline() || viewer.equals(candidate)) {
                    continue;
                }
                UUID candidateAssignment = playerAssignments.get(
                    candidate.getUniqueId()
                );
                if (
                    viewerAssignment != null &&
                    viewerAssignment.equals(candidateAssignment)
                ) {
                    adapter.showPlayer(viewer, candidate);
                    adapter.showPlayer(candidate, viewer);
                } else if (
                    viewerAssignment != null || candidateAssignment != null
                ) {
                    adapter.hidePlayer(viewer, candidate);
                    adapter.hidePlayer(candidate, viewer);
                } else {
                    adapter.showPlayer(viewer, candidate);
                    adapter.showPlayer(candidate, viewer);
                }
            }
        }
    }

    private void showAllPlayers(Player viewer) {
        for (Player candidate : Bukkit.getOnlinePlayers()) {
            if (candidate.isOnline() && !viewer.equals(candidate)) {
                adapter.showPlayer(viewer, candidate);
                adapter.showPlayer(candidate, viewer);
            }
        }
    }

    private Set<Player> getPlayers(UUID instanceId) {
        Set<UUID> ids = instancePlayers.get(instanceId);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return ids
            .stream()
            .map(Bukkit::getPlayer)
            .filter(player -> player != null && player.isOnline())
            .collect(Collectors.toSet());
    }

    private Entity findEntity(UUID entityId) {
        if (entityId == null) {
            return null;
        }
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            Entity entity = world
                .getEntities()
                .stream()
                .filter(e -> entityId.equals(e.getUniqueId()))
                .findFirst()
                .orElse(null);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private static final class RegionBounds {

        private final Region region;

        private RegionBounds(Region region) {
            this.region = region;
        }

        private boolean contains(int x, int y, int z) {
            return region.contains(x, y, z);
        }
    }

    private static final class VisibilityAdapter {

        private final JavaPlugin plugin;
        private final Method hidePlayerModern;
        private final Method showPlayerModern;
        private final Method hideEntityModern;
        private final Method showEntityModern;
        private final Method hidePlayerLegacy;
        private final Method showPlayerLegacy;

        private VisibilityAdapter(JavaPlugin plugin) {
            this.plugin = plugin;
            this.hidePlayerModern = resolveMethod(
                Player.class,
                "hidePlayer",
                JavaPlugin.class,
                Player.class
            );
            this.showPlayerModern = resolveMethod(
                Player.class,
                "showPlayer",
                JavaPlugin.class,
                Player.class
            );
            this.hideEntityModern = resolveMethod(
                Player.class,
                "hideEntity",
                JavaPlugin.class,
                Entity.class
            );
            this.showEntityModern = resolveMethod(
                Player.class,
                "showEntity",
                JavaPlugin.class,
                Entity.class
            );
            this.hidePlayerLegacy = resolveMethod(
                Player.class,
                "hidePlayer",
                Player.class
            );
            this.showPlayerLegacy = resolveMethod(
                Player.class,
                "showPlayer",
                Player.class
            );
        }

        private void hidePlayer(Player viewer, Player target) {
            if (viewer == null || target == null) {
                return;
            }
            if (hidePlayerModern != null) {
                invokeSilent(viewer, hidePlayerModern, plugin, target);
            } else if (hidePlayerLegacy != null) {
                invokeSilent(viewer, hidePlayerLegacy, target);
            }
        }

        private void showPlayer(Player viewer, Player target) {
            if (viewer == null || target == null) {
                return;
            }
            if (showPlayerModern != null) {
                invokeSilent(viewer, showPlayerModern, plugin, target);
            } else if (showPlayerLegacy != null) {
                invokeSilent(viewer, showPlayerLegacy, target);
            }
        }

        private void hideEntity(Player viewer, Entity entity) {
            if (viewer == null || entity == null) {
                return;
            }
            if (hideEntityModern != null) {
                invokeSilent(viewer, hideEntityModern, plugin, entity);
            }
        }

        private void showEntity(Player viewer, Entity entity) {
            if (viewer == null || entity == null) {
                return;
            }
            if (showEntityModern != null) {
                invokeSilent(viewer, showEntityModern, plugin, entity);
            }
        }

        private Method resolveMethod(
            Class<?> type,
            String name,
            Class<?>... parameters
        ) {
            try {
                Method method = type.getMethod(name, parameters);
                method.setAccessible(true);
                return method;
            } catch (Exception ignored) {
                return null;
            }
        }

        private void invokeSilent(
            Object target,
            Method method,
            Object... args
        ) {
            try {
                method.invoke(target, args);
            } catch (Exception ignored) {}
        }
    }
}
