package dev.voltic.helektra.api.model.arena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArenaPoolSnapshot {
  private String arenaId;
  private int available;
  private int inUse;
  private int resetting;
  private double utilization;
  private long averageResetDurationMs;
}
