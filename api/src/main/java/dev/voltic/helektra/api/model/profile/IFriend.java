package dev.voltic.helektra.api.model.profile;

import dev.voltic.helektra.api.model.Model;
import java.util.UUID;

public interface IFriend extends Model {
  UUID getUniqueId();

  String getName();

  void setName(String name);

  Status getStatus();

  void setStatus(Status status);

  enum Status {
    PENDING,
    ACCEPTED,
    BLOCKED,
  }
}
