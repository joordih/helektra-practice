package dev.voltic.helektra.plugin.model.kit.layout.repository;

import dev.voltic.helektra.plugin.model.kit.layout.PlayerKitLayout;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerKitLayoutRepository {
  CompletableFuture<Optional<PlayerKitLayout>> findByPlayerAndKit(UUID playerId, String kitName);
  CompletableFuture<Void> save(PlayerKitLayout layout);
  CompletableFuture<Void> delete(UUID playerId, String kitName);
}
