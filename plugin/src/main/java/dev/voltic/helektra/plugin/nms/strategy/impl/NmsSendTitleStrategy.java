package dev.voltic.helektra.plugin.nms.strategy.impl;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.nms.NmsStrategy;
import dev.voltic.helektra.plugin.nms.util.EnumUtils;
import dev.voltic.helektra.plugin.nms.util.PacketUtils;
import dev.voltic.helektra.plugin.nms.util.ReflectUtils;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Duration;

public final class NmsSendTitleStrategy implements NmsStrategy {

    @Override
    public void execute(Player player, Object... args) {
        if (args.length < 5) return;

        String title = ColorUtils.translate(String.valueOf(args[0]));
        String subtitle = ColorUtils.translate(String.valueOf(args[1]));
        int fadeIn = ((Number) args[2]).intValue();
        int stay = ((Number) args[3]).intValue();
        int fadeOut = ((Number) args[4]).intValue();

        if (tryAdventure(player, title, subtitle, fadeIn, stay, fadeOut)) return;
        if (trySpigot(player, title, subtitle, fadeIn, stay, fadeOut)) return;
        sendLegacy(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    private boolean tryAdventure(Player player, String title, String subtitle, int fi, int st, int fo) {
        try {
            String version = Bukkit.getBukkitVersion();
            if (version.startsWith("1.20.5") || version.startsWith("1.21"))
                return false;

            var audiences = Helektra.getInstance().getAdventure();
            if (audiences == null) return false;

            Title.Times times = Title.Times.times(
                    Duration.ofMillis(fi * 50L),
                    Duration.ofMillis(st * 50L),
                    Duration.ofMillis(fo * 50L)
            );
            audiences.player(player)
                    .showTitle(Title.title(Component.text(title), Component.text(subtitle), times));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean trySpigot(Player player, String title, String subtitle, int fi, int st, int fo) {
        try {
            Method sendTitle = player.getClass().getMethod(
                    "sendTitle", String.class, String.class, int.class, int.class, int.class);
            sendTitle.invoke(player, title, subtitle, fi, st, fo);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void sendLegacy(Player player, String title, String subtitle, int fi, int st, int fo) {
        try {
            Class<?> iComp = ReflectUtils.getNmsClass("IChatBaseComponent");
            Class<?> serializer = iComp.getDeclaredClasses()[0];
            Object main = ReflectUtils.invokeStatic(serializer, "a", "{\"text\":\"" + title + "\"}");
            Object sub = ReflectUtils.invokeStatic(serializer, "a", "{\"text\":\"" + subtitle + "\"}");
            Class<?> packetTitle = ReflectUtils.getNmsClass("PacketPlayOutTitle");
            Class<?> enumClass = packetTitle.getDeclaredClasses()[0];

            Object titleEnum = EnumUtils.getEnumConstant(enumClass, "TITLE");
            Object subEnum = EnumUtils.getEnumConstant(enumClass, "SUBTITLE");

            Constructor<?> cTitle = packetTitle.getConstructor(enumClass, iComp);
            Object pTitle = cTitle.newInstance(titleEnum, main);
            Object pSub = cTitle.newInstance(subEnum, sub);

            try {
                Constructor<?> cTimes = packetTitle.getConstructor(int.class, int.class, int.class);
                Object pTimes = cTimes.newInstance(fi, st, fo);
                PacketUtils.sendPacket(player, pTimes);
            } catch (Throwable ignored) {}

            PacketUtils.sendPacket(player, pTitle);
            PacketUtils.sendPacket(player, pSub);
        } catch (Throwable ignored) {}
    }
}
