package dev.voltic.helektra.plugin.model.kit.repository;

import dev.voltic.helektra.plugin.model.kit.Kit;

import java.util.List;
import java.util.Optional;

public interface KitRepository {
    Optional<Kit> findByName(String name);
    List<Kit> findAll();
    void save(Kit kit);
    void deleteByName(String name);
    List<String> findAllNames();
    List<Kit> findByArenaId(String arenaId);
    void saveAll(List<Kit> kits);
    void loadAll();
}
