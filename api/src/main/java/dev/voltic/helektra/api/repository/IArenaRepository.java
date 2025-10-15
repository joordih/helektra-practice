package dev.voltic.helektra.api.repository;

import dev.voltic.helektra.api.model.arena.Arena;

import java.util.List;
import java.util.Optional;

public interface IArenaRepository {
    Optional<Arena> findById(String id);
    List<Arena> findAll();
    void save(Arena arena);
    void delete(String id);
    boolean exists(String id);
}
