package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.plugin.model.match.MatchArenaTracker;
import dev.voltic.helektra.plugin.model.match.MatchRepository;
import dev.voltic.helektra.plugin.model.match.MatchServiceImpl;
import dev.voltic.helektra.plugin.model.match.MatchSettings;
import dev.voltic.helektra.plugin.model.match.MatchSettings.CommandMessages;
import dev.voltic.helektra.plugin.model.match.MatchSettings.SpectatorConfig;
import dev.voltic.helektra.plugin.model.match.MatchSettings.TitleConfig;
import dev.voltic.helektra.plugin.model.match.queue.QueueService;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.config.FileConfig;
import org.bukkit.configuration.ConfigurationSection;

public class MatchModule extends AbstractModule {

    private final FileConfig settingsConfig;

    public MatchModule(FileConfig settingsConfig) {
        this.settingsConfig = settingsConfig;
    }

    @Override
    protected void configure() {
        bind(MatchRepository.class).in(Scopes.SINGLETON);
        bind(IMatchService.class)
            .to(MatchServiceImpl.class)
            .in(Scopes.SINGLETON);
        bind(IQueueService.class).to(QueueService.class).in(Scopes.SINGLETON);
        bind(MatchArenaTracker.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    MatchSettings provideMatchSettings() {
        ConfigurationSection matchSection = settingsConfig
            .getConfig()
            .getConfigurationSection("match");
        int delaySeconds = matchSection != null
            ? matchSection.getInt("end-delay-seconds", 3)
            : 3;
        ConfigurationSection titlesSection = matchSection != null
            ? matchSection.getConfigurationSection("titles")
            : null;
        TitleConfig winTitle = buildTitle(
            titlesSection,
            "win",
            "&aYou won!",
            "&7You defeated &f%opponent%",
            10,
            60,
            10
        );
        TitleConfig loseTitle = buildTitle(
            titlesSection,
            "lose",
            "&cYou lost",
            "&7You were defeated by &f%opponent%",
            10,
            60,
            10
        );
        ConfigurationSection spectatorSection = matchSection != null
            ? matchSection.getConfigurationSection("spectator")
            : null;
        boolean enabled =
            spectatorSection == null ||
            spectatorSection.getBoolean("enabled", true);
        ConfigurationSection commandSection = spectatorSection != null
            ? spectatorSection.getConfigurationSection("command")
            : null;
        CommandMessages commandMessages = CommandMessages.builder()
            .usage(
                getMessage(
                    commandSection,
                    "usage",
                    "&7Usage: /spectate <player>"
                )
            )
            .notFound(
                getMessage(
                    commandSection,
                    "not-found",
                    "&cThat player is not in a match."
                )
            )
            .alreadySpectating(
                getMessage(
                    commandSection,
                    "already-spectating",
                    "&eYou are already spectating."
                )
            )
            .noPermission(
                getMessage(
                    commandSection,
                    "no-permission",
                    "&cYou don’t have permission to use this command."
                )
            )
            .success(
                getMessage(
                    commandSection,
                    "success",
                    "&aNow spectating &f%player%"
                )
            )
            .disabled(
                getMessage(
                    commandSection,
                    "disabled",
                    "&cSpectator mode is disabled."
                )
            )
            .build();
        SpectatorConfig spectatorConfig = SpectatorConfig.builder()
            .enabled(enabled)
            .command(commandMessages)
            .build();
        return MatchSettings.builder()
            .endDelaySeconds(delaySeconds)
            .winTitle(winTitle)
            .loseTitle(loseTitle)
            .spectator(spectatorConfig)
            .build();
    }

    private TitleConfig buildTitle(
        ConfigurationSection root,
        String key,
        String defaultTitle,
        String defaultSubtitle,
        int fadeIn,
        int stay,
        int fadeOut
    ) {
        ConfigurationSection section = root != null
            ? root.getConfigurationSection(key)
            : null;
        String title = section != null
            ? section.getString("title", defaultTitle)
            : defaultTitle;
        String subtitle = section != null
            ? section.getString("subtitle", defaultSubtitle)
            : defaultSubtitle;
        int fadeInTicks = section != null
            ? section.getInt("fade-in", fadeIn)
            : fadeIn;
        int stayTicks = section != null ? section.getInt("stay", stay) : stay;
        int fadeOutTicks = section != null
            ? section.getInt("fade-out", fadeOut)
            : fadeOut;
        return TitleConfig.builder()
            .title(title)
            .subtitle(subtitle)
            .fadeInTicks(fadeInTicks)
            .stayTicks(stayTicks)
            .fadeOutTicks(fadeOutTicks)
            .build();
    }

    private String getMessage(
        ConfigurationSection section,
        String path,
        String def
    ) {
        if (section == null) {
            return ColorUtils.translate(def);
        }
        return ColorUtils.translate(section.getString(path, def));
    }
}
