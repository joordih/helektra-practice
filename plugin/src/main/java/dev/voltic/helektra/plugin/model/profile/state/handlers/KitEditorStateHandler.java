package dev.voltic.helektra.plugin.model.profile.state.handlers;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.kit.layout.listeners.KitLayoutEditorListener;
import dev.voltic.helektra.plugin.model.profile.state.ProfileHotbarService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateHandler;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@Singleton
public class KitEditorStateHandler implements ProfileStateHandler {

  private final ProfileHotbarService hotbarService;
  private final Provider<KitLayoutEditorListener> listenerProvider;

  @Inject
  public KitEditorStateHandler(
    ProfileHotbarService hotbarService,
    Provider<KitLayoutEditorListener> listenerProvider
  ) {
    this.hotbarService = hotbarService;
    this.listenerProvider = listenerProvider;
  }

  @Override
  public ProfileState target() {
    return ProfileState.KIT_EDITOR;
  }

  @Override
  public void onEnter(Player player, IProfile profile) {
    hotbarService.apply(player, ProfileState.KIT_EDITOR);
  }

  @Override
  public void onExit(Player player, IProfile profile) {
  }

  @Override
  public Collection<Listener> createPlayerListeners(Player player, IProfile profile) {
    KitLayoutEditorListener listener = listenerProvider.get();
    return List.of(listener.createBoundListener(player.getUniqueId()));
  }
}
