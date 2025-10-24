package dev.voltic.helektra.plugin.model.cosmetic.commands;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.cosmetic.menu.CosmeticsMenu;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = { "cosmetics", "cosm" })
public class CosmeticsCommand implements CommandClass {

  private final Helektra helektra;

  @Inject
  public CosmeticsCommand(Helektra helektra) {
    this.helektra = helektra;
  }

  @Command(names = { "" })
  public void mainCommand(@Sender Player sender) {
    helektra.getMenuFactory().openMenu(CosmeticsMenu.class, sender);
  }
}
