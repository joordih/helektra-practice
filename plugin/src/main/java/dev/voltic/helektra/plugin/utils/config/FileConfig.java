package dev.voltic.helektra.plugin.utils.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class FileConfig {

  private File file;
  private FileConfiguration config;

  public File getFile() {
    return this.file;
  }

  public FileConfiguration getConfig() {
    return this.config;
  }

  public FileConfig(JavaPlugin plugin, String fileName) {
    this.file = new File(plugin.getDataFolder(), fileName);
    if (!this.file.exists()) {
      this.file.getParentFile().mkdirs();
      if (plugin.getResource(fileName) == null) {
        try {
          this.file.createNewFile();
        } catch (IOException var4) {
          plugin.getLogger().severe("Failed to create new file " + fileName);
        }
      } else {
        plugin.saveResource(fileName, false);
      }
    }

    this.config = YamlConfiguration.loadConfiguration(this.file);
  }

  public void save() {
    try {
      this.config.save(this.file);
    } catch (IOException var2) {
      Bukkit.getLogger().severe("Could not save config file " + this.file.toString());
      var2.printStackTrace();
    }
  }
}
