package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.model.profile.ProfileServiceImpl;
import dev.voltic.helektra.plugin.model.profile.repository.MongoProfileRepository;
import dev.voltic.helektra.plugin.model.profile.repository.ProfileRepository;

public class ProfileModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProfileRepository.class).to(MongoProfileRepository.class).in(Scopes.SINGLETON);
        bind(IProfileService.class).to(ProfileServiceImpl.class).in(Scopes.SINGLETON);
    }
}
