package dev.voltic.helektra.api.model.arena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArenaFlags {
    @Builder.Default
    private boolean allowPlace = false;
    
    @Builder.Default
    private boolean allowBreak = false;
    
    @Builder.Default
    private boolean allowLiquids = false;
    
    @Builder.Default
    private boolean allowProjectiles = true;
    
    @Builder.Default
    private boolean allowPvP = true;
    
    @Builder.Default
    private boolean allowHunger = true;
    
    @Builder.Default
    private boolean allowItemDrop = false;
    
    @Builder.Default
    private boolean allowItemPickup = false;
}
