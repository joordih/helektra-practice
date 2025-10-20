package dev.voltic.helektra.api.model.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRound {
    private int roundNumber;
    private long startTime;
    private long endTime;
    private String winner;
}
