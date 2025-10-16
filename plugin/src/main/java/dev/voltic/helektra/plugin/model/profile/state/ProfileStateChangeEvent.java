package dev.voltic.helektra.plugin.model.profile.state;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.ProfileState;
import lombok.Getter;

@Getter
public class ProfileStateChangeEvent extends Event {
  private static final HandlerList handlers = new HandlerList();

  private final Player player;
  private final IProfile profile;
  private final ProfileState oldState;
  private final ProfileState newState;

  public ProfileStateChangeEvent(Player player, IProfile profile, ProfileState oldState, ProfileState newState) {
    this.player = player;
    this.profile = profile;
    this.oldState = oldState;
    this.newState = newState;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
