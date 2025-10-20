package dev.voltic.helektra.plugin.nms.util;

import org.bukkit.Bukkit;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class ReflectUtils {

    private static final String VERSION;
    private static final boolean MODERN;
    private static final Map<String, String> CLASS_MAPPINGS = new HashMap<>();

    static {
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = pkg.split("\\.");
        MODERN = parts.length < 4;
        VERSION = MODERN ? "" : parts[3];

        CLASS_MAPPINGS.put("IChatBaseComponent", "net.minecraft.network.chat.IChatBaseComponent");
        CLASS_MAPPINGS.put("ChatMessageType", "net.minecraft.network.chat.ChatMessageType");
        CLASS_MAPPINGS.put("PacketPlayOutChat", "net.minecraft.network.protocol.game.ClientboundSystemChatPacket");
        CLASS_MAPPINGS.put("PacketPlayOutTitle", "net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket");
        CLASS_MAPPINGS.put("PacketPlayOutSubtitle", "net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket");
        CLASS_MAPPINGS.put("ClientboundSetActionBarTextPacket", "net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket");
        CLASS_MAPPINGS.put("ClientboundSystemChatPacket", "net.minecraft.network.protocol.game.ClientboundSystemChatPacket");
    }

    private ReflectUtils() {}

    public static String getServerVersion() {
        return VERSION;
    }

    public static boolean isModern() {
        return MODERN;
    }

    public static Class<?> getNmsClass(String simpleName) {
        try {
            if (!MODERN) {
                return Class.forName("net.minecraft.server." + VERSION + "." + simpleName);
            }
            if (CLASS_MAPPINGS.containsKey(simpleName)) {
                return Class.forName(CLASS_MAPPINGS.get(simpleName));
            }
            String[] guesses = {
                "net.minecraft.network.protocol.game." + simpleName,
                "net.minecraft.network.chat." + simpleName,
                "net.minecraft.server." + simpleName,
                "net.minecraft." + simpleName
            };
            for (String path : guesses) {
                try {
                    return Class.forName(path);
                } catch (ClassNotFoundException ignored) {}
            }
            throw new ClassNotFoundException(simpleName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró clase NMS: " + simpleName, e);
        }
    }

    public static Class<?> tryClasses(String... fqns) {
        for (String fqn : fqns) {
            try {
                return Class.forName(fqn);
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }

    public static Object invoke(Object instance, String method, Object... args) {
        try {
            Method m = findMethod(instance.getClass(), method, args.length);
            m.setAccessible(true);
            return m.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException("Error invocando " + method, e);
        }
    }

    public static Object invokeStatic(Class<?> clazz, String method, Object... args) {
        try {
            Method m = findMethod(clazz, method, args.length);
            m.setAccessible(true);
            return m.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("Error invocando estático " + method, e);
        }
    }

    private static Method findMethod(Class<?> clazz, String name, int paramCount) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount)
                return m;
        }
        throw new RuntimeException("Método no encontrado: " + name + " en " + clazz);
    }

    public static Object getField(Object instance, String field) {
        try {
            Field f = instance.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo campo " + field, e);
        }
    }
}
