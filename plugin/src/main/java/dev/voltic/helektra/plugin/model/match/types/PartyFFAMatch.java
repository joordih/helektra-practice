package dev.voltic.helektra.plugin.model.match.types;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.api.model.match.Participant;
import dev.voltic.helektra.plugin.model.match.Match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PartyFFAMatch extends Match {
    private Map<UUID, String> playerTeams;
    private Map<String, Integer> teamKills;

    public PartyFFAMatch(String matchId, Arena arena, IKit kit, List<UUID> participantIds) {
        super();
        this.setMatchId(matchId);
        this.setMatchType(MatchType.PARTY_FFA);
        this.setArena(arena);
        this.setKit(kit);
        this.setStartTime(System.currentTimeMillis());
        this.playerTeams = new HashMap<>();
        this.teamKills = new HashMap<>();
        this.initializeParticipants(participantIds);
    }

    private void initializeParticipants(List<UUID> participantIds) {
        participantIds.forEach(uuid -> {
            this.getParticipants().add(
                new Participant(
                    uuid, null, 0, 0, true
                )
            );
        });
    }

    public void assignPlayerToTeam(UUID playerId, String teamName) {
        playerTeams.put(playerId, teamName);
        teamKills.putIfAbsent(teamName, 0);
    }

    public String getPlayerTeam(UUID playerId) {
        return playerTeams.get(playerId);
    }

    public int getTeamKills(String teamName) {
        return teamKills.getOrDefault(teamName, 0);
    }

    public void incrementTeamKills(String teamName) {
        teamKills.put(teamName, teamKills.getOrDefault(teamName, 0) + 1);
    }

    public List<String> getTeams() {
        return new ArrayList<>(teamKills.keySet());
    }

    public int getAlivePlayers() {
        return (int) this.getParticipants().stream()
            .filter(p -> p.isAlive())
            .count();
    }

    public boolean hasWinner() {
        return getAlivePlayers() <= 1;
    }

    public String getWinningTeam() {
        return teamKills.entrySet().stream()
            .max((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue()))
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}
