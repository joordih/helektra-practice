package dev.voltic.helektra.plugin.nms.strategy.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.entity.Player;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.nms.NmsStrategy;
import dev.voltic.helektra.plugin.nms.util.EnumUtils;
import dev.voltic.helektra.plugin.nms.util.PacketUtils;
import dev.voltic.helektra.plugin.nms.util.ReflectUtils;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;

public final class NmsActionBarStrategy implements NmsStrategy {

    @Override
    public void execute(Player player, Object... args) {
        if (args.length == 0) return;
        String message = ColorUtils.translate(String.valueOf(args[0]));
        
        if (tryPaperNative(player, message)) return;
        if (tryBukkitAudiences(player, message)) return;
        if (trySpigotApi(player, message)) return;
        sendViaReflection(player, message);
    }

    private boolean tryPaperNative(Player player, String msg) {
        try {
            Method sendActionBar = player.getClass().getMethod("sendActionBar", Component.class);
            sendActionBar.invoke(player, Component.text(msg));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private boolean tryBukkitAudiences(Player player, String msg) {
        try {
            var audiences = Helektra.getInstance().getAdventure();
            if (audiences == null) {
                return false;
            }
            audiences.player(player).sendActionBar(Component.text(msg));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private boolean trySpigotApi(Player player, String msg) {
        try {
            Method spigot = player.getClass().getMethod("spigot");
            Object spigotObj = spigot.invoke(player);
            Class<?> chatMsgType = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Class<?> baseArray = Class.forName("[Lnet.md_5.bungee.api.chat.BaseComponent;");
            Method sendMsg = spigotObj.getClass().getMethod("sendMessage", chatMsgType, baseArray);
            Class<?> componentClazz = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Object component = componentClazz.getConstructor(String.class).newInstance(msg);
            Object type = EnumUtils.getEnumConstant(chatMsgType, "ACTION_BAR");
            sendMsg.invoke(spigotObj, type, new Object[]{new Object[]{component}});
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private void sendViaReflection(Player player, String msg) {
        try {
            Class<?> iComp = ReflectUtils.getNmsClass("IChatBaseComponent");
            Class<?> serializer = findSerializer(iComp);
            Object text = ReflectUtils.invokeStatic(serializer, "a", "{\"text\":\"" + msg + "\"}");

            Class<?> packet = ReflectUtils.tryClasses(
                "net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket",
                "net.minecraft.network.protocol.common.ClientboundSetActionBarTextPacket",
                "net.minecraft.network.protocol.game.PacketPlayOutTitle"
            );

            if (packet != null && packet.getSimpleName().contains("ClientboundSetActionBarTextPacket")) {
                Constructor<?> ctor = packet.getConstructor(iComp);
                Object obj = ctor.newInstance(text);
                PacketUtils.sendPacket(player, obj);
                return;
            }

            try {
                Class<?> pktTitle = ReflectUtils.getNmsClass("PacketPlayOutTitle");
                Class<?> enumClass = pktTitle.getDeclaredClasses()[0];
                Object actionEnum = EnumUtils.getEnumConstant(enumClass, "ACTIONBAR");
                if (actionEnum == null)
                    actionEnum = EnumUtils.getEnumConstant(enumClass, "ACTIONBAR_TEXT");
                Constructor<?> ctor = pktTitle.getConstructor(enumClass, iComp);
                Object obj = ctor.newInstance(actionEnum, text);
                PacketUtils.sendPacket(player, obj);
                return;
            } catch (Throwable ignored) {}

            String version = ReflectUtils.getServerVersion();
            Class<?> pktChat = ReflectUtils.getNmsClass("PacketPlayOutChat");
            if (version.startsWith("v1_8")) {
                Constructor<?> c = pktChat.getConstructor(iComp, byte.class);
                Object obj = c.newInstance(text, (byte) 2);
                PacketUtils.sendPacket(player, obj);
                return;
            }
            try {
                Class<?> chatType = ReflectUtils.getNmsClass("ChatMessageType");
                Object gameInfo = EnumUtils.getEnumConstant(chatType, "GAME_INFO");
                Constructor<?> c = pktChat.getConstructor(iComp, chatType, UUID.class);
                Object obj = c.newInstance(text, gameInfo, player.getUniqueId());
                PacketUtils.sendPacket(player, obj);
                return;
            } catch (NoSuchMethodException ignored) {}
            Constructor<?> c = pktChat.getConstructor(iComp, int.class);
            Object obj = c.newInstance(text, 2);
            PacketUtils.sendPacket(player, obj);
        } catch (Throwable ignored) {}
    }

    private Class<?> findSerializer(Class<?> iComp) {
        for (Class<?> c : iComp.getDeclaredClasses()) {
            try {
                Method a = c.getDeclaredMethod("a", String.class);
                if ((a.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0) return c;
            } catch (Throwable ignored) {}
        }
        return iComp.getDeclaredClasses()[0];
    }
}
