package dev.voltic.helektra.plugin.model.profile.friend;

import jakarta.inject.Singleton;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class FriendAddPromptService {

  private final Set<UUID> pending = ConcurrentHashMap.newKeySet();

  public boolean beginPrompt(UUID playerId) {
    return pending.add(playerId);
  }

  public boolean isPrompting(UUID playerId) {
    return pending.contains(playerId);
  }

  public void finishPrompt(UUID playerId) {
    pending.remove(playerId);
  }
}
