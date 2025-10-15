package dev.voltic.helektra.plugin.model.scoreboard.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.api.model.scoreboard.IScoreboardProvider;
import dev.voltic.helektra.plugin.model.scoreboard.config.PlaceholderService;
import dev.voltic.helektra.plugin.model.scoreboard.config.ScoreboardConfiguration;

public abstract class BaseScoreboardProvider implements IScoreboardProvider {

  protected final IProfileService profileService;
  protected final ScoreboardConfiguration.StateConfiguration stateConfig;
  protected final PlaceholderService placeholderService;
  protected final ProfileState targetState;

  protected BaseScoreboardProvider(IProfileService profileService,
      ScoreboardConfiguration config,
      PlaceholderService placeholderService,
      ProfileState targetState) {
    this.profileService = profileService;
    this.stateConfig = config.getStates().get(targetState);
    this.placeholderService = placeholderService;
    this.targetState = targetState;

    setupCustomPlaceholders();
  }

  @Override
  public String getTitle(Player player) {
    return placeholderService.replacePlaceholders(stateConfig.getTitle(), player);
  }

  @Override
  public List<String> getLines(Player player) {
    List<String> processedLines = new ArrayList<>();

    for (String line : stateConfig.getLines()) {
      String processedLine = placeholderService.replacePlaceholders(line, player);
      processedLines.add(processedLine);
    }

    return processedLines;
  }

  @Override
  public boolean shouldDisplay(Player player) {
    if (!stateConfig.isEnabled()) {
      return false;
    }

    Optional<IProfile> profileOpt = profileService.getProfile(player.getUniqueId()).join();
    return profileOpt.map(profile -> profile.getProfileState() == targetState).orElse(false);
  }

  protected abstract void setupCustomPlaceholders();
}
