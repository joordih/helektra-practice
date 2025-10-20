package dev.voltic.helektra.plugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private static final boolean SUPPORTS_HEX = supportsHexColors();
    private static final boolean SUPPORTS_BUKKIT_COLOR = supportsBukkitColor();

    private ColorUtils() {
    }

    public static String translate(String input) {
        if (input == null)
            return "";

        String output = input;

        if (SUPPORTS_HEX) {
            Matcher matcher = HEX_PATTERN.matcher(output);
            while (matcher.find()) {
                String color = matcher.group();
                try {
                    output = output.replace(color, net.md_5.bungee.api.ChatColor.of(color).toString());
                } catch (Exception ignored) {
                }
            }
        }

        return ChatColor.translateAlternateColorCodes('&', output);
    }

    public static List<String> translate(List<String> lines) {
        List<String> toReturn = new ArrayList<>();

        for (String line : lines) {
            toReturn.add(translate(line));
        }

        return toReturn;
    }

    public static List<String> translate(String[] lines) {
        List<String> toReturn = new ArrayList<>();

        for (String line : lines) {
            if (line != null) {
                toReturn.add(translate(line));
            }
        }

        return toReturn;
    }

    public static String stripColor(String text) {
        return ChatColor.stripColor(text);
    }

    public static Color getColor(String hexOrName) {
        if (hexOrName == null || hexOrName.isEmpty())
            return Color.WHITE;

        try {
            if (SUPPORTS_BUKKIT_COLOR && hexOrName.startsWith("#")) {
                java.awt.Color awt = java.awt.Color.decode(hexOrName);
                return Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());
            }

            return (Color) Color.class.getField(hexOrName.toUpperCase()).get(null);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    private static boolean supportsHexColors() {
        try {
            Class<?> chatColorClass = Class.forName("net.md_5.bungee.api.ChatColor");
            Method method = chatColorClass.getMethod("of", String.class);
            return method != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean supportsBukkitColor() {
        try {
            Method method = Color.class.getMethod("fromRGB", int.class, int.class, int.class);
            return method != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean isVersionAtLeast(int major) {
        try {
            String version = Bukkit.getBukkitVersion();
            int detected = Integer.parseInt(version.split("\\.")[1]);
            return detected >= major;
        } catch (Exception e) {
            return false;
        }
    }

    public static Component legacyToComponent(String text) {
        try {
            return Component.text(ChatColor.stripColor(translate(text))).color(TextColor.color(255, 255, 255));
        } catch (Throwable e) {
            return Component.text(ChatColor.stripColor(translate(text)));
        }
    }
}
