package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.SoundConfigHelper;
import dev.voltic.helektra.plugin.utils.menu.MenuFactory;
import dev.voltic.helektra.plugin.utils.sound.MenuSoundUtils;
import dev.voltic.helektra.plugin.utils.sound.PlayerSoundUtils;

public class UtilsModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(MenuConfigHelper.class).in(Scopes.SINGLETON);
    bind(SoundConfigHelper.class).in(Scopes.SINGLETON);
    bind(MenuFactory.class).in(Scopes.SINGLETON);
    bind(MenuSoundUtils.class).asEagerSingleton();
    bind(PlayerSoundUtils.class).asEagerSingleton();
  }
}
