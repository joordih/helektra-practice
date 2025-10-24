package dev.voltic.helektra.api.model.cosmetic;

public interface ICosmetic {
  String getId();

  String getDisplayName();

  CosmeticType getType();

  String getIconMaterial();

  String getParticleEffect();

  String getColorCode();

  boolean isEnabled();
}
