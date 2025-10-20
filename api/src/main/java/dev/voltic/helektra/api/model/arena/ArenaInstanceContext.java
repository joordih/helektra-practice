package dev.voltic.helektra.api.model.arena;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArenaInstanceContext {
  private String arenaId;
  private UUID instanceId;
  private Region region;
  private Location spawnA;
  private Location spawnB;
  private ArenaTemplateMetadata templateMetadata;
}
