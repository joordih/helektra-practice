package dev.voltic.helektra.plugin.model.match.listeners;

import dev.voltic.helektra.api.model.arena.IArenaVisibilityService;
import dev.voltic.helektra.plugin.model.match.MatchArenaTracker;
import dev.voltic.helektra.plugin.model.match.SpectatorService;
import dev.voltic.helektra.plugin.model.match.event.MatchEndedEvent;
import dev.voltic.helektra.plugin.model.profile.LobbyService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

@Singleton
public class MatchEndListener implements Listener {

    private final LobbyService lobbyService;
    private final JavaPlugin plugin;
    private final MatchArenaTracker matchArenaTracker;
    private final IArenaVisibilityService visibilityService;
    private final SpectatorService spectatorService;
    private final Set<UUID> pendingTeleports = ConcurrentHashMap.newKeySet();

    @Inject
    public MatchEndListener(
        LobbyService lobbyService,
        JavaPlugin plugin,
        MatchArenaTracker matchArenaTracker,
        IArenaVisibilityService visibilityService,
        SpectatorService spectatorService
    ) {
        this.lobbyService = lobbyService;
        this.plugin = plugin;
        this.matchArenaTracker = matchArenaTracker;
        this.visibilityService = visibilityService;
        this.spectatorService = spectatorService;
    }

    @EventHandler
    public void onMatchEnded(MatchEndedEvent event) {
        matchArenaTracker.release(event.getMatch().getId());
        Set<UUID> released = ConcurrentHashMap.newKeySet();
        released.addAll(spectatorService.concludeMatch(event.getMatch()));
        event
            .getMatch()
            .getParticipants()
            .forEach(participant -> {
                Player player = Bukkit.getPlayer(participant.getUniqueId());
                if (player != null && player.isOnline()) {
                    scheduleTeleport(player);
                }
                released.add(participant.getUniqueId());
            });
        event
            .getMatch()
            .getSpectators()
            .forEach(uniqueId -> {
                Player spectator = Bukkit.getPlayer(uniqueId);
                if (spectator != null && spectator.isOnline()) {
                    scheduleTeleport(spectator);
                }
                released.add(uniqueId);
            });
        visibilityService.releasePlayers(released);
    }

    private void scheduleTeleport(Player player) {
        UUID uniqueId = player.getUniqueId();
        if (!pendingTeleports.add(uniqueId)) {
            return;
        }
        new BukkitRunnable() {
            private int attempts;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    pendingTeleports.remove(uniqueId);
                    cancel();
                    return;
                }
                if (player.isDead() || player.getHealth() <= 0) {
                    attempts++;
                    if (attempts >= 40) {
                        pendingTeleports.remove(uniqueId);
                        cancel();
                    }
                    return;
                }
                lobbyService.send(player);
                pendingTeleports.remove(uniqueId);
                cancel();
            }
        }
            .runTaskTimer(plugin, 0L, 5L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        pendingTeleports.remove(event.getPlayer().getUniqueId());
    }
}
