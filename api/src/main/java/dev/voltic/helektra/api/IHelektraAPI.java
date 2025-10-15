package dev.voltic.helektra.api;

import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.scoreboard.IScoreboardService;

public interface IHelektraAPI {
    IProfileService getProfileService();
    IArenaService getArenaService();
    IKitService getKitService();
    IScoreboardService getScoreboardService();
    IMatchService getMatchService();
    String getVersion();
    boolean isReady();
}
