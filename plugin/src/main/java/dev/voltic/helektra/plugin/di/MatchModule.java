package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.plugin.model.match.MatchArenaTracker;
import dev.voltic.helektra.plugin.model.match.MatchRepository;
import dev.voltic.helektra.plugin.model.match.MatchServiceImpl;
import dev.voltic.helektra.plugin.model.match.queue.QueueService;

public class MatchModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(MatchRepository.class).in(Scopes.SINGLETON);
    bind(IMatchService.class).to(MatchServiceImpl.class).in(Scopes.SINGLETON);
    bind(IQueueService.class).to(QueueService.class).in(Scopes.SINGLETON);
    bind(MatchArenaTracker.class).in(Scopes.SINGLETON);
  }
}
