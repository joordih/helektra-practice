package dev.voltic.helektra.plugin.model.cosmetic;

import dev.voltic.helektra.api.model.cosmetic.CosmeticType;
import dev.voltic.helektra.api.model.cosmetic.ICosmetic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Cosmetic implements ICosmetic {

  private String id;
  private String displayName;
  private CosmeticType type;
  private String iconMaterial;
  private String particleEffect;
  private String colorCode;
  private boolean enabled;
}
