package dev.voltic.helektra.plugin.listeners;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.Profile;
import dev.voltic.helektra.plugin.nms.NmsStrategies;
import dev.voltic.helektra.plugin.nms.strategy.impl.NmsBossBarStrategy;
import jakarta.inject.Inject;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class PlayerListeners implements Listener {

    private final Helektra helektra;

    @Inject
    public PlayerListeners(Helektra helektra) {
        this.helektra = helektra;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        var uuid = event.getUniqueId();
        Optional<IProfile> profile = helektra.getProfileService().getProfile(uuid).join();

        if (profile.isEmpty()) {
            profile = Optional.of(new Profile(uuid.toString(), event.getName()));
        }

        helektra.getProfileService().saveProfile(profile.get());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(helektra, () -> {
            NmsStrategies.PING.execute(player);
            NmsStrategies.TITLE.execute(player, "&fBienvenido a", "&eHelektra Practice", 10, 60, 10);
            NmsStrategies.ACTION_BAR.execute(player, "&a¡Buena suerte!");
            NmsStrategies.BOSS_BAR.execute(player,
                    "&eHelektra Practice - &cModo Desarrollo",
                    1.0f,
                    BossBar.Color.WHITE,
                    BossBar.Overlay.PROGRESS);
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        helektra.getProfileService().getProfile(uuid).thenAccept(opt -> opt.ifPresent(profile ->
                Bukkit.getScheduler().runTaskAsynchronously(helektra, () ->
                        helektra.getProfileService().saveProfile(profile))
        ));
        NmsBossBarStrategy.remove(event.getPlayer());
    }
}
