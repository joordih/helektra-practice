package dev.voltic.helektra.api.model.arena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetConfig {
    @Builder.Default
    private ResetStrategy strategy = ResetStrategy.HYBRID;
    
    @Builder.Default
    private double thresholdT1 = 0.18;
    
    @Builder.Default
    private double thresholdT2 = 0.55;
    
    @Builder.Default
    private int perTickBudget = 10000;
    
    @Builder.Default
    private boolean async = true;
    
    @Builder.Default
    private int backoffTpsThreshold = 195;
}
