package dev.voltic.helektra.plugin.model.kit.serialization;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public final class SerializedInventory {

  private final int size;
  private final List<SerializedInventoryItem> items;

  public SerializedInventory(int size, List<SerializedInventoryItem> items) {
    this.size = Math.max(size, 0);
    this.items = items == null
        ? Collections.emptyList()
        : Collections.unmodifiableList(items);
  }

  public static SerializedInventory empty() {
    return new SerializedInventory(0, Collections.emptyList());
  }

  public int itemCount() {
    return items.size();
  }

  public SerializedInventory copy() {
    List<SerializedInventoryItem> copiedItems = items.stream()
        .map(SerializedInventoryItem::copy)
        .collect(Collectors.toList());
    return new SerializedInventory(size, copiedItems);
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  public int maxSlot() {
    return items.stream()
        .map(SerializedInventoryItem::getSlot)
        .filter(Objects::nonNull)
        .max(Integer::compareTo)
        .orElse(-1);
  }
}
