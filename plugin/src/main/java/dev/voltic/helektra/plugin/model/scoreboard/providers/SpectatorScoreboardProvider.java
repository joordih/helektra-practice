package dev.voltic.helektra.plugin.model.scoreboard.providers;

import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.scoreboard.config.PlaceholderService;
import dev.voltic.helektra.plugin.model.scoreboard.config.ScoreboardConfiguration;

public class SpectatorScoreboardProvider extends BaseScoreboardProvider {

  public SpectatorScoreboardProvider(IProfileService profileService,
      ScoreboardConfiguration config,
      PlaceholderService placeholderService) {
    super(profileService, config, placeholderService, ProfileState.SPECTATOR);
  }

  @Override
  protected void setupCustomPlaceholders() {
  }
}
