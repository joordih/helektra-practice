package dev.voltic.helektra.api.model.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LobbyTime {
    @JsonProperty("day") DAY,
    @JsonProperty("afternoon") AFTERNOON,
    @JsonProperty("night") NIGHT
}
