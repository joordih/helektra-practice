package dev.voltic.helektra.plugin.model.profile.hotbar.actions.impl;

import dev.voltic.helektra.plugin.model.profile.hotbar.actions.HotbarAction;
import jakarta.inject.Singleton;
import org.bukkit.entity.Player;

@Singleton
public class JoinQueueAction implements HotbarAction {

  @Override
  public String id() {
    return "JOIN_QUEUE";
  }

  @Override
  public void execute(Player player) {
    player.performCommand("queue");
  }
}
