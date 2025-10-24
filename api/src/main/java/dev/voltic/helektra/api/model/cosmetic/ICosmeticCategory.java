package dev.voltic.helektra.api.model.cosmetic;

import java.util.List;

public interface ICosmeticCategory {
  CosmeticType getType();

  String getDisplayName();

  String getIconMaterial();

  List<ICosmetic> getCosmetics();
}
