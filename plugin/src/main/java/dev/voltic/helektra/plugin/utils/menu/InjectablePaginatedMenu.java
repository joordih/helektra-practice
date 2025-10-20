package dev.voltic.helektra.plugin.utils.menu;

import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import fr.mrmicky.fastinv.PaginatedFastInv;
import org.bukkit.entity.Player;

public abstract class InjectablePaginatedMenu
  extends PaginatedFastInv
  implements ConfigurableMenu {

  protected final MenuConfigHelper menuConfig;
  protected final String menuPath;

  protected InjectablePaginatedMenu(
    MenuConfigHelper menuConfig,
    String menuPath
  ) {
    super(menuConfig.getMenuSize(menuPath), menuConfig.getMenuTitle(menuPath));
    this.menuConfig = menuConfig;
    this.menuPath = menuPath;
  }

  @Override
  public abstract void setup(Player player);
}
