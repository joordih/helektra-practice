package dev.voltic.helektra.plugin.model.arena.reset.strategies;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.ISchedulerService;
import dev.voltic.helektra.api.repository.IArenaTemplateRepository;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;

import java.util.concurrent.CompletableFuture;

@Singleton
public class SectionResetStrategy implements ResetStrategyBase {
    private final IArenaTemplateRepository templateRepository;
    private final WorldGateway worldGateway;
    private final ISchedulerService schedulerService;

    @Inject
    public SectionResetStrategy(IArenaTemplateRepository templateRepository,
                               WorldGateway worldGateway,
                               ISchedulerService schedulerService) {
        this.templateRepository = templateRepository;
        this.worldGateway = worldGateway;
        this.schedulerService = schedulerService;
    }

    @Override
    public CompletableFuture<Void> reset(ArenaInstance instance, Arena arena) {
        return templateRepository.loadTemplate(arena.getId())
            .thenCompose(templateData -> {
                return schedulerService.runSync(() -> {
                    worldGateway.applySectionData(
                        instance.getInstanceRegion().getWorld(),
                        instance.getInstanceRegion(),
                        templateData
                    );
                });
            });
    }
}
