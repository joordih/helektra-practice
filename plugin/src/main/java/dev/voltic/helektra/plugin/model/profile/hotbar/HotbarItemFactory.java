package dev.voltic.helektra.plugin.model.profile.hotbar;

import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

@Singleton
public class HotbarItemFactory {
  public Optional<ItemStack> create(String materialKey, String name, List<String> lore) {
    ItemStack base = resolveMaterial(materialKey).orElse(null);
    if (base == null)
      return Optional.empty();

    ItemBuilder builder = new ItemBuilder(base);
    if (name != null && !name.isBlank()) {
      builder.name(ColorUtils.translate(name));
    }

    if (lore != null && !lore.isEmpty()) {
      builder.lore(ColorUtils.translate(lore));
    }

    return Optional.of(builder.build());
  }

  private Optional<ItemStack> resolveMaterial(String materialKey) {
    String key = materialKey == null || materialKey.isBlank() ? "PAPER" : materialKey;
    Optional<XMaterial> material = XMaterial.matchXMaterial(key);

    if (material.isPresent()) {
      ItemStack parsed = material.get().parseItem();
      if (parsed != null) {
        return Optional.of(parsed);
      }
    }

    ItemStack fallback = XMaterial.PAPER.parseItem();
    if (fallback == null) {
      return Optional.empty();
    }

    return Optional.of(fallback);
  }
}
