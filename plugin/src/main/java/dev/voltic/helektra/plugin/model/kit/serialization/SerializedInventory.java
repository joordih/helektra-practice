package dev.voltic.helektra.plugin.model.kit.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SerializedInventory {

  private final int size;
  private final List<SerializedInventoryItem> items;

  @JsonCreator
  public SerializedInventory(
      @JsonProperty("size") int size,
      @JsonProperty("items") List<SerializedInventoryItem> items) {
    this.size = Math.max(size, 0);
    this.items = items == null
        ? Collections.emptyList()
        : Collections.unmodifiableList(items);
  }

  @JsonIgnore
  public static SerializedInventory empty() {
    return new SerializedInventory(0, Collections.emptyList());
  }

  @JsonIgnore
  public int itemCount() {
    return items.size();
  }

  @JsonIgnore
  public SerializedInventory copy() {
    List<SerializedInventoryItem> copiedItems = items.stream()
        .map(SerializedInventoryItem::copy)
        .collect(Collectors.toList());
    return new SerializedInventory(size, copiedItems);
  }

  @JsonIgnore
  public boolean isEmpty() {
    return items.isEmpty();
  }

  @JsonIgnore
  public int maxSlot() {
    return items.stream()
        .map(SerializedInventoryItem::getSlot)
        .filter(Objects::nonNull)
        .max(Integer::compareTo)
        .orElse(-1);
  }
}
