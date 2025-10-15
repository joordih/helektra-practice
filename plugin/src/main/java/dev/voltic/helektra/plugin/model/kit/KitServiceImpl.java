package dev.voltic.helektra.plugin.model.kit;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.plugin.model.kit.repository.KitRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class KitServiceImpl implements IKitService {

    private final KitRepository repository;

    @Override
    public Optional<IKit> getKit(String name) {
        return repository.findByName(name)
            .map(kit -> (IKit) kit);
    }

    @Override
    public void saveKit(IKit kit) {
        if (!(kit instanceof Kit)) {
            throw new IllegalArgumentException("Kit must be an instance of Kit class");
        }
        
        Kit kitImpl = (Kit) kit;
        repository.save(kitImpl);
    }

    @Override
    public void deleteKit(String name) {
        repository.deleteByName(name);
    }

    @Override
    public List<IKit> getAllKits() {
        return repository.findAll().stream()
            .map(k -> (IKit) k)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getKitNames() {
        return repository.findAllNames();
    }

    @Override
    public List<IKit> getKitsByArena(String arenaId) {
        return repository.findByArenaId(arenaId).stream()
            .map(k -> (IKit) k)
            .collect(Collectors.toList());
    }

    @Override
    public void loadAll() {
        repository.loadAll();
    }

    @Override
    public void saveAll() {
        repository.saveAll(repository.findAll());
    }
    
    public Kit getKitDirect(String name) {
        return repository.findByName(name).orElse(null);
    }
}
