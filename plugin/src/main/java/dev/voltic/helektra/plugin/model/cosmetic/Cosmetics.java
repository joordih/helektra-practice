package dev.voltic.helektra.plugin.model.cosmetic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.voltic.helektra.api.model.cosmetic.CosmeticType;
import dev.voltic.helektra.api.model.cosmetic.ICosmetics;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class Cosmetics implements ICosmetics {

  @JsonProperty("selections")
  private final Map<CosmeticType, String> selections;

  public Cosmetics() {
    this.selections = new HashMap<>();
  }

  @JsonCreator
  public Cosmetics(@JsonProperty("selections") Map<CosmeticType, String> selections) {
    this.selections = selections != null ? new HashMap<>(selections) : new HashMap<>();
  }

  @Override
  public String getSelectedCosmeticId(CosmeticType type) {
    return selections.get(type);
  }

  @Override
  public void setSelectedCosmetic(CosmeticType type, String cosmeticId) {
    if (cosmeticId == null || cosmeticId.isBlank()) {
      selections.remove(type);
    } else {
      selections.put(type, cosmeticId);
    }
  }

  @Override
  public Map<CosmeticType, String> getAllSelections() {
    return new HashMap<>(selections);
  }

  @Override
  public boolean hasSelectedCosmetic(CosmeticType type) {
    return selections.containsKey(type) && selections.get(type) != null;
  }

  @Override
  public void clearSelection(CosmeticType type) {
    selections.remove(type);
  }
}
