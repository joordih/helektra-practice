package dev.voltic.helektra.plugin.model.kit.layout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.voltic.helektra.api.model.kit.IPlayerKitLayout;
import dev.voltic.helektra.plugin.model.kit.serialization.InventorySerializer;
import dev.voltic.helektra.plugin.model.kit.serialization.SerializedInventory;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerKitLayout implements IPlayerKitLayout {

  @JsonProperty("_id")
  private String id;

  private UUID playerId;
  private String kitName;
  private SerializedInventory layout;

  public PlayerKitLayout(UUID playerId, String kitName) {
    this.playerId = playerId;
    this.kitName = kitName;
    this.layout = SerializedInventory.empty();
    this.id = generateId(playerId, kitName);
  }

  @Override
  @JsonIgnore
  public ItemStack[] getLayoutContents() {
    if (layout == null || layout.isEmpty()) {
      return new ItemStack[0];
    }
    return InventorySerializer.deserialize(layout, kitName);
  }

  @Override
  @JsonIgnore
  public void setLayoutContents(ItemStack[] contents) {
    if (contents == null || contents.length == 0) {
      this.layout = SerializedInventory.empty();
      return;
    }
    this.layout = InventorySerializer.serializeArray(contents);
  }

  @Override
  public boolean hasCustomLayout() {
    return layout != null && !layout.isEmpty();
  }

  private static String generateId(UUID playerId, String kitName) {
    return playerId.toString() + "_" + kitName.toLowerCase();
  }
}
