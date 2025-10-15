package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import dev.voltic.helektra.api.model.scoreboard.IScoreboardService;
import dev.voltic.helektra.plugin.model.scoreboard.HelektraScoreboardService;

public class ScoreboardModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IScoreboardService.class).to(HelektraScoreboardService.class);
    }
}
