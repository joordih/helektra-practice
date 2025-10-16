package dev.voltic.helektra.api.model.kit;

import dev.voltic.helektra.api.model.Model;
import java.util.List;
import java.util.Set;
import org.bukkit.inventory.ItemStack;

public interface IKit extends Model {
  String getName();
  String getDisplayName();
  ItemStack getIcon();

  Set<String> getArenaIds();
  int getQueue();
  int getPlaying();
  int getSlot();
  int getKitEditorSlot();
  double getHealth();
  double getDamageMultiplier();
  List<String> getDescription();

  void incrementQueue();
  void decrementQueue();
  void incrementPlaying();
  void decrementPlaying();

  boolean hasArena(String arenaId);
  void toggleArena(String arenaId);
  void setIcon(ItemStack icon);
}
