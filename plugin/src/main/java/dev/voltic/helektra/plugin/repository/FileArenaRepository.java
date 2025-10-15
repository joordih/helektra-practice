package dev.voltic.helektra.plugin.repository;

import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.repository.IArenaRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class FileArenaRepository implements IArenaRepository {
    private final Map<String, Arena> arenas = new ConcurrentHashMap<>();

    @Override
    public Optional<Arena> findById(String id) {
        return Optional.ofNullable(arenas.get(id));
    }

    @Override
    public List<Arena> findAll() {
        return new ArrayList<>(arenas.values());
    }

    @Override
    public void save(Arena arena) {
        arenas.put(arena.getId(), arena);
    }

    @Override
    public void delete(String id) {
        arenas.remove(id);
    }

    @Override
    public boolean exists(String id) {
        return arenas.containsKey(id);
    }
}
