package dev.voltic.helektra.plugin.model.scoreboard.providers;

import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.plugin.model.scoreboard.config.PlaceholderService;
import dev.voltic.helektra.plugin.model.scoreboard.config.ScoreboardConfiguration;
import java.time.Duration;
import java.time.Instant;

public class QueueScoreboardProvider extends BaseScoreboardProvider {

  private final IQueueService queueService;

  public QueueScoreboardProvider(IProfileService profileService,
      ScoreboardConfiguration config,
      PlaceholderService placeholderService,
      IQueueService queueService) {
    super(profileService, config, placeholderService, ProfileState.IN_QUEUE);
    this.queueService = queueService;
  }

  @Override
  protected void setupCustomPlaceholders() {
    placeholderService.registerCustomPlaceholder("queue_time",
        player -> queueService.getTicket(player.getUniqueId())
            .map(ticket -> Duration.between(ticket.getJoinedAt(), Instant.now()).getSeconds())
            .map(Object::toString)
            .orElse("0"));
  }
}
