package dev.voltic.helektra.api.model.profile;

import java.util.UUID;

import dev.voltic.helektra.api.model.Model;

public interface IProfile extends Model {

  UUID getUniqueId();

  String getName();

  void setName(String name);

  int getLevel();

  void setLevel(int level);

  ProfileState getProfileState();

  void setProfileState(ProfileState profileState);

  Settings getSettings();

  void setSettings(Settings settings);

  default PingMatchmaking getPingMatchmaking() {
    return getSettings().getPingMatchmaking();
  }

  default boolean isPingMatchmakingEnabled() {
    return getPingMatchmaking().isEnabled();
  }

  default void setPingMatchmakingEnabled(boolean enabled) {
    getPingMatchmaking().setEnabled(enabled);
  }

  default int getMinPingMatchmaking() {
    return getPingMatchmaking().getMin();
  }

  default void setMinPingMatchmaking(int min) {
    if (min < 0)
      throw new IllegalArgumentException("The ping cannot be negative");
    if (getPingMatchmaking().getMax() < min)
      throw new IllegalArgumentException("The maximum ping cannot be less than the minimum");
    getPingMatchmaking().setMin(min);
  }

  default int getMaxPingMatchmaking() {
    return getPingMatchmaking().getMax();
  }

  default void setMaxPingMatchmaking(int max) {
    if (max < getPingMatchmaking().getMin())
      throw new IllegalArgumentException("The maximum ping cannot be less than the minimum");
    getPingMatchmaking().setMax(max);
  }

  interface Settings {

    boolean isAllowDuels();

    void setAllowDuels(boolean allowDuels);

    boolean isAllowParty();

    void setAllowParty(boolean allowParty);

    boolean isAllowSpectators();

    void setAllowSpectators(boolean allowSpectators);

    boolean isViewPlayers();

    void setViewPlayers(boolean viewPlayers);

    boolean isViewEventMessages();

    void setViewEventMessages(boolean viewEventMessages);

    boolean isKitMode();

    void setKitMode(boolean kitMode);

    PingMatchmaking getPingMatchmaking();

    void setPingMatchmaking(PingMatchmaking pingMatchmaking);

    LobbyTime getLobbyTime();

    void setLobbyTime(LobbyTime lobbyTime);
  }
}
