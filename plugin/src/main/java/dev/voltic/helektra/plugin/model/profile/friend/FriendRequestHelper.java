package dev.voltic.helektra.plugin.model.profile.friend;

import dev.voltic.helektra.api.model.profile.IFriend;
import java.util.UUID;

public class FriendRequestHelper {

  public static boolean isIncomingRequest(Friend friend, UUID viewerId) {
    if (friend.getStatus() != IFriend.Status.PENDING) {
      return false;
    }
    return friend.getName() == null || friend.getName().isEmpty();
  }

  public static boolean isOutgoingRequest(Friend friend, UUID viewerId) {
    if (friend.getStatus() != IFriend.Status.PENDING) {
      return false;
    }
    return friend.getName() != null && !friend.getName().isEmpty();
  }

  public static UUID extractSender(Friend friend) {
    String compositeId = friend.getId();
    String[] parts = compositeId.split(":");
    if (parts.length != 2) {
      throw new IllegalStateException("Invalid friend ID format");
    }
    boolean isOutgoing = friend.getName() != null && !friend.getName().isEmpty();
    if (isOutgoing) {
      return UUID.fromString(parts[0]);
    } else {
      return UUID.fromString(parts[1]);
    }
  }

  public static UUID extractReceiver(Friend friend) {
    String compositeId = friend.getId();
    String[] parts = compositeId.split(":");
    if (parts.length != 2) {
      throw new IllegalStateException("Invalid friend ID format");
    }
    boolean isOutgoing = friend.getName() != null && !friend.getName().isEmpty();
    if (isOutgoing) {
      return UUID.fromString(parts[1]);
    } else {
      return UUID.fromString(parts[0]);
    }
  }
}
