package dev.voltic.helektra.plugin.model.profile.hotbar;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public final class ProfileHotbarLayout {
  private final Map<Integer, ProfileHotbarItem> itemsBySlot;

  public ProfileHotbarLayout(Collection<ProfileHotbarItem> items) {
    Map<Integer, ProfileHotbarItem> copy = Maps.newHashMap();

    for (ProfileHotbarItem item : items) {
      copy.put(item.slot(), item);
    }

    this.itemsBySlot = Collections.unmodifiableMap(copy);
  }

  public void apply(Player player) {
    PlayerInventory inventory = player.getInventory();

    for (ProfileHotbarItem item : itemsBySlot.values()) {
      inventory.setItem(item.slot(), item.item());
    }
  }

  public Optional<String> actionFor(int slot) {
    ProfileHotbarItem item = itemsBySlot.get(slot);
    if (item == null)
      return Optional.empty();

    return Optional.of(item.action());
  }

  public boolean isEmpty() {
    return itemsBySlot.isEmpty();
  }
}
