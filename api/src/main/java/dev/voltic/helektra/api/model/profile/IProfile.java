package dev.voltic.helektra.api.model.profile;

import dev.voltic.helektra.api.model.Model;
import java.util.UUID;

public interface IProfile extends Model {
    String getName();
    void setName(String name);
    int getLevel();
    void setLevel(int level);
    UUID getUniqueId();

    boolean isAllowDuels();
    void setAllowDuels(boolean allowDuels);

    PingMatchmaking getPingMatchmaking();
    void setPingMatchmaking(PingMatchmaking pingMatchmaking);
    
    boolean isPingMatchmakingEnabled();
    void setPingMatchmakingEnabled(boolean enabled);
    int getMinPingMatchmaking();
    void setMinPingMatchmaking(int min);
    int getMaxPingMatchmaking();
    void setMaxPingMatchmaking(int max);

    LobbyTime getLobbyTime();
    void setLobbyTime(LobbyTime lobbyTime);

    boolean isViewEventMessages();
    void setViewEventMessages(boolean viewEventMessages);

    boolean isViewPlayers();
    void setViewPlayers(boolean viewPlayers);

    boolean isAllowParty();
    void setAllowParty(boolean allowParty);

    boolean isAllowSpectators();
    void setAllowSpectators(boolean allowSpectators);

    ProfileState getProfileState();
    void setProfileState(ProfileState profileState);
}
