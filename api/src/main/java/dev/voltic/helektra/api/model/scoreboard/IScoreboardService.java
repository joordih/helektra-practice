package dev.voltic.helektra.api.model.scoreboard;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface IScoreboardService {
    
    IScoreboard createScoreboard(Player player);
    
    Optional<IScoreboard> getScoreboard(UUID uuid);
    
    Optional<IScoreboard> getScoreboard(Player player);
    
    void removeScoreboard(UUID uuid);
    
    void removeScoreboard(Player player);
    
    void updateAll();
    
    boolean hasScoreboard(UUID uuid);
    
    boolean hasScoreboard(Player player);
}
