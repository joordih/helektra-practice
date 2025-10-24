package dev.voltic.helektra.plugin.model.cosmetic.service;

import dev.voltic.helektra.api.model.cosmetic.CosmeticType;
import dev.voltic.helektra.api.model.cosmetic.ICosmetic;
import dev.voltic.helektra.api.model.cosmetic.ICosmeticCategory;
import dev.voltic.helektra.api.model.cosmetic.ICosmeticService;
import dev.voltic.helektra.plugin.model.cosmetic.Cosmetic;
import dev.voltic.helektra.plugin.model.cosmetic.CosmeticCategory;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
public class CosmeticServiceImpl implements ICosmeticService {

  private final JavaPlugin plugin;
  private final Map<String, Cosmetic> cosmeticsById;
  private final Map<CosmeticType, CosmeticCategory> categoriesByType;
  private final Logger logger;

  @Inject
  public CosmeticServiceImpl(JavaPlugin plugin) {
    this.plugin = plugin;
    this.cosmeticsById = new HashMap<>();
    this.categoriesByType = new HashMap<>();
    this.logger = plugin.getLogger();
  }

  @Override
  public void loadCosmetics() {
    cosmeticsById.clear();
    categoriesByType.clear();

    plugin.saveResource("cosmetics.yml", false);
    FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
      new java.io.File(plugin.getDataFolder(), "cosmetics.yml")
    );

    ConfigurationSection categoriesSection = config.getConfigurationSection("categories");
    if (categoriesSection == null) {
      logger.warning("No cosmetics categories found in cosmetics.yml");
      return;
    }

    for (String categoryKey : categoriesSection.getKeys(false)) {
      ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryKey);
      if (categorySection == null) continue;

      CosmeticType type = parseCosmeticType(categorySection.getString("type", ""));
      if (type == null) {
        logger.warning("Invalid cosmetic type for category: " + categoryKey);
        continue;
      }

      String displayName = ColorUtils.translate(categorySection.getString("display-name", categoryKey));
      String iconMaterial = categorySection.getString("icon-material", "CHEST");

      CosmeticCategory category = new CosmeticCategory(type, displayName, iconMaterial);

      ConfigurationSection itemsSection = categorySection.getConfigurationSection("items");
      if (itemsSection != null) {
        for (String itemKey : itemsSection.getKeys(false)) {
          ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
          if (itemSection == null) continue;

          String id = categoryKey + "." + itemKey;
          String itemDisplayName = ColorUtils.translate(itemSection.getString("display-name", itemKey));
          String itemIconMaterial = itemSection.getString("icon-material", "PAPER");
          String particleEffect = itemSection.getString("particle-effect", "FLAME");
          String colorCode = itemSection.getString("color", "#FFFFFF");
          boolean enabled = itemSection.getBoolean("enabled", true);

          Cosmetic cosmetic = new Cosmetic(id, itemDisplayName, type, itemIconMaterial, particleEffect, colorCode, enabled);
          cosmeticsById.put(id, cosmetic);
          category.addCosmetic(cosmetic);
        }
      }

      categoriesByType.put(type, category);
    }

    logger.info("Loaded " + cosmeticsById.size() + " cosmetics across " + categoriesByType.size() + " categories");
  }

  @Override
  public Optional<ICosmetic> getCosmeticById(String id) {
    return Optional.ofNullable(cosmeticsById.get(id));
  }

  @Override
  public List<ICosmetic> getCosmeticsByType(CosmeticType type) {
    return categoriesByType.getOrDefault(type, new CosmeticCategory(type, "", ""))
      .getCosmetics()
      .stream()
      .map(cosmetic -> (ICosmetic) cosmetic)
      .toList();
  }

  @Override
  public List<ICosmeticCategory> getAllCategories() {
    return new ArrayList<>(categoriesByType.values());
  }

  @Override
  public Optional<ICosmeticCategory> getCategoryByType(CosmeticType type) {
    return Optional.ofNullable(categoriesByType.get(type));
  }

  @Override
  public boolean isCosmeticEnabled(String id) {
    return getCosmeticById(id).map(ICosmetic::isEnabled).orElse(false);
  }

  private CosmeticType parseCosmeticType(String typeString) {
    try {
      return CosmeticType.valueOf(typeString.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
