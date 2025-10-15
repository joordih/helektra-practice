package dev.voltic.helektra.api.model.kit;

import java.util.List;
import java.util.Optional;

public interface IKitService {
    Optional<IKit> getKit(String name);
    void saveKit(IKit kit);
    void deleteKit(String name);
    List<IKit> getAllKits();
    List<String> getKitNames();
    List<IKit> getKitsByArena(String arenaId);
    void loadAll();
    void saveAll();
}
