package dev.voltic.helektra.plugin.utils.menu;

import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;

public abstract class InjectableMenu
  extends FastInv
  implements ConfigurableMenu {

  protected final MenuConfigHelper menuConfig;
  protected final String menuPath;

  protected InjectableMenu(MenuConfigHelper menuConfig, String menuPath) {
    super(menuConfig.getMenuSize(menuPath), menuConfig.getMenuTitle(menuPath));
    this.menuConfig = menuConfig;
    this.menuPath = menuPath;
  }

  @Override
  public abstract void setup(Player player);
}
