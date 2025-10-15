package dev.voltic.helektra.plugin.model.match.commands;

import com.google.inject.Inject;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.match.menu.MatchModeMenu;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

import java.util.List;

@Command(names = {"queue", "match", "play"})
public class QueueCommand implements CommandClass {
    private final IMatchService matchService;
    private final Helektra helektra;

    @Inject
    public QueueCommand(IMatchService matchService, Helektra helektra) {
        this.matchService = matchService;
        this.helektra = helektra;
    }

    @Command(names = "")
    public void mainCommand(@Sender Player player) {
        if (!(player instanceof Player)) {
            player.sendMessage(TranslationUtils.translate("error.player-only"));
            return;
        }
        helektra.getMenuFactory().openMenu(MatchModeMenu.class, player);
    }

    @Command(names = "list")
    public void listMatches(@Sender Player player) {
        List<?> activeMatches = matchService.getActiveMatches();
        if (activeMatches.isEmpty()) {
            player.sendMessage(TranslationUtils.translate("match.no-active-matches"));
            return;
        }
        player.sendMessage(TranslationUtils.translate("match.active-matches-header", "count", activeMatches.size()));
        activeMatches.forEach(match -> {
            player.sendMessage(TranslationUtils.translate("match.active-match-format"));
        });
    }

    @Command(names = "status")
    public void matchStatus(@Sender Player player) {
        var matchOpt = matchService.getMatchByParticipant(player.getUniqueId());
        if (matchOpt.isEmpty()) {
            player.sendMessage(TranslationUtils.translate("match.not-in-match"));
            return;
        }
        var match = matchOpt.get();
        player.sendMessage(TranslationUtils.translate("match.status-header", "type", match.getMatchType().getDisplayName()));
        player.sendMessage(TranslationUtils.translate("match.status-participants", "count", match.getParticipants().size()));
        player.sendMessage(TranslationUtils.translate("match.status-spectators", "count", match.getSpectators().size()));
    }

    @Command(names = "leave")
    public void leaveMatch(@Sender Player player) {
        var matchOpt = matchService.getMatchByParticipant(player.getUniqueId());
        if (matchOpt.isEmpty()) {
            player.sendMessage(TranslationUtils.translate("match.not-in-match"));
            return;
        }
        var match = matchOpt.get();
        if (match.isSpectator(player.getUniqueId())) {
            match.removeSpectator(player.getUniqueId());
            matchService.saveMatch(match);
            player.sendMessage(TranslationUtils.translate("match.spectator-left"));
        } else {
            player.sendMessage(TranslationUtils.translate("match.cannot-leave-participant"));
        }
    }
}
