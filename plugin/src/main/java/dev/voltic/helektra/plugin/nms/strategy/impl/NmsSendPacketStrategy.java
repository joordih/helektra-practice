package dev.voltic.helektra.plugin.nms.strategy.impl;

import dev.voltic.helektra.plugin.nms.NmsStrategy;
import dev.voltic.helektra.plugin.nms.util.PacketUtils;
import org.bukkit.entity.Player;

public final class NmsSendPacketStrategy implements NmsStrategy {

    @Override
    public void execute(Player player, Object... args) {
        if (args.length == 0) return;
        PacketUtils.sendPacket(player, args[0]);
    }
}
