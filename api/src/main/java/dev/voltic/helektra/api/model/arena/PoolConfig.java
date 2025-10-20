package dev.voltic.helektra.api.model.arena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoolConfig {
    @Builder.Default
    private int preloaded = 8;
    
    @Builder.Default
    private int minIdle = 4;
    
    @Builder.Default
    private int maxActive = 32;
    
    @Builder.Default
    private boolean autoScale = true;
}
