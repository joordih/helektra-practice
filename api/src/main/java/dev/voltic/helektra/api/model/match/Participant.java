package dev.voltic.helektra.api.model.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    private UUID uniqueId;
    private String name;
    private int kills;
    private int deaths;
    private boolean alive;
}
