package dev.voltic.helektra.plugin.model.kit.layout;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IPlayerKitLayout;
import dev.voltic.helektra.api.model.kit.IPlayerKitLayoutService;
import dev.voltic.helektra.plugin.model.kit.Kit;
import dev.voltic.helektra.plugin.model.kit.layout.repository.PlayerKitLayoutRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerKitLayoutServiceImpl implements IPlayerKitLayoutService {

  private final PlayerKitLayoutRepository repository;

  @Override
  public CompletableFuture<Optional<IPlayerKitLayout>> getLayout(
    UUID playerId,
    String kitName
  ) {
    return repository
      .findByPlayerAndKit(playerId, kitName)
      .thenApply(opt -> opt.map(layout -> (IPlayerKitLayout) layout));
  }

  @Override
  public CompletableFuture<Void> saveLayout(
    UUID playerId,
    String kitName,
    ItemStack[] contents
  ) {
    return CompletableFuture.runAsync(() -> {
      PlayerKitLayout layout = new PlayerKitLayout(playerId, kitName);
      ItemStack[] filteredContents = filterArmorSlots(contents);
      layout.setLayoutContents(filteredContents);
      repository.save(layout).join();
    });
  }

  @Override
  public CompletableFuture<Void> deleteLayout(UUID playerId, String kitName) {
    return repository.delete(playerId, kitName);
  }

  @Override
  public void applyLayout(Player player, IKit kit) {
    if (!(kit instanceof Kit pluginKit)) {
      return;
    }

    UUID playerId = player.getUniqueId();
    String kitName = kit.getName();

    getLayout(playerId, kitName).thenAccept(layoutOpt -> {
      if (layoutOpt.isPresent() && layoutOpt.get().hasCustomLayout()) {
        ItemStack[] customContents = layoutOpt.get().getLayoutContents();
        pluginKit.applyLoadout(player, Arrays.asList(customContents));
      } else {
        pluginKit.applyLoadout(player);
      }
    });
  }

  private ItemStack[] filterArmorSlots(ItemStack[] contents) {
    if (contents == null) {
      return new ItemStack[0];
    }

    int inventorySize = Math.min(contents.length, 36);
    ItemStack[] filtered = new ItemStack[inventorySize];

    for (int i = 0; i < inventorySize; i++) {
      ItemStack item = contents[i];
      if (
        item != null && item.getType() != Material.AIR && !isArmorPiece(item)
      ) {
        filtered[i] = item.clone();
      }
    }

    return filtered;
  }

  private boolean isArmorPiece(ItemStack item) {
    if (item == null || item.getType() == Material.AIR) {
      return false;
    }
    String typeName = item.getType().name();
    return (
      typeName.endsWith("_HELMET") ||
      typeName.endsWith("_CHESTPLATE") ||
      typeName.endsWith("_LEGGINGS") ||
      typeName.endsWith("_BOOTS")
    );
  }
}
