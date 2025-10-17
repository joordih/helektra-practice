package dev.voltic.helektra.plugin.model.profile.menu.friend;

import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class FriendMenuSelectionService {

  private final Map<UUID, FriendMenuItemFactory.FriendView> selections = new ConcurrentHashMap<>();

  public void select(
    UUID ownerId,
    FriendMenuItemFactory.FriendView view
  ) {
    selections.put(ownerId, view);
  }

  public Optional<FriendMenuItemFactory.FriendView> getSelection(UUID ownerId) {
    return Optional.ofNullable(selections.get(ownerId));
  }

  public void clear(UUID ownerId) {
    selections.remove(ownerId);
  }
}
