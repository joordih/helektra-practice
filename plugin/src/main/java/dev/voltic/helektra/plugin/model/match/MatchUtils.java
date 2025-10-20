package dev.voltic.helektra.plugin.model.match;

import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.api.model.match.Participant;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class MatchUtils {
    
    public static boolean isPlayerInMatch(Player player, IMatch match) {
        return match.getParticipants().stream()
            .anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()));
    }

    public static boolean isPlayerSpectating(Player player, IMatch match) {
        return match.isSpectator(player.getUniqueId());
    }

    public static boolean isPlayerInvolvedInMatch(Player player, IMatch match) {
        return isPlayerInMatch(player, match) || isPlayerSpectating(player, match);
    }

    public static Optional<Participant> getParticipantByPlayer(Player player, IMatch match) {
        return match.getParticipant(player.getUniqueId());
    }

    public static boolean isMatchFull(IMatch match) {
        return match.getParticipants().size() >= match.getMatchType().getMaxParticipants();
    }

    public static int getMaxParticipants(MatchType type) {
        return type.getMaxParticipants();
    }

    public static int getMinParticipants(MatchType type) {
        return type.getMinParticipants();
    }

    public static boolean canStartMatch(IMatch match) {
        int minParticipants = getMinParticipants(match.getMatchType());
        return match.getParticipants().size() >= minParticipants;
    }

    public static int getAlivePlayers(IMatch match) {
        return (int) match.getParticipants().stream()
            .filter(Participant::isAlive)
            .count();
    }

    public static List<Participant> getDeadPlayers(IMatch match) {
        return match.getParticipants().stream()
            .filter(p -> !p.isAlive())
            .toList();
    }

    public static Optional<Participant> getWinner(IMatch match) {
        if (match.getMatchType() == MatchType.DUEL || match.getMatchType() == MatchType.QUEUE) {
            return match.getParticipants().stream()
                .filter(Participant::isAlive)
                .findFirst();
        }
        return match.getParticipants().stream()
            .max((p1, p2) -> Integer.compare(p1.getKills(), p2.getKills()));
    }
}
