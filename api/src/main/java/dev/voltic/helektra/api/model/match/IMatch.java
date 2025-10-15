package dev.voltic.helektra.api.model.match;

import dev.voltic.helektra.api.model.Model;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.profile.IProfile;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMatch extends Model {
    String getMatchId();
    MatchType getMatchType();
    MatchStatus getStatus();
    void setStatus(MatchStatus status);
    
    Arena getArena();
    IKit getKit();
    
    List<Participant> getParticipants();
    List<UUID> getSpectators();
    
    void addSpectator(UUID uniqueId);
    void removeSpectator(UUID uniqueId);
    boolean isSpectator(UUID uniqueId);
    
    Optional<Participant> getParticipant(UUID uniqueId);
    Optional<IProfile> getProfile(Player player);
    
    List<MatchRound> getRounds();
    Optional<MatchRound> getCurrentRound();
    void addRound(MatchRound round);
    
    boolean hasEnded();
    long getStartTime();
    long getEndTime();
    
    void broadcast(String message);
    void broadcastActionBar(String message);
}
