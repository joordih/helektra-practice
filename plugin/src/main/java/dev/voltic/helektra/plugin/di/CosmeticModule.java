package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import dev.voltic.helektra.api.model.cosmetic.ICosmeticService;
import dev.voltic.helektra.plugin.model.cosmetic.service.CosmeticServiceImpl;

public class CosmeticModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ICosmeticService.class).to(CosmeticServiceImpl.class);
  }
}
