package dev.voltic.helektra.plugin.utils;

import dev.voltic.helektra.api.model.profile.LobbyTime;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.nms.NmsStrategies;
import dev.voltic.helektra.plugin.utils.xseries.XPotion;
import dev.voltic.helektra.plugin.utils.xseries.XSound;
import dev.voltic.helektra.plugin.utils.xseries.particles.ParticleDisplay;
import dev.voltic.helektra.plugin.utils.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class BukkitUtils {

    private static final long DAY_TIME = 1000L;
    private static final long AFTERNOON_TIME = 9000L;
    private static final long NIGHT_TIME = 13000L;

    public static void setPlayerTimeSmoothly(Player player, LobbyTime lobbyTime) {
        long targetTime = getTimeFromLobbyTime(lobbyTime);
        
        PotionEffect blindness = new PotionEffect(XPotion.BLINDNESS.getPotionEffectType(), 60, 1, false, false);
        PotionEffect slowness = new PotionEffect(XPotion.SLOWNESS.getPotionEffectType(), 60, 3, false, false);
        
        player.addPotionEffect(blindness);
        player.addPotionEffect(slowness);
        
        spawnTeleportParticles(player, lobbyTime);
        playTeleportSound(player);
        fadeIn(player, lobbyTime);
        
        Bukkit.getScheduler().runTaskLater(Helektra.getInstance(), () -> {
            player.setPlayerTime(targetTime, false);
            spawnArrivalParticles(player, lobbyTime);
        }, 20L);
        
        Bukkit.getScheduler().runTaskLater(Helektra.getInstance(), () -> {
            fadeOut(player, lobbyTime);
            playArrivalSound(player);
        }, 40L);
    }

    private static void spawnTeleportParticles(Player player, LobbyTime time) {
        Location loc = player.getLocation().clone();
        
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            Bukkit.getScheduler().runTaskLater(Helektra.getInstance(), () -> {
                Location particleLoc = loc.clone().add(0, 0.5 + (finalI * 0.5), 0);
                
                ParticleDisplay.of(XParticle.PORTAL)
                        .withLocation(particleLoc)
                        .offset(0.5, 0.5, 0.5)
                        .withCount(30)
                        .withExtra(1.0)
                        .forceSpawn(true)
                        .spawn();
                        
                ParticleDisplay.of(XParticle.SMOKE)
                        .withLocation(particleLoc)
                        .offset(0.3, 0.3, 0.3)
                        .withCount(10)
                        .withExtra(0.05)
                        .forceSpawn(true)
                        .spawn();
            }, i * 3L);
        }
    }

    private static void spawnArrivalParticles(Player player, LobbyTime time) {
        Location loc = player.getLocation().clone().add(0, 1, 0);
        
        ParticleDisplay spiral = ParticleDisplay.of(XParticle.END_ROD)
                .withLocation(loc)
                .offset(0, 0, 0)
                .withCount(1)
                .forceSpawn(true);
                
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            Bukkit.getScheduler().runTaskLater(Helektra.getInstance(), () -> {
                double angle = finalI * Math.PI / 5;
                double radius = 1.5 - (finalI * 0.05);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = finalI * 0.1;
                
                spiral.withLocation(loc.clone().add(x, y, z)).spawn();
            }, i);
        }
        
        Bukkit.getScheduler().runTaskLater(Helektra.getInstance(), () -> {
            ParticleDisplay burst;
            switch (time) {
                case DAY -> {
                    burst = ParticleDisplay.of(XParticle.HAPPY_VILLAGER)
                            .withLocation(loc)
                            .offset(1.0, 0.5, 1.0)
                            .withCount(25)
                            .withExtra(0)
                            .forceSpawn(true);
                }
                case AFTERNOON -> {
                    burst = ParticleDisplay.of(XParticle.FIREWORK)
                            .withLocation(loc)
                            .offset(1.0, 0.5, 1.0)
                            .withCount(20)
                            .withExtra(0.1)
                            .forceSpawn(true);
                }
                case NIGHT -> {
                    Color color = Color.fromRGB(138, 43, 226);
                    java.awt.Color awtColor = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
                    burst = ParticleDisplay.of(XParticle.DUST)
                            .withLocation(loc)
                            .offset(1.0, 0.5, 1.0)
                            .withCount(30)
                            .withColor(awtColor, 1.5f)
                            .forceSpawn(true);
                }
                default -> {
                    burst = ParticleDisplay.of(XParticle.CLOUD)
                            .withLocation(loc)
                            .offset(1.0, 0.5, 1.0)
                            .withCount(20);
                }
            }
            burst.spawn();
        }, 20L);
    }

    private static void playTeleportSound(Player player) {
        try {
            XSound.ENTITY_ENDERMAN_TELEPORT.play(player, 1.0f, 0.8f);
        } catch (Exception e) {
            XSound.ENTITY_PLAYER_LEVELUP.play(player, 1.0f, 0.8f);
        }
        
        Bukkit.getScheduler().runTaskLater(Helektra.getInstance(), () -> {
            try {
                XSound.BLOCK_PORTAL_TRAVEL.play(player, 0.3f, 1.5f);
            } catch (Exception ignored) {}
        }, 5L);
    }

    private static void playArrivalSound(Player player) {
        try {
            XSound.ENTITY_PLAYER_LEVELUP.play(player, 0.8f, 1.2f);
        } catch (Exception e) {
            try {
                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player, 0.8f, 1.2f);
            } catch (Exception ignored) {}
        }
        
        Bukkit.getScheduler().runTaskLater(Helektra.getInstance(), () -> {
            try {
                XSound.BLOCK_BEACON_POWER_SELECT.play(player, 0.5f, 1.8f);
            } catch (Exception ignored) {}
        }, 5L);
    }

    private static void fadeIn(Player player, LobbyTime time) {
        String subtitle = switch (time) {
            case DAY -> TranslationUtils.translate("time.traveling-day");
            case AFTERNOON -> TranslationUtils.translate("time.traveling-afternoon");
            case NIGHT -> TranslationUtils.translate("time.traveling-night");
        };
        
        try {
            String title = TranslationUtils.translate("time.teleporting");
            NmsStrategies.TITLE.execute(player, title, subtitle, 10, 30, 10);
        } catch (Exception ignored) {}
    }

    private static void fadeOut(Player player, LobbyTime time) {
        String title = switch (time) {
            case DAY -> TranslationUtils.translate("time.arrived-day");
            case AFTERNOON -> TranslationUtils.translate("time.arrived-afternoon");
            case NIGHT -> TranslationUtils.translate("time.arrived-night");
        };
        
        String subtitle = switch (time) {
            case DAY -> TranslationUtils.translate("time.subtitle-day");
            case AFTERNOON -> TranslationUtils.translate("time.subtitle-afternoon");
            case NIGHT -> TranslationUtils.translate("time.subtitle-night");
        };
        
        try {
            NmsStrategies.TITLE.execute(player, title, subtitle, 10, 40, 15);
        } catch (Exception ignored) {}
    }

    public static long getTimeFromLobbyTime(LobbyTime lobbyTime) {
        return switch (lobbyTime) {
            case DAY -> DAY_TIME;
            case AFTERNOON -> AFTERNOON_TIME;
            case NIGHT -> NIGHT_TIME;
        };
    }

    public static void resetPlayerTime(Player player) {
        player.resetPlayerTime();
    }
}