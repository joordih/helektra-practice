package dev.voltic.helektra.api.model.kit;

import dev.voltic.helektra.api.model.Model;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public interface IKit extends Model {
  String getName();
  String getDisplayName();
  ItemStack getIcon();

  Set<String> getArenaIds();
  int getQueue(QueueType type);

  default int getQueue() {
    return getQueue(QueueType.UNRANKED);
  }

  int getPlaying();
  int getSlot();
  int getKitEditorSlot();
  double getHealth();
  double getDamageMultiplier();
  List<String> getDescription();
  Map<UUID, IKit> getQueuedPlayers(QueueType type);

  void incrementQueue(UUID playerId, QueueType type);
  void decrementQueue(UUID playerId, QueueType type);
  void incrementPlaying();
  void decrementPlaying();

  boolean hasArena(String arenaId);
  void toggleArena(String arenaId);
  void setIcon(ItemStack icon);
}
