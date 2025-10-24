package dev.voltic.helektra.plugin.utils.menu;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.sound.MenuSoundUtils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;

@Singleton
public class MenuFactory {

  private final Injector injector;

  @Inject
  public MenuFactory(Injector injector) {
    this.injector = injector;
  }

  public void openMenu(Class<? extends ConfigurableMenu> menuClass, Player player) {
    try {
      ConfigurableMenu instance = injector.getInstance(menuClass);
      instance.setup(player);

      if (instance instanceof DynamicMenu dynamicMenu) {
        dynamicMenu.open(player);
      } else if (instance instanceof FastInv fastInv) {
        fastInv.open(player);
      } else {
        player.sendMessage(TranslationUtils.translate("error.generic"));
        MenuSoundUtils.playErrorSound(player);
        return;
      }

    } catch (Exception e) {
      player.sendMessage(TranslationUtils.translate("error.generic"));
      MenuSoundUtils.playErrorSound(player);
      e.printStackTrace();
    }
  }

  public <T> T getInstance(Class<T> clazz) {
    return injector.getInstance(clazz);
  }
}
