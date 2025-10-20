package dev.voltic.helektra.plugin.model.profile.hotbar;

import org.bukkit.inventory.ItemStack;

public record ProfileHotbarItem(int slot, ItemStack item, String action) {
  public ProfileHotbarItem {
    if (slot < 0) {
      throw new IllegalArgumentException("slot must be non-negative");
    }
    if (item == null) {
      throw new IllegalArgumentException("item cannot be null");
    }
    if (action == null || action.isBlank()) {
      throw new IllegalArgumentException("action cannot be blank");
    }

    item = item.clone();
  }

  @Override
  public ItemStack item() {
    return item.clone();
  }
}
