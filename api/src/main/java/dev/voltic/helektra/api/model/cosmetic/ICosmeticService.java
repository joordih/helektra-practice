package dev.voltic.helektra.api.model.cosmetic;

import java.util.List;
import java.util.Optional;

public interface ICosmeticService {
  void loadCosmetics();

  Optional<ICosmetic> getCosmeticById(String id);

  List<ICosmetic> getCosmeticsByType(CosmeticType type);

  List<ICosmeticCategory> getAllCategories();

  Optional<ICosmeticCategory> getCategoryByType(CosmeticType type);

  boolean isCosmeticEnabled(String id);
}
