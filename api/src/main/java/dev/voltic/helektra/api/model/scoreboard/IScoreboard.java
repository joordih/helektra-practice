package dev.voltic.helektra.api.model.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public interface IScoreboard {
    Player getPlayer();
    
    void updateTitle(String title);
    
    void updateLines(String... lines);
    
    void updateLines(List<String> lines);
    
    boolean isDeleted();

    void delete();
}
