package dev.voltic.helektra.plugin.api;

import dev.voltic.helektra.api.IHelektraAPI;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.scoreboard.IScoreboardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;

@Singleton
@Getter
public class HelektraAPI implements IHelektraAPI {
    
    private static final String VERSION = "1.0.0";
    
    private final IProfileService profileService;
    private final IArenaService arenaService;
    private final IKitService kitService;
    private final IScoreboardService scoreboardService;
    
    private boolean ready = false;
    
    @Inject
    public HelektraAPI(
            IProfileService profileService,
            IArenaService arenaService,
            IKitService kitService,
            IScoreboardService scoreboardService) {
        this.profileService = profileService;
        this.arenaService = arenaService;
        this.kitService = kitService;
        this.scoreboardService = scoreboardService;
    }
    
    @Override
    public String getVersion() {
        return VERSION;
    }
    
    @Override
    public boolean isReady() {
        return ready;
    }
    
    public void setReady() {
        this.ready = true;
    }
}
