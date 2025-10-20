package dev.voltic.helektra.plugin.model.profile.state.handlers;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileHotbarService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.listeners.QueueStateBoundListener;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@Singleton
public class QueueStateHandler implements ProfileStateHandler {

  private final ProfileHotbarService hotbarService;
  private final Provider<QueueStateBoundListener> listenerProvider;
  private final IQueueService queueService;

  @Inject
  public QueueStateHandler(
    ProfileHotbarService hotbarService,
    Provider<QueueStateBoundListener> listenerProvider,
    IQueueService queueService
  ) {
    this.hotbarService = hotbarService;
    this.listenerProvider = listenerProvider;
    this.queueService = queueService;
  }

  @Override
  public ProfileState target() {
    return ProfileState.IN_QUEUE;
  }

  @Override
  public void onEnter(Player player, IProfile profile) {
    hotbarService.apply(player, ProfileState.IN_QUEUE);
  }

  @Override
  public void onExit(Player player, IProfile profile) {
    queueService.clearTicket(profile.getUniqueId());
  }

  @Override
  public Collection<Listener> createPlayerListeners(
    Player player,
    IProfile profile
  ) {
    return List.of(listenerProvider.get().bind(player.getUniqueId()));
  }
}
