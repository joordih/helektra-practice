package dev.voltic.helektra.plugin.model.match.commands;

import com.google.inject.Inject;
import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.plugin.model.match.MatchArenaTracker;
import dev.voltic.helektra.plugin.model.match.MatchSettings;
import dev.voltic.helektra.plugin.model.match.SpectatorService;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = "spectate")
public class SpectateCommand implements CommandClass {

    private final MatchSettings settings;
    private final IMatchService matchService;
    private final SpectatorService spectatorService;
    private final MatchArenaTracker arenaTracker;

    @Inject
    public SpectateCommand(
        MatchSettings settings,
        IMatchService matchService,
        SpectatorService spectatorService,
        MatchArenaTracker arenaTracker
    ) {
        this.settings = settings;
        this.matchService = matchService;
        this.spectatorService = spectatorService;
        this.arenaTracker = arenaTracker;
    }

    @Command(names = "")
    public void spectate(@Sender Player sender, String targetName) {
        if (!spectatorService.isEnabled()) {
            sender.sendMessage(
                settings.getSpectator().getCommand().getDisabled()
            );
            return;
        }
        if (!sender.hasPermission("helektra.spectate")) {
            sender.sendMessage(
                settings.getSpectator().getCommand().getNoPermission()
            );
            return;
        }
        if (targetName == null || targetName.isEmpty()) {
            sender.sendMessage(settings.getSpectator().getCommand().getUsage());
            return;
        }
        Optional<IMatch> existing = matchService.getMatchByParticipant(
            sender.getUniqueId()
        );
        if (existing.isPresent()) {
            sender.sendMessage(
                settings.getSpectator().getCommand().getAlreadySpectating()
            );
            return;
        }
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(
                settings.getSpectator().getCommand().getNotFound()
            );
            return;
        }
        Optional<IMatch> matchOpt = matchService.getMatchByParticipant(
            target.getUniqueId()
        );
        if (matchOpt.isEmpty()) {
            sender.sendMessage(
                settings.getSpectator().getCommand().getNotFound()
            );
            return;
        }
        if (spectatorService.isSpectating(sender.getUniqueId())) {
            sender.sendMessage(
                settings.getSpectator().getCommand().getAlreadySpectating()
            );
            return;
        }
        IMatch match = matchOpt.get();
        if (match.hasEnded()) {
            sender.sendMessage(
                settings.getSpectator().getCommand().getNotFound()
            );
            return;
        }
        var instance = arenaTracker.get(match.getMatchId());
        if (instance == null) {
            sender.sendMessage(
                settings.getSpectator().getCommand().getNotFound()
            );
            return;
        }
        spectatorService.enterMatchSpectator(match, instance, sender, false);
        sender.sendMessage(
            settings
                .getSpectator()
                .getCommand()
                .getSuccess()
                .replace("%player%", target.getName())
        );
    }
}
