package dev.voltic.helektra.api.model.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PingMatchmaking {

  @JsonProperty("enabled")
  private boolean enabled;

  @JsonProperty("min")
  private int min;

  @JsonProperty("max")
  private int max;

  @JsonCreator
  public PingMatchmaking(
      @JsonProperty("enabled") boolean enabled,
      @JsonProperty("min") int min,
      @JsonProperty("max") int max) {
    validateMin(min);
    validateRange(min, max);
    this.enabled = enabled;
    this.min = min;
    this.max = max;
  }

  public boolean enabled() {
    return enabled;
  }

  public int min() {
    return min;
  }

  public int max() {
    return max;
  }

  public void setMin(int min) {
    validateMin(min);
    validateRange(min, this.max);
    this.min = min;
  }

  public void setMax(int max) {
    validateRange(this.min, max);
    this.max = max;
  }

  private void validateMin(int value) {
    if (value < 0)
      throw new IllegalArgumentException("The ping cannot be negative");
  }

  private void validateRange(int min, int max) {
    if (max < min)
      throw new IllegalArgumentException("The maximum ping cannot be less than the minimum");
  }
}
