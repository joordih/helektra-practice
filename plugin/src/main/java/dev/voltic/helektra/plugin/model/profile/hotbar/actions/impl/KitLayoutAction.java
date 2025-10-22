package dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.kit.layout.menu.KitLayoutSelectorMenu;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.entity.Player;

@Singleton
public class KitLayoutAction implements HotbarAction {

  private final Helektra helektra;

  @Inject
  public KitLayoutAction(Helektra helektra) {
    this.helektra = helektra;
  }

  @Override
  public String id() {
    return "KIT_LAYOUT";
  }

  @Override
  public void execute(Player player) {
    helektra.getMenuFactory().openMenu(KitLayoutSelectorMenu.class, player);
  }
}
