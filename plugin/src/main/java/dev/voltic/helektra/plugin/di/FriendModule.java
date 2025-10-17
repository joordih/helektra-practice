package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import dev.voltic.helektra.api.model.profile.IFriendService;
import dev.voltic.helektra.plugin.model.profile.friend.FriendServiceImpl;
import dev.voltic.helektra.plugin.model.profile.friend.repository.FriendRepository;
import dev.voltic.helektra.plugin.model.profile.friend.repository.MongoFriendRepository;

public class FriendModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(FriendRepository.class)
      .to(MongoFriendRepository.class)
      .in(Scopes.SINGLETON);

    bind(IFriendService.class).to(FriendServiceImpl.class).in(Scopes.SINGLETON);
  }
}
