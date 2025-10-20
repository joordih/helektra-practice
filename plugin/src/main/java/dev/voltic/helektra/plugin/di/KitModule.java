package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.plugin.model.kit.KitServiceImpl;
import dev.voltic.helektra.plugin.model.kit.repository.KitRepository;
import dev.voltic.helektra.plugin.model.kit.repository.YamlKitRepository;
import dev.voltic.helektra.plugin.utils.config.FileConfig;

public class KitModule extends AbstractModule {

    private final FileConfig kitsConfig;

    public KitModule(FileConfig kitsConfig) {
        this.kitsConfig = kitsConfig;
    }

    @Override
    protected void configure() {
        bind(IKitService.class).to(KitServiceImpl.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    public KitRepository provideKitRepository() {
        return new YamlKitRepository(kitsConfig);
    }
}
