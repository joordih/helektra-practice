package dev.voltic.helektra.plugin.model.match.listeners;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.IMatchService;
import jakarta.inject.Inject;

public class MatchListener implements Listener {
    private final IMatchService matchService;

    @Inject
    public MatchListener(IMatchService matchService) {
        this.matchService = matchService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Optional<IMatch> matchOpt = matchService.getMatchByParticipant(player.getUniqueId());
        
        if (matchOpt.isPresent()) {
            IMatch match = matchOpt.get();
            
            if (match.isSpectator(player.getUniqueId())) {
                match.removeSpectator(player.getUniqueId());
                matchService.saveMatch(match);
            }
        }
    }
}
