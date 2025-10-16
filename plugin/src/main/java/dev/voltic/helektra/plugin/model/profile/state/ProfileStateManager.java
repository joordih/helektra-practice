package dev.voltic.helektra.plugin.model.profile.state;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.api.model.scoreboard.IScoreboardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@Singleton
public class ProfileStateManager {

  private final IProfileService profileService;
  private final IScoreboardService scoreboardService;
  private final ProfileStateListenerRegistry listenerRegistry;
  private final Map<ProfileState, ProfileStateHandler> handlers;
  private final Logger logger;

  @Inject
  public ProfileStateManager(IProfileService profileService,
      IScoreboardService scoreboardService,
      Set<ProfileStateHandler> handlerSet,
      ProfileStateListenerRegistry listenerRegistry) {
    this.profileService = profileService;
    this.scoreboardService = scoreboardService;
    this.listenerRegistry = listenerRegistry;
    this.handlers = new EnumMap<>(ProfileState.class);
    this.logger = Bukkit.getLogger();
    for (ProfileStateHandler handler : handlerSet) {
      handlers.put(handler.target(), handler);
    }
  }

  public void setState(Player player, ProfileState newState) {
    Optional<IProfile> profileOpt = profileService.getCachedProfile(player.getUniqueId());
    if (profileOpt.isEmpty()) {
      return;
    }

    IProfile profile = profileOpt.get();
    ProfileState currentState = profile.getProfileState();
    if (currentState == newState) {
      return;
    }

    UUID playerId = player.getUniqueId();

    listenerRegistry.unregister(playerId);
    handleExit(player, profile, currentState);

    profile.setProfileState(newState);
    profileService.saveProfile(profile);

    handleEnter(player, profile, playerId, newState);

    ensureScoreboard(player);
    Bukkit.getPluginManager().callEvent(new ProfileStateChangeEvent(player, profile, currentState, newState));
  }

  private void handleExit(Player player, IProfile profile, ProfileState state) {
    ProfileStateHandler handler = handlers.get(state);
    if (handler == null) {
      return;
    }
    runSafely(() -> handler.onExit(player, profile), handler, "exit", state);
  }

  private void handleEnter(Player player, IProfile profile, UUID playerId, ProfileState state) {
    ProfileStateHandler handler = handlers.get(state);

    if (handler == null) {
      return;
    }

    runSafely(() -> handler.onEnter(player, profile), handler, "enter", state);
    registerPlayerListeners(playerId, handler.createPlayerListeners(player, profile));
  }

  private void registerPlayerListeners(UUID playerId, Iterable<Listener> listeners) {
    if (listeners == null)
      return;

    ArrayList<Listener> collected = Lists.newArrayList();

    for (Listener listener : listeners) {
      if (listener != null) {
        collected.add(listener);
      }
    }

    if (collected.isEmpty()) {
      return;
    }

    listenerRegistry.register(playerId, collected);
  }

  private void ensureScoreboard(Player player) {
    scoreboardService.getScoreboard(player).orElseGet(() -> scoreboardService.createScoreboard(player));
  }

  private void runSafely(Runnable task, ProfileStateHandler handler, String stage, ProfileState state) {
    try {
      task.run();
    } catch (Exception exception) {
      logger.log(Level.SEVERE,
          "Error during profile state " + stage + " for " + state.name() + " using " + handler.getClass().getName(),
          exception);
    }
  }
}
