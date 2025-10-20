package dev.voltic.helektra.plugin.nms.util;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;

public class PacketUtils {
    
    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = ReflectUtils.invoke(player, "getHandle");
            Object connection;

            try {
                connection = ReflectUtils.getField(handle, "playerConnection");
                Method send = connection.getClass().getMethod("sendPacket", ReflectUtils.getNmsClass("Packet"));
                send.invoke(connection, packet);
            } catch (Exception e) {
                connection = ReflectUtils.getField(handle, "b");
                Method send = connection.getClass().getMethod("a", ReflectUtils.getNmsClass("network.protocol.Packet"));
                send.invoke(connection, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPing(Player player) {
        try {
            Object handle = ReflectUtils.invoke(player, "getHandle");
            try {
                return (int) ReflectUtils.getField(handle, "ping");
            } catch (Exception ignored) {
                return (int) ReflectUtils.getField(handle, "e");
            }
        } catch (Exception e) {
            return -1;
        }
    }
}
