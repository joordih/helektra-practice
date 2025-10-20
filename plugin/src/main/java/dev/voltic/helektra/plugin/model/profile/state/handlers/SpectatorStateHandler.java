package dev.voltic.helektra.plugin.model.profile.state.handlers;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.state.ProfileHotbarService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateHandler;
import dev.voltic.helektra.plugin.model.profile.state.listeners.SpectatorBoundListener;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@Singleton
public class SpectatorStateHandler implements ProfileStateHandler {

  private final ProfileHotbarService hotbarService;
  private final Provider<SpectatorBoundListener> listenerProvider;

  @Inject
  public SpectatorStateHandler(ProfileHotbarService hotbarService, Provider<SpectatorBoundListener> listenerProvider) {
    this.hotbarService = hotbarService;
    this.listenerProvider = listenerProvider;
  }

  @Override
  public ProfileState target() {
    return ProfileState.SPECTATOR;
  }

  @Override
  public void onEnter(Player player, IProfile profile) {
    player.setAllowFlight(true);
    player.setFlying(true);
    hotbarService.apply(player, ProfileState.SPECTATOR);
  }

  @Override
  public void onExit(Player player, IProfile profile) {
    player.setFlying(false);
    player.setAllowFlight(false);
  }

  @Override
  public Collection<Listener> createPlayerListeners(Player player, IProfile profile) {
    return List.of(listenerProvider.get().bind(player.getUniqueId()));
  }
}
