package dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.cosmetic.menu.CosmeticsMenu;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;

public class CosmeticsAction implements HotbarAction {

  private final Helektra helektra;

  @Inject
  public CosmeticsAction(Helektra helektra) {
    this.helektra = helektra;
  }

  @Override
  public String id() {
    return "COSMETICS_MENU";
  }

  @Override
  public void execute(Player player) {
    helektra.getMenuFactory().openMenu(CosmeticsMenu.class, player);
  }
}
