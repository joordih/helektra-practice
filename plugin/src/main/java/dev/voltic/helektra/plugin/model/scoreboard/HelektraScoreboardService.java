package dev.voltic.helektra.plugin.model.scoreboard;

import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.scoreboard.config.PlaceholderService;
import dev.voltic.helektra.plugin.model.scoreboard.config.ScoreboardConfiguration;
import dev.voltic.helektra.plugin.model.scoreboard.providers.EventScoreboardProvider;
import dev.voltic.helektra.plugin.model.scoreboard.providers.GameScoreboardProvider;
import dev.voltic.helektra.plugin.model.scoreboard.providers.KitEditorScoreboardProvider;
import dev.voltic.helektra.plugin.model.scoreboard.providers.LobbyScoreboardProvider;
import dev.voltic.helektra.plugin.model.scoreboard.providers.PartyScoreboardProvider;
import dev.voltic.helektra.plugin.model.scoreboard.providers.QueueScoreboardProvider;
import dev.voltic.helektra.plugin.model.scoreboard.providers.SpectatorScoreboardProvider;
import dev.voltic.helektra.plugin.model.scoreboard.wrapper.ScoreboardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;

@Singleton
@Getter
public class HelektraScoreboardService extends ScoreboardService {

  private final ScoreboardConfiguration config;
  private final PlaceholderService placeholderService;

  @Inject
  public HelektraScoreboardService(IProfileService profileService, Helektra plugin) {
    super();

    this.config = ScoreboardConfiguration.fromConfig(plugin.getScoreboardsConfig());
    this.placeholderService = new PlaceholderService();

    placeholderService.setPlaceholderAPIEnabled(true);

    registerProvider(new GameScoreboardProvider(profileService, config, placeholderService));
    registerProvider(new QueueScoreboardProvider(profileService, config, placeholderService));
    registerProvider(new EventScoreboardProvider(profileService, config, placeholderService));
    registerProvider(new SpectatorScoreboardProvider(profileService, config, placeholderService));
    registerProvider(new KitEditorScoreboardProvider(profileService, config, placeholderService));
    registerProvider(new PartyScoreboardProvider(profileService, config, placeholderService));
    registerProvider(new LobbyScoreboardProvider(profileService, config, placeholderService));
  }
}
