package dev.voltic.helektra.plugin.nms.strategy.impl;

import org.bukkit.entity.Player;

import dev.voltic.helektra.plugin.nms.NmsStrategy;
import dev.voltic.helektra.plugin.nms.util.PacketUtils;
import dev.voltic.helektra.plugin.utils.ColorUtils;

public final class NmsPingStrategy implements NmsStrategy {

    @Override
    public void execute(Player player, Object... args) {
        int ping = PacketUtils.getPing(player);
        player.sendMessage(ColorUtils.translate("&7Tu ping: &a" + ping + "ms"));
    }
}
