package dev.voltic.helektra.plugin.model.profile.state;

import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.ProfileState;

public interface ProfileStateHandler {

  ProfileState target();

  void onEnter(Player player, IProfile profile);

  void onExit(Player player, IProfile profile);

  default Collection<Listener> listeners() {
    return List.of();
  }

  default Collection<Listener> createPlayerListeners(Player player, IProfile profile) {
    return List.of();
  }
}
