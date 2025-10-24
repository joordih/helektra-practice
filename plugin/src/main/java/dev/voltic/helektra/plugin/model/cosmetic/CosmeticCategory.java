package dev.voltic.helektra.plugin.model.cosmetic;

import dev.voltic.helektra.api.model.cosmetic.CosmeticType;
import dev.voltic.helektra.api.model.cosmetic.ICosmetic;
import dev.voltic.helektra.api.model.cosmetic.ICosmeticCategory;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CosmeticCategory implements ICosmeticCategory {

  private CosmeticType type;
  private String displayName;
  private String iconMaterial;
  private List<ICosmetic> cosmetics;

  public CosmeticCategory(CosmeticType type, String displayName, String iconMaterial) {
    this.type = type;
    this.displayName = displayName;
    this.iconMaterial = iconMaterial;
    this.cosmetics = new ArrayList<>();
  }

  public void addCosmetic(ICosmetic cosmetic) {
    cosmetics.add(cosmetic);
  }
}
