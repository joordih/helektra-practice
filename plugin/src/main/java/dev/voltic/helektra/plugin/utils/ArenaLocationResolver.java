package dev.voltic.helektra.plugin.utils;

import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Location;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.World;

@Singleton
public class ArenaLocationResolver {
    public Optional<org.bukkit.Location> resolve(Location location) {
        if (location == null || location.getWorld() == null) {
            return Optional.empty();
        }
        World world = Bukkit.getWorld(location.getWorld());
        if (world == null) {
            return Optional.empty();
        }
        return Optional.of(new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()));
    }
}
