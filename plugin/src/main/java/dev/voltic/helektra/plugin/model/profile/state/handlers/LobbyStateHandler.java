package dev.voltic.helektra.plugin.model.profile.state.handlers;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.listeners.LobbyStateBoundListener;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@Singleton
public class LobbyStateHandler implements ProfileStateHandler {

  private final Provider<LobbyStateBoundListener> listenerProvider;

  @Inject
  public LobbyStateHandler(Provider<LobbyStateBoundListener> listenerProvider) {
    this.listenerProvider = listenerProvider;
  }

  @Override
  public ProfileState target() {
    return ProfileState.LOBBY;
  }

  @Override
  public void onEnter(Player player, IProfile profile) {}

  @Override
  public void onExit(Player player, IProfile profile) {}

  @Override
  public Collection<Listener> createPlayerListeners(
    Player player,
    IProfile profile
  ) {
    return List.of(listenerProvider.get().bind(player.getUniqueId()));
  }
}
