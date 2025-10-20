package dev.voltic.helektra.plugin.model.match.listeners;

import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.plugin.model.match.event.MatchEndedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Singleton
public class QueueMatchListener implements Listener {

    private final IQueueService queueService;
    private final IMatchService matchService;

    @Inject
    public QueueMatchListener(
        IQueueService queueService,
        IMatchService matchService
    ) {
        this.queueService = queueService;
        this.matchService = matchService;
    }

    @EventHandler
    public void onMatchEnded(MatchEndedEvent event) {
        String matchId = event.getMatch().getMatchId();
        queueService
            .completeMatch(matchId)
            .whenComplete((ignored, error) -> {
                if (error != null) {
                    String message = error.getMessage() != null
                        ? error.getMessage()
                        : error.toString();
                    org.bukkit.Bukkit.getLogger().severe(
                        "Failed to complete match " + matchId + ": " + message
                    );
                }
                matchService.removeMatch(matchId);
            });
    }
}
