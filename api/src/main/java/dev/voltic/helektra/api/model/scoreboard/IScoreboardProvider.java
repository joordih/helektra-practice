package dev.voltic.helektra.api.model.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public interface IScoreboardProvider {
    
    String getTitle(Player player);
    
    List<String> getLines(Player player);
    
    boolean shouldDisplay(Player player);
}
