package dev.voltic.helektra.plugin.model.match.event;

import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.Participant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ParticipantDeathEvent extends MatchEvent {

  private final UUID participantId;
  private final UUID killerId;

  public ParticipantDeathEvent(
    IMatch match,
    UUID participantId,
    UUID killerId
  ) {
    super(match);
    this.participantId = participantId;
    this.killerId = killerId;
  }

  public Participant getDeadParticipant() {
    return match.getParticipant(participantId).orElse(null);
  }

  public Participant getKiller() {
    if (killerId == null) {
      return null;
    }
    return match.getParticipant(killerId).orElse(null);
  }
}
