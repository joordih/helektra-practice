package dev.voltic.helektra.plugin.model.cosmetic.listener;

import dev.voltic.helektra.api.model.cosmetic.CosmeticType;
import dev.voltic.helektra.api.model.cosmetic.ICosmetic;
import dev.voltic.helektra.api.model.cosmetic.ICosmeticService;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.utils.xseries.particles.ParticleDisplay;
import dev.voltic.helektra.plugin.utils.xseries.particles.XParticle;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.awt.Color;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Singleton
public class CosmeticEffectHelper {

  private final IProfileService profileService;
  private final ICosmeticService cosmeticService;

  @Inject
  public CosmeticEffectHelper(IProfileService profileService, ICosmeticService cosmeticService) {
    this.profileService = profileService;
    this.cosmeticService = cosmeticService;
  }

  public void applyTrailEffect(Player player, CosmeticType type, Location location) {
    Optional<IProfile> profileOpt = profileService.getCachedProfile(player.getUniqueId());
    if (profileOpt.isEmpty()) return;

    IProfile profile = profileOpt.get();
    String selectedCosmeticId = profile.getCosmetics().getSelectedCosmeticId(type);
    if (selectedCosmeticId == null) return;

    Optional<ICosmetic> cosmeticOpt = cosmeticService.getCosmeticById(selectedCosmeticId);
    if (cosmeticOpt.isEmpty() || !cosmeticOpt.get().isEnabled()) return;

    ICosmetic cosmetic = cosmeticOpt.get();
    spawnParticle(location, cosmetic.getParticleEffect(), cosmetic.getColorCode());
  }

  private void spawnParticle(Location location, String particleEffect, String colorCode) {
    try {
      ParticleDisplay display = ParticleDisplay.of(XParticle.valueOf(particleEffect));
      display.withLocation(location);
      display.withCount(1);
      display.offset(0.02, 0.02, 0.02);
      display.withExtra(0);

      if (colorCode != null && !colorCode.isEmpty()) {
        Color color = Color.decode(colorCode);
        display.withColor(color, 0.6f);
      }

      display.spawn();
    } catch (Exception ignored) {
    }
  }
}
