package dev.voltic.helektra.plugin.utils;

import com.google.inject.Singleton;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.config.FileConfig;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

@Singleton
public class MenuConfigHelper {

  private final FileConfig menusConfig;

  @Inject
  public MenuConfigHelper() {
    this.menusConfig = Helektra.getInstance().getMenusConfig();
  }

  public String getMenuTitle(@NotNull String menuPath) {
    String title = menusConfig
      .getConfig()
      .getString(menuPath + ".title", "&7Menu");
    return ColorUtils.translate(title);
  }

  public int getMenuSize(@NotNull String menuPath) {
    int size = menusConfig.getConfig().getInt(menuPath + ".size", 27);
    if (size < 9) size = 9;
    if (size > 54) size = 54;
    if (size % 9 != 0) size = ((size / 9) + 1) * 9;
    return size;
  }

  public MenuItemConfig getItemConfig(
    @NotNull String menuPath,
    @NotNull String itemKey
  ) {
    String fullPath = menuPath + ".items." + itemKey;
    ConfigurationSection section = menusConfig
      .getConfig()
      .getConfigurationSection(fullPath);

    if (section == null) {
      return new MenuItemConfig(itemKey);
    }

    return new MenuItemConfig(itemKey, section);
  }

  public int getInt(@NotNull String path, int defaultValue) {
    return menusConfig.getConfig().getInt(path, defaultValue);
  }

  public boolean contains(@NotNull String path) {
    return menusConfig.getConfig().contains(path);
  }

  public List<String> getItemKeys(@NotNull String menuPath) {
    ConfigurationSection itemsSection = menusConfig
      .getConfig()
      .getConfigurationSection(menuPath + ".items");
    if (itemsSection == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(itemsSection.getKeys(false));
  }

  public String getString(@NotNull String path, @NotNull String defaultValue) {
    return ColorUtils.translate(
      menusConfig.getConfig().getString(path, defaultValue)
    );
  }

  public List<String> getStringList(@NotNull String path) {
    return menusConfig
      .getConfig()
      .getStringList(path)
      .stream()
      .map(ColorUtils::translate)
      .collect(Collectors.toList());
  }

  public String replacePlaceholders(
    @NotNull String text,
    @NotNull Map<String, String> placeholders
  ) {
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      text = text.replace("{" + entry.getKey() + "}", entry.getValue());
    }
    return text;
  }

  public List<String> replacePlaceholders(
    @NotNull List<String> texts,
    @NotNull Map<String, String> placeholders
  ) {
    return texts
      .stream()
      .map(text -> replacePlaceholders(text, placeholders))
      .collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  public static class MenuItemConfig {

    private final String key;
    private final ConfigurationSection section;

    public MenuItemConfig(String key) {
      this.key = key;
      this.section = null;
    }

    public MenuItemConfig(String key, ConfigurationSection section) {
      this.key = key;
      this.section = section;
    }

    public int getPosition() {
      return section != null ? section.getInt("position", 0) : 0;
    }

    public Material getMaterial() {
      if (section == null) return getDefaultMaterial();
      String materialName = section.getString("material", "STONE");
      return parseMaterial(materialName);
    }

    public Material getMaterial(boolean enabled) {
      if (section == null) return getDefaultMaterial();
      String key = enabled ? "material-enabled" : "material-disabled";
      String materialName = section.getString(key);

      if (materialName == null) {
        materialName = section.getString("material", "STONE");
      }

      return parseMaterial(materialName);
    }

    public String getName() {
      if (section == null) return "";
      return ColorUtils.translate(section.getString("name", ""));
    }

    public String getName(boolean enabled) {
      if (section == null) return "";
      String key = enabled ? "enabled-name" : "disabled-name";
      String name = section.getString(key);

      if (name == null) {
        name = section.getString("name", "");
      }

      return ColorUtils.translate(name);
    }

    public List<String> getLore() {
      if (section == null) return new ArrayList<>();
      return section
        .getStringList("lore")
        .stream()
        .map(ColorUtils::translate)
        .collect(Collectors.toList());
    }

    public List<String> getLore(Map<String, String> placeholders) {
      List<String> lore = getLore();
      return lore
        .stream()
        .map(line -> {
          for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            line = line.replace("{" + entry.getKey() + "}", entry.getValue());
          }
          return line;
        })
        .collect(Collectors.toList());
    }

    public String getString(String key) {
      if (section == null) return "";
      return ColorUtils.translate(section.getString(key, ""));
    }

    public String getString(String key, String defaultValue) {
      if (section == null) return ColorUtils.translate(defaultValue);
      return ColorUtils.translate(section.getString(key, defaultValue));
    }

    public int getInt(String key, int defaultValue) {
      if (section == null) return defaultValue;
      return section.getInt(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
      if (section == null) return defaultValue;
      return section.getBoolean(key, defaultValue);
    }

    public String getMessage(String messageKey) {
      if (section == null) return "";
      String path = "messages." + messageKey;
      return ColorUtils.translate(section.getString(path, ""));
    }

    public boolean exists() {
      return section != null;
    }

    private Material parseMaterial(String materialName) {
      if (materialName == null || materialName.isEmpty()) {
        return getDefaultMaterial();
      }

      materialName = materialName.toUpperCase().trim();

      Optional<XMaterial> xMaterialOpt = XMaterial.matchXMaterial(materialName);

      if (xMaterialOpt.isPresent()) {
        XMaterial xMaterial = xMaterialOpt.get();

        if (xMaterial.isSupported()) {
          Material material = xMaterial.get();
          if (material != null) {
            return material;
          }
        }
      }

      try {
        Material bukkitMaterial = Material.getMaterial(materialName);
        if (bukkitMaterial != null) {
          return bukkitMaterial;
        }
      } catch (Exception ignored) {}

      if (Helektra.getInstance().getLogger() != null) {
        Helektra.getInstance()
          .getLogger()
          .warning(
            "Could not find material '" +
              materialName +
              "', using STONE as fallback"
          );
      }

      return getDefaultMaterial();
    }

    private Material getDefaultMaterial() {
      return Material.STONE;
    }
  }
}
