package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import dev.voltic.helektra.api.model.kit.IPlayerKitLayoutService;
import dev.voltic.helektra.plugin.model.kit.layout.PlayerKitLayoutServiceImpl;
import dev.voltic.helektra.plugin.model.kit.layout.repository.MongoPlayerKitLayoutRepository;
import dev.voltic.helektra.plugin.model.kit.layout.repository.PlayerKitLayoutRepository;

public class PlayerKitLayoutModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PlayerKitLayoutRepository.class)
      .to(MongoPlayerKitLayoutRepository.class)
      .in(Scopes.SINGLETON);

    bind(IPlayerKitLayoutService.class)
      .to(PlayerKitLayoutServiceImpl.class)
      .in(Scopes.SINGLETON);
  }
}
