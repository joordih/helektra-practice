package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import dev.voltic.helektra.plugin.model.profile.hotbar.HotbarActionExecutor;
import dev.voltic.helektra.plugin.model.profile.hotbar.HotbarItemFactory;
import dev.voltic.helektra.plugin.model.profile.hotbar.ProfileHotbarLayoutRepository;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl.FriendsAction;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl.JoinQueueAction;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl.KitEditorAction;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl.LeaveQueueAction;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl.SettingsAction;
import dev.voltic.helektra.plugin.model.profile.state.ProfileHotbarService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateListenerRegistry;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.model.profile.state.handlers.EventStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.handlers.InGameStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.handlers.KitEditorStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.handlers.LobbyStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.handlers.PartyStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.handlers.QueueStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.handlers.SpectatorStateHandler;

public class ProfileStateModule extends AbstractModule {

  @Override
  protected void configure() {
    bindSingleton(ProfileStateManager.class);
    bindSingleton(ProfileStateListenerRegistry.class);
    bindSingleton(ProfileHotbarService.class);
    bindSingleton(HotbarActionExecutor.class);
    bindSingleton(ProfileHotbarLayoutRepository.class);
    bindSingleton(HotbarItemFactory.class);

    bindProfileStateHandlers();
    bindHotbarActions();
  }

  private void bindProfileStateHandlers() {
    Multibinder<ProfileStateHandler> handlerBinder = Multibinder.newSetBinder(
      binder(),
      ProfileStateHandler.class
    );
    bindHandler(handlerBinder, LobbyStateHandler.class);
    bindHandler(handlerBinder, QueueStateHandler.class);
    bindHandler(handlerBinder, InGameStateHandler.class);
    bindHandler(handlerBinder, SpectatorStateHandler.class);
    bindHandler(handlerBinder, KitEditorStateHandler.class);
    bindHandler(handlerBinder, PartyStateHandler.class);
    bindHandler(handlerBinder, EventStateHandler.class);
  }

  private void bindHotbarActions() {
    Multibinder<HotbarAction> actionBinder = Multibinder.newSetBinder(
      binder(),
      HotbarAction.class
    );
    bindAction(actionBinder, JoinQueueAction.class);
    bindAction(actionBinder, LeaveQueueAction.class);
    bindAction(actionBinder, KitEditorAction.class);
    bindAction(actionBinder, SettingsAction.class);
    bindAction(actionBinder, FriendsAction.class);
  }

  private <T> void bindSingleton(Class<T> type) {
    bind(type).in(Scopes.SINGLETON);
  }

  private <T extends ProfileStateHandler> void bindHandler(
    Multibinder<ProfileStateHandler> binder,
    Class<T> type
  ) {
    binder.addBinding().to(type).in(Scopes.SINGLETON);
  }

  private <T extends HotbarAction> void bindAction(
    Multibinder<HotbarAction> binder,
    Class<T> type
  ) {
    binder.addBinding().to(type).in(Scopes.SINGLETON);
  }
}
