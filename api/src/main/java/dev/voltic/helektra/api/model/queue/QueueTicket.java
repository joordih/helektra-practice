package dev.voltic.helektra.api.model.queue;

import dev.voltic.helektra.api.model.kit.QueueType;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueTicket {
    private UUID playerId;
    private String kitName;
    private QueueType queueType;
    private Instant joinedAt;
}
