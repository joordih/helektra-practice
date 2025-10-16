package dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl;

import jakarta.inject.Singleton;
import org.bukkit.entity.Player;

import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;

@Singleton
public class SettingsAction implements HotbarAction {
  @Override
  public String id() {
    return "SETTINGS";
  }

  @Override
  public void execute(Player player) {
    player.performCommand("settings");
  }
}
