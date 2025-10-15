package dev.voltic.helektra.plugin.model.arena.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.google.inject.Inject;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.repository.IArenaJournalRepository;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;

public class ArenaBlockTrackingListener implements Listener {
    private final IArenaService arenaService;
    private final IArenaJournalRepository journalRepository;
    private final WorldGateway worldGateway;

    @Inject
    public ArenaBlockTrackingListener(IArenaService arenaService,
                                     IArenaJournalRepository journalRepository,
                                     WorldGateway worldGateway) {
        this.arenaService = arenaService;
        this.journalRepository = journalRepository;
        this.worldGateway = worldGateway;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        Arena arena = getArenaAt(loc);
        
        if (arena == null) return;

        String blockData = worldGateway.getBlockData(
            loc.getWorld().getName(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ()
        );

        journalRepository.recordBlockChange(
            arena.getId(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ(),
            blockData
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        Arena arena = getArenaAt(loc);
        
        if (arena == null) return;

        String blockData = worldGateway.getBlockData(
            loc.getWorld().getName(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ()
        );

        journalRepository.recordBlockChange(
            arena.getId(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ(),
            blockData
        );
    }

    private Arena getArenaAt(Location location) {
        return arenaService.getAllArenas().stream()
            .filter(arena -> arena.getRegion() != null)
            .filter(arena -> arena.getRegion().getWorld().equals(location.getWorld().getName()))
            .filter(arena -> arena.isInRegion(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
            .findFirst()
            .orElse(null);
    }
}
