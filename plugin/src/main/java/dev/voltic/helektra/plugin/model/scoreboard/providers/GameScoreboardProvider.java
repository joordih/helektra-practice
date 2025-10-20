package dev.voltic.helektra.plugin.model.scoreboard.providers;

import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.scoreboard.config.PlaceholderService;
import dev.voltic.helektra.plugin.model.scoreboard.config.ScoreboardConfiguration;

public class GameScoreboardProvider extends BaseScoreboardProvider {

  public GameScoreboardProvider(IProfileService profileService,
      ScoreboardConfiguration config,
      PlaceholderService placeholderService) {
    super(profileService, config, placeholderService, ProfileState.IN_GAME);
  }

  @Override
  protected void setupCustomPlaceholders() {
    placeholderService.registerCustomPlaceholder("player_health",
        player -> String.valueOf((int) player.getHealth()));

    placeholderService.registerCustomPlaceholder("opponent_name",
        player -> "???");
  }
}
