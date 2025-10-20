package dev.voltic.helektra.plugin.model.scoreboard.providers;

import org.bukkit.Bukkit;

import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.scoreboard.config.PlaceholderService;
import dev.voltic.helektra.plugin.model.scoreboard.config.ScoreboardConfiguration;

public class EventScoreboardProvider extends BaseScoreboardProvider {

  public EventScoreboardProvider(IProfileService profileService,
      ScoreboardConfiguration config,
      PlaceholderService placeholderService) {
    super(profileService, config, placeholderService, ProfileState.IN_EVENT);
  }

  @Override
  protected void setupCustomPlaceholders() {
    placeholderService.registerCustomPlaceholder("event_participants",
        player -> String.valueOf(Bukkit.getOnlinePlayers().size()));

    placeholderService.registerCustomPlaceholder("online_players",
        player -> String.valueOf(Bukkit.getOnlinePlayers().size()));
  }
}
