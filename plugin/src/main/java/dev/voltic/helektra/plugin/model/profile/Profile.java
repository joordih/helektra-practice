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
  private ProfileState profileState = ProfileState.LOBBY;

  public Profile(String id, String name) {
    this(id, name, 0, defaultSettings());
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
    this.settings = settings != null ? settings : defaultSettings();
  }

  private static Settings defaultSettings() {
    return new Settings(
        true,
        new PingMatchmaking(false, 0, 200),
        LobbyTime.DAY,
        true,
        true,
        true,
        true,
        false);
  }

  @JsonIgnore
  @Override
  public UUID getUniqueId() {
    return UUID.fromString(id);
  }

  @Override
  public Settings getSettings() {
    return settings;
  }

  @Override
  public void setSettings(IProfile.Settings settings) {
    if (settings instanceof Settings s)
      this.settings = s;
    else
      throw new IllegalArgumentException("Invalid settings type: " + settings.getClass());
  }

  public static class Settings implements IProfile.Settings {

    @JsonProperty("allowDuels")
    private boolean allowDuels;

    @JsonProperty("pingMatchmaking")
    private PingMatchmaking pingMatchmaking;

    @JsonProperty("lobbyTime")
    private LobbyTime lobbyTime;

    @JsonProperty("viewEventMessages")
    private boolean viewEventMessages;

    @JsonProperty("viewPlayers")
    private boolean viewPlayers;

    @JsonProperty("allowParty")
    private boolean allowParty;

    @JsonProperty("allowSpectators")
    private boolean allowSpectators;

    @JsonProperty("kitMode")
    private boolean kitMode;

    @JsonCreator
    public Settings(
        @JsonProperty("allowDuels") boolean allowDuels,
        @JsonProperty("pingMatchmaking") PingMatchmaking pingMatchmaking,
        @JsonProperty("lobbyTime") LobbyTime lobbyTime,
        @JsonProperty("viewEventMessages") boolean viewEventMessages,
        @JsonProperty("viewPlayers") boolean viewPlayers,
        @JsonProperty("allowParty") boolean allowParty,
        @JsonProperty("allowSpectators") boolean allowSpectators,
        @JsonProperty("kitMode") boolean kitMode) {

      if (pingMatchmaking == null)
        throw new IllegalArgumentException("PingMatchmaking cannot be null");
      if (lobbyTime == null)
        throw new IllegalArgumentException("LobbyTime cannot be null");

      this.allowDuels = allowDuels;
      this.pingMatchmaking = pingMatchmaking;
      this.lobbyTime = lobbyTime;
      this.viewEventMessages = viewEventMessages;
      this.viewPlayers = viewPlayers;
      this.allowParty = allowParty;
      this.allowSpectators = allowSpectators;
      this.kitMode = kitMode;
    }

    @Override
    public boolean isAllowDuels() {
      return allowDuels;
    }

    @Override
    public void setAllowDuels(boolean allowDuels) {
      this.allowDuels = allowDuels;
    }

    @Override
    public boolean isAllowParty() {
      return allowParty;
    }

    @Override
    public void setAllowParty(boolean allowParty) {
      this.allowParty = allowParty;
    }

    @Override
    public boolean isAllowSpectators() {
      return allowSpectators;
    }

    @Override
    public void setAllowSpectators(boolean allowSpectators) {
      this.allowSpectators = allowSpectators;
    }

    @Override
    public boolean isViewPlayers() {
      return viewPlayers;
    }

    @Override
    public void setViewPlayers(boolean viewPlayers) {
      this.viewPlayers = viewPlayers;
    }

    @Override
    public boolean isViewEventMessages() {
      return viewEventMessages;
    }

    @Override
    public void setViewEventMessages(boolean viewEventMessages) {
      this.viewEventMessages = viewEventMessages;
    }

    @Override
    public boolean isKitMode() {
      return kitMode;
    }

    @Override
    public void setKitMode(boolean kitMode) {
      this.kitMode = kitMode;
    }

    @Override
    public PingMatchmaking getPingMatchmaking() {
      return pingMatchmaking;
    }

    @Override
    public void setPingMatchmaking(PingMatchmaking pingMatchmaking) {
      this.pingMatchmaking = pingMatchmaking;
    }

    @Override
    public LobbyTime getLobbyTime() {
      return lobbyTime;
    }

    @Override
    public void setLobbyTime(LobbyTime lobbyTime) {
      this.lobbyTime = lobbyTime;
    }
  }
}
