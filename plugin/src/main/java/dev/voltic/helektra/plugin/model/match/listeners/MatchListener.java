package dev.voltic.helektra.plugin.model.match.listeners;

import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.api.model.match.Participant;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.plugin.model.match.MatchConclusionService;
import dev.voltic.helektra.plugin.model.match.MatchUtils;
import dev.voltic.helektra.plugin.model.match.event.ParticipantDeathEvent;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MatchListener implements Listener {

    private final IMatchService matchService;
    private final IProfileService profileService;
    private final IQueueService queueService;
    private final MatchConclusionService conclusionService;

    @Inject
    public MatchListener(
        IMatchService matchService,
        IProfileService profileService,
        IQueueService queueService,
        MatchConclusionService conclusionService
    ) {
        this.matchService = matchService;
        this.profileService = profileService;
        this.queueService = queueService;
        this.conclusionService = conclusionService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Optional<IProfile> profileOpt = profileService.getCachedProfile(
            player.getUniqueId()
        );
        Optional<IMatch> matchOpt = matchService.getMatchByParticipant(
            player.getUniqueId()
        );
        if (profileOpt.isEmpty()) {
            return;
        }
        IProfile profile = profileOpt.get();
        if (profile.getProfileState() == ProfileState.IN_QUEUE) {
            queueService.leaveQueue(player.getUniqueId());
        }
        matchOpt.ifPresent(match -> {
            if (match.isSpectator(player.getUniqueId())) {
                match.removeSpectator(player.getUniqueId());
                matchService.saveMatch(match);
            }
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        Optional<IMatch> matchOpt = matchService.getMatchByParticipant(
            player.getUniqueId()
        );
        if (matchOpt.isEmpty()) {
            return;
        }
        IMatch match = matchOpt.get();
        if (match.hasEnded()) {
            return;
        }
        MatchType type = match.getMatchType();
        if (type != MatchType.DUEL && type != MatchType.QUEUE) {
            return;
        }
        Optional<Participant> participantOpt = match.getParticipant(
            player.getUniqueId()
        );
        if (participantOpt.isEmpty()) {
            return;
        }
        Participant participant = participantOpt.get();
        participant.setAlive(false);
        participant.setDeaths(participant.getDeaths() + 1);
        participant.setName(player.getName());
        Player killer = player.getKiller();
        UUID killerId = killer != null ? killer.getUniqueId() : null;
        if (killerId != null) {
            match
                .getParticipant(killerId)
                .ifPresent(killerParticipant -> {
                    killerParticipant.setKills(
                        killerParticipant.getKills() + 1
                    );
                    killerParticipant.setName(killer.getName());
                });
        }
        matchService.saveMatch(match);
        Bukkit.getPluginManager().callEvent(
            new ParticipantDeathEvent(match, player.getUniqueId(), killerId)
        );
        Optional<Participant> winner = MatchUtils.getWinner(match);
        if (winner.isPresent()) {
            Participant winnerParticipant = winner.get();
            String winnerName = resolveParticipantName(winnerParticipant);
            if (winnerName == null || winnerName.isEmpty()) {
                winnerName = TranslationUtils.translate("match.end.unknown");
            }
            match.broadcast(
                TranslationUtils.translate(
                    "match.end.winner",
                    "winner",
                    winnerName
                )
            );
        } else {
            match.broadcast(TranslationUtils.translate("match.end.draw"));
        }
        conclusionService.conclude(match, participant, winner);
    }

    private String resolveParticipantName(Participant participant) {
        if (participant.getName() != null && !participant.getName().isEmpty()) {
            return participant.getName();
        }
        Player online = Bukkit.getPlayer(participant.getUniqueId());
        if (online != null) {
            participant.setName(online.getName());
            return online.getName();
        }
        String offlineName = Bukkit.getOfflinePlayer(
            participant.getUniqueId()
        ).getName();
        if (offlineName != null && !offlineName.isEmpty()) {
            participant.setName(offlineName);
            return offlineName;
        }
        return null;
    }
}
