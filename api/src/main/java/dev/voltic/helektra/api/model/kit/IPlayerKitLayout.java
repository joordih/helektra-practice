package dev.voltic.helektra.api.model.kit;

import dev.voltic.helektra.api.model.Model;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public interface IPlayerKitLayout extends Model {
  UUID getPlayerId();
  String getKitName();
  ItemStack[] getLayoutContents();
  void setLayoutContents(ItemStack[] contents);
  boolean hasCustomLayout();
}
