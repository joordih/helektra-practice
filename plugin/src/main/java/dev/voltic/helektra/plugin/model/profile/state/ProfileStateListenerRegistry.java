package dev.voltic.helektra.plugin.model.profile.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ProfileStateListenerRegistry {

  private final JavaPlugin plugin;
  private final Map<UUID, List<Listener>> active;

  @Inject
  public ProfileStateListenerRegistry(JavaPlugin plugin) {
    this.plugin = plugin;
    this.active = new HashMap<>();
  }

  public void register(UUID uuid, List<Listener> listeners) {
    if (listeners == null || listeners.isEmpty())
      return;
    for (Listener listener : listeners) {
      Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
    active.computeIfAbsent(uuid, k -> new ArrayList<>()).addAll(listeners);
  }

  public void unregister(UUID uuid) {
    List<Listener> listeners = active.remove(uuid);
    if (listeners == null)
      return;
    for (Listener listener : listeners) {
      HandlerList.unregisterAll(listener);
    }
  }
}
