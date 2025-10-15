package dev.voltic.helektra.plugin.nms;

import dev.voltic.helektra.api.strategy.Strategy;
import org.bukkit.entity.Player;

public interface NmsStrategy extends Strategy {
    void execute(Player player, Object... args);
}
