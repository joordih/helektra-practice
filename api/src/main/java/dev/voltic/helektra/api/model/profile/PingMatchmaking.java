package dev.voltic.helektra.api.model.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PingMatchmaking(
    @JsonProperty("enabled") boolean enabled,
    @JsonProperty("min") int min,
    @JsonProperty("max") int max
) {
    @JsonCreator
    public PingMatchmaking {
        if (min < 0)
            throw new IllegalArgumentException("The ping cannot be negative");
        if (max < min)
            throw new IllegalArgumentException("The maximum ping cannot be less than the minimum");
    }
}
