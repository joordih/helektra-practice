package dev.voltic.helektra.plugin.model.kit.serialization;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SerializedInventoryItem {

  private final int slot;
  private final Map<String, Object> data;

  public SerializedInventoryItem copy() {
    return new SerializedInventoryItem(
      slot,
      InventorySerializer.deepCopyMap(data)
    );
  }
}
