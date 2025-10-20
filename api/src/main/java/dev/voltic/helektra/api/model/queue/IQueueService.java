package dev.voltic.helektra.api.model.queue;

import dev.voltic.helektra.api.model.kit.QueueType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IQueueService {
    QueueJoinResult joinQueue(UUID playerId, String kitName, QueueType queueType);
    boolean leaveQueue(UUID playerId);
    void clearTicket(UUID playerId);
    Optional<QueueTicket> getTicket(UUID playerId);
    List<QueueTicket> getQueue(String kitName, QueueType queueType);
    void completeMatch(String matchId);
}
