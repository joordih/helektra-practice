package dev.voltic.helektra.plugin.model.profile.hotbar.actions;

import org.bukkit.entity.Player;

public interface HotbarAction {
  String id();

  void execute(Player player);
}
