package dev.voltic.helektra.plugin.model.profile;

import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.LocationUtil;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Singleton
public class LobbyService {

  private final Helektra helektra;
  private final ProfileStateManager profileStateManager;

  @Inject
  public LobbyService(
    Helektra helektra,
    ProfileStateManager profileStateManager
  ) {
    this.helektra = helektra;
    this.profileStateManager = profileStateManager;
  }

  public void send(Player player) {
    String rawSpawn = helektra
      .getSettingsConfig()
      .getConfig()
      .getString("settings.spawn");
    Location spawn = LocationUtil.deserialize(rawSpawn);
    if (spawn != null) {
      player.teleport(spawn);
    }
    profileStateManager.setState(player, ProfileState.LOBBY);
  }
}
