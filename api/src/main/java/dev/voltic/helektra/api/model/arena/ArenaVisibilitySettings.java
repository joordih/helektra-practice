package dev.voltic.helektra.api.model.arena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArenaVisibilitySettings {
  private int gapChunks;
  private boolean hidePlayers;
  private boolean hideEntities;
  private boolean multiworldEnabled;

  public int getGapBlocks() {
    return Math.max(0, gapChunks) * 16;
  }
}
