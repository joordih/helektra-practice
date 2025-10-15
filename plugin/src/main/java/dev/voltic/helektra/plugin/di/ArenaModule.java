package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.*;
import dev.voltic.helektra.api.repository.IArenaJournalRepository;
import dev.voltic.helektra.api.repository.IArenaRepository;
import dev.voltic.helektra.api.repository.IArenaTemplateRepository;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.arena.*;
import dev.voltic.helektra.plugin.model.arena.reset.ArenaResetService;
import dev.voltic.helektra.plugin.model.arena.reset.strategies.*;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;
import dev.voltic.helektra.plugin.repository.FileArenaRepository;
import dev.voltic.helektra.plugin.repository.FileArenaTemplateRepository;
import dev.voltic.helektra.plugin.repository.MemoryArenaJournalRepository;
import dev.voltic.helektra.plugin.utils.config.FileConfig;

public class ArenaModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(IArenaService.class).to(ArenaService.class);
        bind(IArenaPoolService.class).to(ArenaPoolService.class);
        bind(IArenaResetService.class).to(ArenaResetService.class);
        bind(IArenaTemplateService.class).to(ArenaTemplateService.class);
        bind(IArenaSelectionService.class).to(ArenaSelectionService.class);
        bind(IPhysicsGuardService.class).to(PhysicsGuardService.class);
        bind(IMetricsService.class).to(MetricsService.class);
        bind(ISchedulerService.class).to(SchedulerService.class);
        
        bind(IArenaRepository.class).to(FileArenaRepository.class);
        bind(IArenaTemplateRepository.class).to(FileArenaTemplateRepository.class);
        bind(IArenaJournalRepository.class).to(MemoryArenaJournalRepository.class);
        
        bind(WorldGateway.class).in(Singleton.class);
        bind(JournalResetStrategy.class).in(Singleton.class);
        bind(SectionResetStrategy.class).in(Singleton.class);
        bind(ChunkSwapResetStrategy.class).in(Singleton.class);
        bind(HybridResetStrategy.class).in(Singleton.class);
    }
    
    @Provides
    @Singleton
    FileConfig provideArenasConfig(Helektra plugin) {
        return new FileConfig(plugin, "arenas.yml");
    }
}
