package dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;
import dev.voltic.helektra.plugin.model.profile.menu.FriendsMenu;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;

public class FriendsAction implements HotbarAction {

  private final Helektra helektra;

  @Inject
  public FriendsAction(Helektra helektra) {
    this.helektra = helektra;
  }

  @Override
  public String id() {
    return "FRIENDS_MENU";
  }

  @Override
  public void execute(Player player) {
    helektra.getMenuFactory().openMenu(FriendsMenu.class, player);
  }
}
