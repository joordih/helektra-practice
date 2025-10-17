package dev.voltic.helektra.plugin.utils.menu;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;

@Singleton
public class MenuFactory {

  private final Injector injector;

  @Inject
  public MenuFactory(Injector injector) {
    this.injector = injector;
  }

  public <T extends FastInv & ConfigurableMenu> void openMenu(
    Class<T> menuClass,
    Player player
  ) {
    try {
      T menu = injector.getInstance(menuClass);
      menu.setup(player);
      menu.open(player);
    } catch (Exception e) {
      player.sendMessage(TranslationUtils.translate("error.generic"));
      e.printStackTrace();
    }
  }
}
