package dev.voltic.helektra.api.model.kit;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IPlayerKitLayoutService {
  CompletableFuture<Optional<IPlayerKitLayout>> getLayout(UUID playerId, String kitName);
  CompletableFuture<Void> saveLayout(UUID playerId, String kitName, ItemStack[] contents);
  CompletableFuture<Void> deleteLayout(UUID playerId, String kitName);
  void applyLayout(Player player, IKit kit);
}
