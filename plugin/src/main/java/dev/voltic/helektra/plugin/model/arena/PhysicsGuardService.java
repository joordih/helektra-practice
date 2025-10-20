package dev.voltic.helektra.plugin.model.arena;

import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.IPhysicsGuardService;
import dev.voltic.helektra.api.model.arena.Region;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class PhysicsGuardService implements IPhysicsGuardService {
    private final Set<Region> suspendedRegions = ConcurrentHashMap.newKeySet();

    @Override
    public void suspend(Region region) {
        suspendedRegions.add(region);
    }

    @Override
    public void resume(Region region) {
        suspendedRegions.remove(region);
    }

    @Override
    public boolean isSuspended(Region region) {
        return suspendedRegions.contains(region);
    }

    @Override
    public void suspendAll() {
    }

    @Override
    public void resumeAll() {
        suspendedRegions.clear();
    }

    public boolean isSuspendedAt(String world, int x, int y, int z) {
        return suspendedRegions.stream()
            .anyMatch(region -> region.getWorld().equals(world) && region.contains(x, y, z));
    }
}
