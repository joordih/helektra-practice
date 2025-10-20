package dev.voltic.helektra.plugin.model.match;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MatchSettings {

    int endDelaySeconds;
    TitleConfig winTitle;
    TitleConfig loseTitle;
    SpectatorConfig spectator;

    @Value
    @Builder
    public static class TitleConfig {

        String title;
        String subtitle;
        int fadeInTicks;
        int stayTicks;
        int fadeOutTicks;
    }

    @Value
    @Builder
    public static class SpectatorConfig {

        boolean enabled;
        CommandMessages command;
    }

    @Value
    @Builder
    public static class CommandMessages {

        String usage;
        String notFound;
        String alreadySpectating;
        String noPermission;
        String success;
        String disabled;
    }
}
