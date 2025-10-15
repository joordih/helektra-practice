package dev.voltic.helektra.plugin.model.arena.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.google.inject.Inject;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.arena.IPhysicsGuardService;
import dev.voltic.helektra.plugin.model.arena.PhysicsGuardService;

public class ArenaProtectionListener implements Listener {
    private final IArenaService arenaService;
    private final PhysicsGuardService physicsGuard;

    @Inject
    public ArenaProtectionListener(IArenaService arenaService, IPhysicsGuardService physicsGuard) {
        this.arenaService = arenaService;
        this.physicsGuard = (PhysicsGuardService) physicsGuard;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Location loc = event.getBlock().getLocation();
        String world = loc.getWorld().getName();
        
        if (physicsGuard.isSuspendedAt(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFromTo(BlockFromToEvent event) {
        Location from = event.getBlock().getLocation();
        Location to = event.getToBlock().getLocation();
        String world = from.getWorld().getName();
        
        if (physicsGuard.isSuspendedAt(world, from.getBlockX(), from.getBlockY(), from.getBlockZ()) ||
            physicsGuard.isSuspendedAt(world, to.getBlockX(), to.getBlockY(), to.getBlockZ())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockGrow(BlockGrowEvent event) {
        Location loc = event.getBlock().getLocation();
        String world = loc.getWorld().getName();
        
        if (physicsGuard.isSuspendedAt(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockSpread(BlockSpreadEvent event) {
        Location loc = event.getBlock().getLocation();
        String world = loc.getWorld().getName();
        
        if (physicsGuard.isSuspendedAt(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        Location loc = event.getLocation();
        String world = loc.getWorld().getName();
        
        if (physicsGuard.isSuspendedAt(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
		private Arena getArenaAt(Location location) {
        return arenaService.getAllArenas().stream()
            .filter(arena -> arena.getRegion() != null)
            .filter(arena -> arena.getRegion().getWorld().equals(location.getWorld().getName()))
            .filter(arena -> arena.isInRegion(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
            .findFirst()
            .orElse(null);
    }
}
