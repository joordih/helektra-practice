package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.ArenaVisibilitySettings;
import dev.voltic.helektra.api.model.arena.IArenaPoolService;
import dev.voltic.helektra.api.model.arena.IArenaResetService;
import dev.voltic.helektra.api.model.arena.IArenaSelectionService;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.arena.IArenaSnapshotService;
import dev.voltic.helektra.api.model.arena.IArenaTemplateService;
import dev.voltic.helektra.api.model.arena.IArenaVisibilityService;
import dev.voltic.helektra.api.model.arena.IMetricsService;
import dev.voltic.helektra.api.model.arena.IPhysicsGuardService;
import dev.voltic.helektra.api.model.arena.ISchedulerService;
import dev.voltic.helektra.api.repository.IArenaJournalRepository;
import dev.voltic.helektra.api.repository.IArenaRepository;
import dev.voltic.helektra.api.repository.IArenaTemplateRepository;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.arena.ArenaPoolService;
import dev.voltic.helektra.plugin.model.arena.ArenaSelectionService;
import dev.voltic.helektra.plugin.model.arena.ArenaService;
import dev.voltic.helektra.plugin.model.arena.ArenaTemplateService;
import dev.voltic.helektra.plugin.model.arena.ArenaVisibilityService;
import dev.voltic.helektra.plugin.model.arena.MetricsService;
import dev.voltic.helektra.plugin.model.arena.PhysicsGuardService;
import dev.voltic.helektra.plugin.model.arena.SchedulerService;
import dev.voltic.helektra.plugin.model.arena.reset.ArenaResetService;
import dev.voltic.helektra.plugin.model.arena.snapshot.ArenaSnapshotService;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;
import dev.voltic.helektra.plugin.repository.FileArenaRepository;
import dev.voltic.helektra.plugin.repository.FileArenaTemplateRepository;
import dev.voltic.helektra.plugin.repository.MemoryArenaJournalRepository;
import dev.voltic.helektra.plugin.utils.config.FileConfig;
import org.bukkit.configuration.ConfigurationSection;

public class ArenaModule extends AbstractModule {

    private final FileConfig settingsConfig;

    public ArenaModule(FileConfig settingsConfig) {
        this.settingsConfig = settingsConfig;
    }

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
        bind(IArenaSnapshotService.class).to(ArenaSnapshotService.class);
        bind(IArenaVisibilityService.class).to(ArenaVisibilityService.class);

        bind(IArenaRepository.class).to(FileArenaRepository.class);
        bind(IArenaTemplateRepository.class).to(
            FileArenaTemplateRepository.class
        );
        bind(IArenaJournalRepository.class).to(
            MemoryArenaJournalRepository.class
        );

        bind(WorldGateway.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    FileConfig provideArenasConfig(Helektra plugin) {
        return new FileConfig(plugin, "arenas.yml");
    }

    @Provides
    @Singleton
    ArenaVisibilitySettings provideArenaVisibilitySettings() {
        ConfigurationSection section = settingsConfig
            .getConfig()
            .getConfigurationSection("arena.visibility");
        int gapChunks = section != null ? section.getInt("gap-chunks", 64) : 64;
        boolean hideEntities = section != null
            ? section.getBoolean("hide-entities", true)
            : true;
        boolean hidePlayers = section != null
            ? section.getBoolean("hide-players", true)
            : true;
        boolean multiworldEnabled = section != null
            ? section.getBoolean("multiworld-enabled", false)
            : false;
        return ArenaVisibilitySettings.builder()
            .gapChunks(gapChunks)
            .hideEntities(hideEntities)
            .hidePlayers(hidePlayers)
            .multiworldEnabled(multiworldEnabled)
            .build();
    }
}
