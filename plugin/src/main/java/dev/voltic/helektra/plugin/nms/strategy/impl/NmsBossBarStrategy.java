package dev.voltic.helektra.plugin.nms.strategy.impl;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.nms.NmsStrategy;
import dev.voltic.helektra.plugin.nms.strategy.NmsStrategies;
import dev.voltic.helektra.plugin.nms.util.EnumUtils;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NmsBossBarStrategy implements NmsStrategy {

    private static final Map<UUID, BossBar> ADV_BARS = new ConcurrentHashMap<>();
    private static final Map<UUID, Object> BUKKIT_BARS = new ConcurrentHashMap<>();

    @Override
    public void execute(Player player, Object... args) {
        String title = ColorUtils.translate(args.length > 0 ? String.valueOf(args[0]) : "&eHelektra Practice");
        float progress = parseProgress(args.length > 1 ? args[1] : 1.0f);
        BossBar.Color color = args.length > 2 && args[2] instanceof BossBar.Color ? (BossBar.Color) args[2] : BossBar.Color.WHITE;
        BossBar.Overlay overlay = args.length > 3 && args[3] instanceof BossBar.Overlay ? (BossBar.Overlay) args[3] : BossBar.Overlay.PROGRESS;

        var adventure = Helektra.getInstance().getAdventure();
        if (adventure != null) {
            BossBar bar = ADV_BARS.computeIfAbsent(player.getUniqueId(), id -> {
                BossBar b = BossBar.bossBar(Component.text(title), progress, color, overlay);
                adventure.player(player).showBossBar(b);
                return b;
            });
            bar.name(Component.text(title));
            bar.progress(progress);
            bar.color(color);
            bar.overlay(overlay);
            return;
        }

        if (supportsBukkitBossBar()) {
            try {
                Object bossBar = BUKKIT_BARS.get(player.getUniqueId());
                Class<?> barColorClazz = Class.forName("org.bukkit.boss.BarColor");
                Class<?> barStyleClazz = Class.forName("org.bukkit.boss.BarStyle");
                Class<?> bossBarClazz = Class.forName("org.bukkit.boss.BossBar");
                Object barColor = EnumUtils.getEnumConstant(barColorClazz, mapColor(color));
                Object barStyle = EnumUtils.getEnumConstant(barStyleClazz, mapOverlay(overlay));
                if (bossBar == null) {
                    Method create = Bukkit.class.getMethod("createBossBar", String.class, barColorClazz, barStyleClazz);
                    bossBar = create.invoke(null, title, barColor, barStyle);
                    bossBarClazz.getMethod("addPlayer", Player.class).invoke(bossBar, player);
                    BUKKIT_BARS.put(player.getUniqueId(), bossBar);
                } else {
                    bossBarClazz.getMethod("setTitle", String.class).invoke(bossBar, title);
                    bossBarClazz.getMethod("setColor", barColorClazz).invoke(bossBar, barColor);
                    bossBarClazz.getMethod("setStyle", barStyleClazz).invoke(bossBar, barStyle);
                }
                double clamped = Math.max(0.0, Math.min(1.0, (double) progress));
                bossBarClazz.getMethod("setProgress", double.class).invoke(bossBar, clamped);
                bossBarClazz.getMethod("setVisible", boolean.class).invoke(bossBar, true);
                return;
            } catch (Throwable ignored) {
            }
        }

        try {
            NmsStrategies.ACTION_BAR.execute(player, title);
        } catch (Throwable ignored) {
            player.sendMessage(title);
        }
    }

    public static void remove(Player player) {
        var adventure = Helektra.getInstance().getAdventure();
        BossBar adv = ADV_BARS.remove(player.getUniqueId());
        if (adv != null && adventure != null) {
            adventure.player(player).hideBossBar(adv);
        }
        Object bar = BUKKIT_BARS.remove(player.getUniqueId());
        if (bar != null && supportsBukkitBossBar()) {
            try {
                bar.getClass().getMethod("removePlayer", Player.class).invoke(bar, player);
                bar.getClass().getMethod("removeAll").invoke(bar);
            } catch (Throwable ignored) {
            }
        }
    }

    private static boolean supportsBukkitBossBar() {
        try {
            Class.forName("org.bukkit.boss.BossBar");
            Class.forName("org.bukkit.boss.BarColor");
            Class.forName("org.bukkit.boss.BarStyle");
            Bukkit.class.getMethod("createBossBar", String.class,
                    Class.forName("org.bukkit.boss.BarColor"),
                    Class.forName("org.bukkit.boss.BarStyle"));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static float parseProgress(Object v) {
        if (v == null) return 1.0f;
        if (v instanceof Number) return ((Number) v).floatValue();
        try {
            return Float.parseFloat(v.toString());
        } catch (NumberFormatException e) {
            return 1.0f;
        }
    }

    private static String mapColor(BossBar.Color color) {
        String n = color.name();
        return switch (n) {
            case "PINK" -> "PINK";
            case "BLUE" -> "BLUE";
            case "RED" -> "RED";
            case "GREEN" -> "GREEN";
            case "YELLOW" -> "YELLOW";
            case "PURPLE" -> "PURPLE";
            case "WHITE" -> "WHITE";
            default -> "WHITE";
        };
    }

    private static String mapOverlay(BossBar.Overlay overlay) {
        String n = overlay.name();
        return switch (n) {
            case "PROGRESS" -> "SOLID";
            case "NOTCHED_6" -> "SEGMENTED_6";
            case "NOTCHED_10" -> "SEGMENTED_10";
            case "NOTCHED_12" -> "SEGMENTED_12";
            case "NOTCHED_20" -> "SEGMENTED_20";
            default -> "SOLID";
        };
    }
}
