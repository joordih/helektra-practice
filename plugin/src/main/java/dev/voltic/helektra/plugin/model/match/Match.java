package dev.voltic.helektra.plugin.model.match;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.MatchRound;
import dev.voltic.helektra.api.model.match.MatchStatus;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.api.model.match.Participant;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.plugin.Helektra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match implements IMatch {
    private String matchId;
    private MatchType matchType;
    private MatchStatus status;
    private Arena arena;
    private IKit kit;
    private List<Participant> participants;
    private List<UUID> spectators;
    private List<MatchRound> rounds;
    private long startTime;
    private long endTime;

    @Override
    public String getId() {
        return matchId;
    }

    @Override
    public String getMatchId() {
        return matchId;
    }

    @Override
    public MatchType getMatchType() {
        return matchType;
    }

    @Override
    public MatchStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    @Override
    public Arena getArena() {
        return arena;
    }

    @Override
    public IKit getKit() {
        return kit;
    }

    @Override
    public List<Participant> getParticipants() {
        return participants != null ? participants : new ArrayList<>();
    }

    @Override
    public List<UUID> getSpectators() {
        return spectators != null ? spectators : new ArrayList<>();
    }

    @Override
    public void addSpectator(UUID uniqueId) {
        if (spectators == null) {
            spectators = new ArrayList<>();
        }
        if (!spectators.contains(uniqueId)) {
            spectators.add(uniqueId);
        }
    }

    @Override
    public void removeSpectator(UUID uniqueId) {
        if (spectators != null) {
            spectators.remove(uniqueId);
        }
    }

    @Override
    public boolean isSpectator(UUID uniqueId) {
        return spectators != null && spectators.contains(uniqueId);
    }

    @Override
    public Optional<Participant> getParticipant(UUID uniqueId) {
        return getParticipants().stream()
            .filter(p -> p.getUniqueId().equals(uniqueId))
            .findFirst();
    }

    @Override
    public Optional<IProfile> getProfile(Player player) {
        return Helektra.getInstance().getAPI().getProfileService()
            .getCachedProfile(player.getUniqueId());
    }

    @Override
    public List<MatchRound> getRounds() {
        return rounds != null ? rounds : new ArrayList<>();
    }

    @Override
    public Optional<MatchRound> getCurrentRound() {
        if (rounds == null || rounds.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rounds.get(rounds.size() - 1));
    }

    @Override
    public void addRound(MatchRound round) {
        if (rounds == null) {
            rounds = new ArrayList<>();
        }
        rounds.add(round);
    }

    @Override
    public boolean hasEnded() {
        return status == MatchStatus.ENDED;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public void broadcast(String message) {
        Component component = Component.text(message).color(NamedTextColor.WHITE);
        
        getParticipants().forEach(participant -> {
            Player player = org.bukkit.Bukkit.getPlayer(participant.getUniqueId());
            if (player != null && player.isOnline()) {
                Helektra.getInstance().getAdventure().player(player).sendMessage(component);
            }
        });
        
        getSpectators().forEach(spectatorId -> {
            Player spectator = org.bukkit.Bukkit.getPlayer(spectatorId);
            if (spectator != null && spectator.isOnline()) {
                Helektra.getInstance().getAdventure().player(spectator).sendMessage(component);
            }
        });
    }

    @Override
    public void broadcastActionBar(String message) {
        Component component = Component.text(message).color(NamedTextColor.YELLOW);
        
        getParticipants().forEach(participant -> {
            Player player = org.bukkit.Bukkit.getPlayer(participant.getUniqueId());
            if (player != null && player.isOnline()) {
                Helektra.getInstance().getAdventure().player(player).sendActionBar(component);
            }
        });
        
        getSpectators().forEach(spectatorId -> {
            Player spectator = org.bukkit.Bukkit.getPlayer(spectatorId);
            if (spectator != null && spectator.isOnline()) {
                Helektra.getInstance().getAdventure().player(spectator).sendActionBar(component);
            }
        });
    }
}
