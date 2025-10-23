package dev.voltic.helektra.plugin.utils.menu;

import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.sound.MenuSoundUtils;
import fr.mrmicky.fastinv.FastInv;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public abstract class InjectableMenu
  extends FastInv
  implements ConfigurableMenu {

  protected final MenuConfigHelper menuConfig;
  protected final String menuPath;
  private final Map<Integer, MenuItemConfig> itemConfigsBySlot;

  protected InjectableMenu(MenuConfigHelper menuConfig, String menuPath) {
    super(menuConfig.getMenuSize(menuPath), menuConfig.getMenuTitle(menuPath));
    this.menuConfig = menuConfig;
    this.menuPath = menuPath;
    this.itemConfigsBySlot = loadItemConfigs();
  }

  protected InjectableMenu(
    MenuConfigHelper menuConfig,
    String menuPath,
    int size
  ) {
    super(size, menuConfig.getMenuTitle(menuPath));
    this.menuConfig = menuConfig;
    this.menuPath = menuPath;
    this.itemConfigsBySlot = loadItemConfigs();
  }

  @Override
  public abstract void setup(Player player);

  @Override
  public void open(Player player) {
    super.open(player);
    MenuSoundUtils.playOpenSound(player, menuPath);
  }

  @Override
  protected void onClose(InventoryCloseEvent event) {
    super.onClose(event);
    if (event.getPlayer() instanceof Player player) {
      MenuSoundUtils.playCloseSound(player, menuPath);
    }
  }

  @Override
  public void setItem(
    int slot,
    ItemStack item,
    Consumer<InventoryClickEvent> action
  ) {
    if (action == null) {
      super.setItem(slot, item, null);
      return;
    }
    MenuItemConfig soundConfig = itemConfigsBySlot.get(slot);
    Consumer<InventoryClickEvent> wrapped = event -> {
      if (event.getWhoClicked() instanceof Player player) {
        MenuSoundUtils.playItemSound(player, soundConfig, menuPath);
      }
      action.accept(event);
    };
    super.setItem(slot, item, wrapped);
  }

  protected void registerItemSound(int slot, MenuItemConfig config) {
    if (config != null && config.exists()) {
      itemConfigsBySlot.put(slot, config);
    }
  }

  private Map<Integer, MenuItemConfig> loadItemConfigs() {
    Map<Integer, MenuItemConfig> map = new HashMap<>();
    for (String key : menuConfig.getItemKeys(menuPath)) {
      MenuItemConfig config = menuConfig.getItemConfig(menuPath, key);
      if (config.exists()) {
        for (int slot : config.getSlots()) {
          map.put(slot, config);
        }
      }
    }
    return map;
  }
}
