package dev.voltic.helektra.plugin.model.match;

import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.IArenaVisibilityService;
import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.LobbyService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.ArenaLocationResolver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Singleton
public class SpectatorService {

    private final MatchSettings settings;
    private final ProfileStateManager profileStateManager;
    private final LobbyService lobbyService;
    private final ArenaLocationResolver locationResolver;
    private final IArenaVisibilityService visibilityService;
    private final IMatchService matchService;
    private final Map<UUID, SpectatorSession> sessions =
        new ConcurrentHashMap<>();
    private final Map<String, Set<UUID>> spectatorsByMatch =
        new ConcurrentHashMap<>();

    @Inject
    public SpectatorService(
        MatchSettings settings,
        ProfileStateManager profileStateManager,
        LobbyService lobbyService,
        ArenaLocationResolver locationResolver,
        IArenaVisibilityService visibilityService,
        IMatchService matchService
    ) {
        this.settings = settings;
        this.profileStateManager = profileStateManager;
        this.lobbyService = lobbyService;
        this.locationResolver = locationResolver;
        this.visibilityService = visibilityService;
        this.matchService = matchService;
    }

    public boolean isEnabled() {
        return settings.getSpectator().isEnabled();
    }

    public boolean isSpectating(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    public void enterMatchSpectator(
        IMatch match,
        ArenaInstance instance,
        Player player,
        boolean temporary
    ) {
        if (match == null || player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        if (sessions.containsKey(playerId)) {
            return;
        }
        SpectatorSession session = new SpectatorSession(
            match.getMatchId(),
            player.getGameMode(),
            player.getAllowFlight(),
            temporary
        );
        sessions.put(playerId, session);
        spectatorsByMatch
            .computeIfAbsent(match.getMatchId(), key ->
                ConcurrentHashMap.newKeySet()
            )
            .add(playerId);
        if (!temporary) {
            match.addSpectator(playerId);
        }
        profileStateManager.setState(player, ProfileState.SPECTATOR);
        applySpectatorGamemode(player);
        teleportToArena(instance, player);
        if (instance != null) {
            visibilityService.assignPlayers(
                instance,
                Collections.singleton(playerId)
            );
        }
    }

    public Set<UUID> concludeMatch(IMatch match) {
        if (match == null) {
            return Collections.emptySet();
        }
        Set<UUID> tracked = spectatorsByMatch.remove(match.getMatchId());
        if (tracked == null || tracked.isEmpty()) {
            return Collections.emptySet();
        }
        Set<UUID> released = new HashSet<>();
        for (UUID spectatorId : tracked) {
            SpectatorSession session = sessions.remove(spectatorId);
            if (session == null) {
                continue;
            }
            Player spectator = Bukkit.getPlayer(spectatorId);
            if (spectator != null && spectator.isOnline()) {
                restoreGamemode(spectator, session);
                lobbyService.send(spectator);
            }
            if (!session.temporary()) {
                match.removeSpectator(spectatorId);
            }
            released.add(spectatorId);
        }
        return released;
    }

    public void leaveSpectator(Player player) {
        if (player == null) {
            return;
        }
        SpectatorSession session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }
        Set<UUID> tracked = spectatorsByMatch.get(session.matchId());
        if (tracked != null) {
            tracked.remove(player.getUniqueId());
            if (tracked.isEmpty()) {
                spectatorsByMatch.remove(session.matchId());
            }
        }
        Optional<IMatch> matchOpt = matchService.getMatch(session.matchId());
        matchOpt.ifPresent(match -> {
            if (!session.temporary()) {
                match.removeSpectator(player.getUniqueId());
            }
        });
        restoreGamemode(player, session);
        lobbyService.send(player);
        visibilityService.releasePlayers(
            Collections.singleton(player.getUniqueId())
        );
    }

    private void applySpectatorGamemode(Player player) {
        try {
            player.setGameMode(GameMode.SPECTATOR);
        } catch (Throwable ignored) {
            player.setAllowFlight(true);
            player.setFlying(true);
            try {
                player.setGameMode(GameMode.ADVENTURE);
            } catch (Throwable ignoredAgain) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    private void teleportToArena(ArenaInstance instance, Player player) {
        Location target = resolveLocation(instance);
        if (target != null) {
            player.teleport(target);
        }
    }

    private Location resolveLocation(ArenaInstance instance) {
        if (instance == null) {
            return null;
        }
        return locationResolver
            .resolve(instance.getInstanceSpawnA())
            .or(() -> locationResolver.resolve(instance.getInstanceSpawnB()))
            .map(location -> location.clone().add(0, 2, 0))
            .orElse(null);
    }

    private void restoreGamemode(Player player, SpectatorSession session) {
        if (player == null || session == null) {
            return;
        }
        GameMode previous = session.previousMode();
        if (previous != null) {
            try {
                player.setGameMode(previous);
            } catch (Throwable ignored) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
        player.setAllowFlight(session.allowFlight());
        if (!session.allowFlight()) {
            player.setFlying(false);
        }
    }

    private record SpectatorSession(
        String matchId,
        GameMode previousMode,
        boolean allowFlight,
        boolean temporary
    ) {}
}
