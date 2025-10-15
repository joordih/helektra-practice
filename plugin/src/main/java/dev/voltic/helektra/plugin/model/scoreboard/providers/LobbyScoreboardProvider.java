package dev.voltic.helektra.plugin.model.scoreboard.providers;

import java.util.Optional;

import org.bukkit.Bukkit;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.scoreboard.config.PlaceholderService;
import dev.voltic.helektra.plugin.model.scoreboard.config.ScoreboardConfiguration;

public class LobbyScoreboardProvider extends BaseScoreboardProvider {

  public LobbyScoreboardProvider(IProfileService profileService,
      ScoreboardConfiguration config,
      PlaceholderService placeholderService) {
    super(profileService, config, placeholderService, ProfileState.LOBBY);
  }

  @Override
  protected void setupCustomPlaceholders() {
    placeholderService.registerCustomPlaceholder("online_players",
        player -> String.valueOf(Bukkit.getOnlinePlayers().size()));

    placeholderService.registerCustomPlaceholder("profile_state_name",
        player -> {
          Optional<IProfile> profileOpt = profileService.getProfile(player.getUniqueId()).join();
          if (profileOpt.isPresent()) {
            ProfileState state = profileOpt.get().getProfileState();
            return switch (state) {
              case LOBBY -> "In Lobby";
              case IN_GAME -> "In Game";
              case IN_QUEUE -> "In Queue";
              case KIT_EDITOR -> "Kit Editor";
              case IN_PARTY -> "In Party";
              case SPECTATOR -> "Spectating";
              case IN_EVENT -> "In Event";
            };
          }
          return "Unknown";
        });

    placeholderService.registerCustomPlaceholder("player_level",
        player -> {
          Optional<IProfile> profileOpt = profileService.getProfile(player.getUniqueId()).join();
          if (profileOpt.isPresent()) {
            return String.valueOf(profileOpt.get().getLevel());
          }
          return "Unknown";
        });
  }
}
