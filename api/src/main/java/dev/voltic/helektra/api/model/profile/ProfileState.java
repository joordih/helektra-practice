package dev.voltic.helektra.api.model.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProfileState {
    @JsonProperty("lobby") LOBBY,
    @JsonProperty("in_game") IN_GAME,
    @JsonProperty("in_queue") IN_QUEUE,
    @JsonProperty("kit_editor") KIT_EDITOR,
    @JsonProperty("in_party") IN_PARTY,
    @JsonProperty("spectator") SPECTATOR,
    @JsonProperty("in_event") IN_EVENT
}
