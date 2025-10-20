package dev.voltic.helektra.plugin.model.arena.reset.legacy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.repository.IArenaTemplateRepository;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("deprecation")
@Singleton
public class SectionResetStrategy implements ResetStrategyBase {

    private final IArenaTemplateRepository templateRepository;
    private final WorldGateway worldGateway;

    @Inject
    public SectionResetStrategy(
        IArenaTemplateRepository templateRepository,
        WorldGateway worldGateway
    ) {
        this.templateRepository = templateRepository;
        this.worldGateway = worldGateway;
    }

    @Override
    public CompletableFuture<Void> reset(ArenaInstance instance, Arena arena) {
        return templateRepository
            .loadTemplate(arena.getId())
            .thenCompose(template ->
                worldGateway.applySectionData(
                    instance.getInstanceRegion().getWorld(),
                    instance.getInstanceRegion(),
                    template.getData()
                )
            );
    }
}
