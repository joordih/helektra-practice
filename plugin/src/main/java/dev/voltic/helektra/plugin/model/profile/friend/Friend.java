package dev.voltic.helektra.plugin.model.profile.friend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.voltic.helektra.api.model.profile.IFriend;
import java.beans.ConstructorProperties;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Friend implements IFriend {

  @JsonProperty("_id")
  private String id;

  private String name;
  private Status status;

  @JsonCreator
  @ConstructorProperties({ "_id", "name", "status" })
  public Friend(
    @JsonProperty("_id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("status") Status status
  ) {
    this.id = id;
    this.name = name;
    this.status = status == null ? Status.PENDING : status;
  }

  @Override
  public UUID getUniqueId() {
    String value = id;
    int separator = value.indexOf(':');
    if (separator >= 0 && separator + 1 < value.length()) {
      value = value.substring(separator + 1);
    }
    return UUID.fromString(value);
  }
}
