package dev.voltic.helektra.plugin.model.profile.state;

import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.hotbar.ProfileHotbarLayout;
import dev.voltic.helektra.plugin.model.profile.hotbar.ProfileHotbarLayoutRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.entity.Player;

@Singleton
public class ProfileHotbarService {

  private final ProfileHotbarLayoutRepository layoutRepository;
  private final Map<ProfileState, ProfileHotbarLayout> layouts;

  @Inject
  public ProfileHotbarService(ProfileHotbarLayoutRepository layoutRepository) {
    this.layoutRepository = layoutRepository;
    this.layouts = new EnumMap<>(ProfileState.class);
    reload();
  }

  public void reload() {
    layouts.clear();
    layouts.putAll(layoutRepository.load());
  }

  public void apply(Player player, ProfileState state) {
    player.getInventory().clear();
    ProfileHotbarLayout layout = layouts.get(state);
    if (layout == null || layout.isEmpty()) {
      return;
    }
    layout.apply(player);
  }

  public Optional<String> resolveAction(ProfileState state, int slot) {
    ProfileHotbarLayout layout = layouts.get(state);
    if (layout == null) {
      return Optional.empty();
    }
    return layout.actionFor(slot);
  }
}
