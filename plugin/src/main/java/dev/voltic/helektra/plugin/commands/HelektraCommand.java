package dev.voltic.helektra.plugin.commands;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.LocationUtil;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = "helektra")
public class HelektraCommand implements CommandClass {

  private final Helektra helektra;

  @Inject
  public HelektraCommand(Helektra helektra) {
    this.helektra = helektra;
  }

  @Command(names = "")
  public void mainCommand(@Sender Player player) {}

  @Command(names = "setspawn")
  public void setSpawn(@Sender Player player) {
    helektra
      .getSettingsConfig()
      .getConfig()
      .set("settings.spawn", LocationUtil.serialize(player.getLocation()));

    helektra.getSettingsConfig().save();
  }
}
