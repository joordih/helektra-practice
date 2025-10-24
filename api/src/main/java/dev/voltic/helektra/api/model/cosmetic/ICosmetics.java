package dev.voltic.helektra.api.model.cosmetic;

import java.util.Map;

public interface ICosmetics {
  String getSelectedCosmeticId(CosmeticType type);

  void setSelectedCosmetic(CosmeticType type, String cosmeticId);

  Map<CosmeticType, String> getAllSelections();

  boolean hasSelectedCosmetic(CosmeticType type);

  void clearSelection(CosmeticType type);
}
