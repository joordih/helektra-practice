package dev.voltic.helektra.plugin.model.profile;

import java.beans.ConstructorProperties;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.LobbyTime;
import dev.voltic.helektra.api.model.profile.PingMatchmaking;
import dev.voltic.helektra.api.model.profile.ProfileState;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Profile implements IProfile {

    @JsonProperty("_id")
    private String id;
    private String name;
    private int level;
    private Settings settings;
    
    @JsonIgnore
    private ProfileState profileState;

    public Profile(String id, String name) {
        this.id = id;
        this.name = name;
        this.level = 0;
        this.settings = new Settings(
            true, 
            new PingMatchmaking(false, 0, 200),
            LobbyTime.DAY,
            true,
            true,
            true,
            true
        );
        this.profileState = ProfileState.LOBBY;
    }

    @ConstructorProperties({
        "_id",
        "name",
        "level",
        "settings"
    })
    public Profile(String id, String name, int level, Settings settings) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.settings = (settings != null) ? settings : new Settings(
            true,
            new PingMatchmaking(false, 0, 200),
            LobbyTime.DAY,
            true,
            true,
            true,
            true
        );
        this.profileState = ProfileState.LOBBY;
    }

    @JsonIgnore
    public UUID getUniqueId() {
        return UUID.fromString(id);
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public boolean isAllowDuels() {
        return settings.allowDuels();
    }

    @Override
    public void setAllowDuels(boolean allowDuels) {
        this.settings = new Settings(
            allowDuels,
            settings.pingMatchmaking(),
            settings.lobbyTime(),
            settings.viewEventMessages(),
            settings.viewPlayers(),
            settings.allowParty(),
            settings.allowSpectators()
        );
    }

    @Override
    public PingMatchmaking getPingMatchmaking() {
        return settings.pingMatchmaking();
    }

    @Override
    public void setPingMatchmaking(PingMatchmaking pingMatchmaking) {
        this.settings = new Settings(
            settings.allowDuels(),
            pingMatchmaking,
            settings.lobbyTime(),
            settings.viewEventMessages(),
            settings.viewPlayers(),
            settings.allowParty(),
            settings.allowSpectators()
        );
    }

    @Override
    public boolean isPingMatchmakingEnabled() {
        return settings.pingMatchmaking().enabled();
    }

    @Override
    public void setPingMatchmakingEnabled(boolean enabled) {
        PingMatchmaking pm = settings.pingMatchmaking();
        this.settings = new Settings(
            settings.allowDuels(),
            new PingMatchmaking(enabled, pm.min(), pm.max()),
            settings.lobbyTime(),
            settings.viewEventMessages(),
            settings.viewPlayers(),
            settings.allowParty(),
            settings.allowSpectators()
        );
    }

    @Override
    public int getMinPingMatchmaking() {
        return settings.pingMatchmaking().min();
    }

    @Override
    public void setMinPingMatchmaking(int min) {
        PingMatchmaking pm = settings.pingMatchmaking();
        this.settings = new Settings(
            settings.allowDuels(),
            new PingMatchmaking(pm.enabled(), min, pm.max()),
            settings.lobbyTime(),
            settings.viewEventMessages(),
            settings.viewPlayers(),
            settings.allowParty(),
            settings.allowSpectators()
        );
    }

    @Override
    public int getMaxPingMatchmaking() {
        return settings.pingMatchmaking().max();
    }

    @Override
    public void setMaxPingMatchmaking(int max) {
        PingMatchmaking pm = settings.pingMatchmaking();
        this.settings = new Settings(
            settings.allowDuels(),
            new PingMatchmaking(pm.enabled(), pm.min(), max),
            settings.lobbyTime(),
            settings.viewEventMessages(),
            settings.viewPlayers(),
            settings.allowParty(),
            settings.allowSpectators()
        );
    }

    @Override
    public LobbyTime getLobbyTime() {
        return settings.lobbyTime();
    }

    @Override
    public void setLobbyTime(LobbyTime lobbyTime) {
        this.settings = new Settings(
            settings.allowDuels(),
            settings.pingMatchmaking(),
            lobbyTime,
            settings.viewEventMessages(),
            settings.viewPlayers(),
            settings.allowParty(),
            settings.allowSpectators()
        );
    }

    @Override
    public boolean isViewEventMessages() {
        return settings.viewEventMessages();
    }

    @Override
    public void setViewEventMessages(boolean viewEventMessages) {
        this.settings = new Settings(
            settings.allowDuels(),
            settings.pingMatchmaking(),
            settings.lobbyTime(),
            viewEventMessages,
            settings.viewPlayers(),
            settings.allowParty(),
            settings.allowSpectators()
        );
    }

    @Override
    public boolean isViewPlayers() {
        return settings.viewPlayers();
    }

    @Override
    public void setViewPlayers(boolean viewPlayers) {
        this.settings = new Settings(
            settings.allowDuels(),
            settings.pingMatchmaking(),
            settings.lobbyTime(),
            settings.viewEventMessages(),
            viewPlayers,
            settings.allowParty(),
            settings.allowSpectators()
        );
    }

    @Override
    public boolean isAllowParty() {
        return settings.allowParty();
    }

    @Override
    public void setAllowParty(boolean allowParty) {
        this.settings = new Settings(
            settings.allowDuels(),
            settings.pingMatchmaking(),
            settings.lobbyTime(),
            settings.viewEventMessages(),
            settings.viewPlayers(),
            allowParty,
            settings.allowSpectators()
        );
    }

    @Override
    public boolean isAllowSpectators() {
        return settings.allowSpectators();
    }

    @Override
    public void setAllowSpectators(boolean allowSpectators) {
        this.settings = new Settings(
            settings.allowDuels(),
            settings.pingMatchmaking(),
            settings.lobbyTime(),
            settings.viewEventMessages(),
            settings.viewPlayers(),
            settings.allowParty(),
            allowSpectators
        );
    }

    @Override
    public ProfileState getProfileState() {
        return profileState;
    }

    @Override
    public void setProfileState(ProfileState profileState) {
        this.profileState = profileState;
    }

    public record Settings(
        @JsonProperty("allowDuels") boolean allowDuels,
        @JsonProperty("pingMatchmaking") PingMatchmaking pingMatchmaking,
        @JsonProperty("lobbyTime") LobbyTime lobbyTime,
        @JsonProperty("viewEventMessages") boolean viewEventMessages,
        @JsonProperty("viewPlayers") boolean viewPlayers,
        @JsonProperty("allowParty") boolean allowParty,
        @JsonProperty("allowSpectators") boolean allowSpectators
    ) {
        @JsonCreator
        public Settings {
            if (pingMatchmaking == null)
                throw new IllegalArgumentException("PingMatchmaking cannot be null");
            if (lobbyTime == null)
                throw new IllegalArgumentException("LobbyTime cannot be null");
        }
    }
}
