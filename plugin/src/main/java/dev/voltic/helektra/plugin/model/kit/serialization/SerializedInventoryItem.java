package dev.voltic.helektra.plugin.model.kit.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SerializedInventoryItem {

  private final int slot;
  private final Map<String, Object> data;

  @JsonCreator
  public SerializedInventoryItem(
      @JsonProperty("slot") int slot,
      @JsonProperty("data") Map<String, Object> data) {
    this.slot = slot;
    this.data = data;
  }

  public SerializedInventoryItem copy() {
    return new SerializedInventoryItem(
      slot,
      InventorySerializer.deepCopyMap(data)
    );
  }
}
