package dev.voltic.helektra.plugin.listeners;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.Profile;
import dev.voltic.helektra.plugin.model.profile.state.ProfileHotbarService;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.nms.strategy.NmsStrategies;
import dev.voltic.helektra.plugin.nms.strategy.impl.NmsBossBarStrategy;
import dev.voltic.helektra.plugin.utils.BukkitUtils;
import dev.voltic.helektra.plugin.utils.LocationUtil;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.xseries.XPotion;
import jakarta.inject.Inject;
import java.util.Optional;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;

public class PlayerListeners implements Listener {

  private final Helektra helektra;
  private final ProfileStateManager profileStateManager;
  private final IProfileService profileService;

  @Inject
  public PlayerListeners(
    Helektra helektra,
    ProfileStateManager profileStateManager,
    IProfileService profileService,
    ProfileHotbarService hotbarService
  ) {
    this.helektra = helektra;
    this.profileStateManager = profileStateManager;
    this.profileService = profileService;
  }

  @EventHandler
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    var uuid = event.getUniqueId();
    Optional<IProfile> profile = helektra
      .getProfileService()
      .getProfile(uuid)
      .join();

    if (profile.isEmpty()) {
      profile = Optional.of(new Profile(uuid.toString(), event.getName()));
    }

    helektra.getProfileService().saveProfile(profile.get());
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    var uuid = player.getUniqueId();

    Location spawnLocation = LocationUtil.deserialize(
      helektra.getSettingsConfig().getConfig().getString("settings.spawn")
    );
    player.teleport(spawnLocation);

    Bukkit.getScheduler().runTaskLater(
      helektra,
      () -> {
        NmsStrategies.PING.execute(player);
        NmsStrategies.TITLE.execute(
          player,
          "&fWelcome to",
          "&eHelektra Practice",
          10,
          60,
          10
        );
        NmsStrategies.ACTION_BAR.execute(player, "&a¡Good luck!");
        NmsStrategies.BOSS_BAR.execute(
          player,
          "&eHelektra Practice - &6Development Mode",
          1.0f,
          BossBar.Color.WHITE,
          BossBar.Overlay.PROGRESS
        );
      },
      20L
    );

    Optional<IProfile> cachedProfile = profileService.getCachedProfile(uuid);
    IProfile profile;

    if (cachedProfile.isPresent()) {
      profile = cachedProfile.get();
    } else {
      Optional<IProfile> loadedProfile = profileService.getProfile(uuid).join();
      if (loadedProfile.isEmpty()) {
        player.kickPlayer(TranslationUtils.translate("profile.error"));
        return;
      }
      profile = loadedProfile.get();
      profileService.cacheProfile(profile);
    }

    profileStateManager.setState(player, ProfileState.LOBBY);

    player.sendMessage(TranslationUtils.translate("profile.loaded"));
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    var uuid = event.getPlayer().getUniqueId();
    helektra
      .getProfileService()
      .getProfile(uuid)
      .thenAccept(opt ->
        opt.ifPresent(profile ->
          Bukkit.getScheduler().runTaskAsynchronously(helektra, () ->
            helektra.getProfileService().saveProfile(profile)
          )
        )
      );
    NmsBossBarStrategy.remove(event.getPlayer());
  }

  @EventHandler
  public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
    Player player = event.getPlayer();

    if (event.getItem().isSimilar(BukkitUtils.GOLDEN_HEAD)) {
      player.removePotionEffect(XPotion.REGENERATION.get());
      player.addPotionEffect(
        new PotionEffect(XPotion.REGENERATION.get(), 20 * 10, 1)
      );
    }
  }
}
