package dev.voltic.helektra.plugin.model.kit.serialization;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@SuppressWarnings("all")
public final class InventorySerializer {

  private static final String MATERIAL_KEY = "x-material";

  private InventorySerializer() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static SerializedInventory serialize(PlayerInventory inventory) {
    if (inventory == null) {
      return SerializedInventory.empty();
    }
    return serialize(inventory.getContents(), inventory.getSize());
  }

  public static SerializedInventory serialize(
    ItemStack[] contents,
    int sizeHint
  ) {
    if (contents == null) {
      return new SerializedInventory(Math.max(0, sizeHint), List.of());
    }
    List<SerializedInventoryItem> serialized = new ArrayList<>();
    int length = contents.length;
    for (int slot = 0; slot < length; slot++) {
      ItemStack stack = contents[slot];
      if (stack == null || stack.getType() == Material.AIR) {
        continue;
      }
      serialized.add(new SerializedInventoryItem(slot, serializeItem(stack)));
    }
    int finalSize = Math.max(sizeHint, length);
    return new SerializedInventory(finalSize, serialized);
  }

  public static SerializedInventory serialize(Collection<ItemStack> stacks) {
    if (stacks == null || stacks.isEmpty()) {
      return SerializedInventory.empty();
    }
    List<SerializedInventoryItem> serialized = new ArrayList<>();
    int slot = 0;
    for (ItemStack stack : stacks) {
      if (stack == null || stack.getType() == Material.AIR) {
        slot++;
        continue;
      }
      serialized.add(new SerializedInventoryItem(slot, serializeItem(stack)));
      slot++;
    }
    return new SerializedInventory(
      Math.max(serialized.size(), slot),
      serialized
    );
  }

  public static Map<String, Object> serializeIcon(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return null;
    }
    return serializeItem(stack);
  }

  private static Map<String, Object> serializeItem(ItemStack stack) {
    Map<String, Object> raw = deepCopyMap(stack.serialize());
    try {
      XMaterial xMaterial = XMaterial.matchXMaterial(stack);
      raw.put(MATERIAL_KEY, xMaterial.name());
    } catch (Exception ignored) {
      raw.put(MATERIAL_KEY, stack.getType().name());
    }
    return raw;
  }

  public static List<Map<String, Object>> toConfig(
    SerializedInventory inventory
  ) {
    return inventory
      .getItems()
      .stream()
      .map(item -> {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("slot", item.getSlot());
        data.put("item", deepCopyMap(item.getData()));
        return data;
      })
      .collect(Collectors.toList());
  }

  public static SerializedInventory fromConfig(int size, List<?> rawEntries) {
    if (rawEntries == null || rawEntries.isEmpty()) {
      return new SerializedInventory(size, List.of());
    }

    List<SerializedInventoryItem> items = new ArrayList<>();
    int highestSlot = -1;

    for (Object raw : rawEntries) {
      if (!(raw instanceof Map<?, ?> map)) {
        continue;
      }
      Object slotObj = map.get("slot");
      Object itemObj = map.get("item");
      if (!(slotObj instanceof Number) || itemObj == null) {
        continue;
      }
      int slot = ((Number) slotObj).intValue();
      Map<String, Object> data = extractMap(itemObj);
      if (data == null || data.isEmpty()) {
        continue;
      }
      items.add(new SerializedInventoryItem(slot, deepCopyMap(data)));
      highestSlot = Math.max(highestSlot, slot);
    }

    int finalSize = Math.max(size, highestSlot + 1);
    return new SerializedInventory(finalSize, items);
  }

  public static SerializedInventory fromLegacy(List<?> legacyItems) {
    if (legacyItems == null || legacyItems.isEmpty()) {
      return SerializedInventory.empty();
    }
    List<SerializedInventoryItem> items = new ArrayList<>();
    int slot = 0;
    for (Object obj : legacyItems) {
      if (
        !(obj instanceof ItemStack stack) || stack.getType() == Material.AIR
      ) {
        slot++;
        continue;
      }
      items.add(new SerializedInventoryItem(slot, serializeItem(stack)));
      slot++;
    }
    return new SerializedInventory(Math.max(items.size(), slot), items);
  }

  public static ItemStack[] deserialize(
    SerializedInventory inventory,
    String kitName
  ) {
    if (inventory == null || inventory.isEmpty()) {
      return new ItemStack[0];
    }
    int size = Math.max(inventory.getSize(), inventory.maxSlot() + 1);
    ItemStack[] contents = new ItemStack[size];

    for (SerializedInventoryItem serializedItem : inventory.getItems()) {
      ItemStack stack = deserializeItem(serializedItem.getData(), kitName);
      if (stack != null) {
        int slot = serializedItem.getSlot();
        if (slot >= 0 && slot < contents.length) {
          contents[slot] = stack;
        }
      }
    }
    return contents;
  }

  public static List<ItemStack> deserializeToList(
    SerializedInventory inventory,
    String kitName
  ) {
    ItemStack[] contents = deserialize(inventory, kitName);
    List<ItemStack> list = new ArrayList<>();
    for (ItemStack stack : contents) {
      if (stack != null && stack.getType() != Material.AIR) {
        list.add(stack);
      }
    }
    return list;
  }

  public static ItemStack deserializeIcon(Object raw, String kitName) {
    Map<String, Object> data;
    if (raw instanceof ItemStack stack) {
      data = serializeItem(stack);
    } else if (raw instanceof Map<?, ?> map) {
      data = extractMap(map);
    } else if (raw instanceof MemorySection section) {
      data = extractMap(section);
    } else {
      return null;
    }
    return deserializeItem(data, kitName);
  }

  private static ItemStack deserializeItem(
    Map<String, Object> rawData,
    String kitName
  ) {
    if (rawData == null || rawData.isEmpty()) {
      return null;
    }

    Map<String, Object> data = deepCopyMap(rawData);
    String materialHint = null;

    Object materialKey = data.remove(MATERIAL_KEY);
    if (materialKey instanceof String hint) {
      materialHint = hint;
    }

    Object legacyTypeObj = data.get("type");
    if (legacyTypeObj instanceof String legacyType) {
      if (materialHint == null) {
        materialHint = legacyType;
      }
      if (data.containsKey("id")) {
        data.remove("type");
      }
    }

    if (materialHint == null) {
      Object idObj = data.get("id");
      if (idObj instanceof String idStr) {
        materialHint = idStr;
      }
    }

    try {
      return ItemStack.deserialize(data);
    } catch (IllegalArgumentException | IllegalStateException ex) {
      return buildFallbackItem(materialHint, rawData, kitName);
    }
  }

  private static ItemStack buildFallbackItem(
    String materialHint,
    Map<String, Object> rawData,
    String kitName
  ) {
    String normalized = normalizeMaterial(materialHint);
    if (normalized == null) {
      logUnsupported(String.valueOf(materialHint), kitName);
      return null;
    }

    Optional<XMaterial> matched;
    try {
      matched = XMaterial.matchXMaterial(normalized);
    } catch (Exception ignored) {
      matched = Optional.empty();
    }

    if (matched.isEmpty()) {
      logUnsupported(normalized, kitName);
      return null;
    }

    Material material = matched.get().parseMaterial();
    if (material == null) {
      logUnsupported(normalized, kitName);
      return null;
    }

    ItemStack item = new ItemStack(material, extractAmount(rawData));
    return item;
  }

  private static int extractAmount(Map<String, Object> rawData) {
    Object amountObj = rawData.get("amount");
    if (amountObj instanceof Number number) {
      return Math.max(1, number.intValue());
    }
    Object countObj = rawData.get("count");
    if (countObj instanceof Number number) {
      return Math.max(1, number.intValue());
    }
    return 1;
  }

  private static String normalizeMaterial(String materialHint) {
    if (materialHint == null || materialHint.isBlank()) {
      return null;
    }
    String value = materialHint.trim();
    if (value.contains(":")) {
      value = value.substring(value.indexOf(':') + 1);
    }
    return value.toUpperCase().replace('-', '_');
  }

  private static void logUnsupported(String materialKey, String kitName) {
    if (kitName == null) {
      return;
    }
    String message = TranslationUtils.translate(
      "kit.serializer.unsupported-material",
      "kit",
      kitName,
      "material",
      materialKey
    );
    Helektra.getInstance().getLogger().warning(ColorUtils.stripColor(message));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> extractMap(Object raw) {
    if (raw instanceof Map<?, ?> map) {
      return map
        .entrySet()
        .stream()
        .collect(
          Collectors.toMap(
            entry -> String.valueOf(entry.getKey()),
            entry -> deepCopy(entry.getValue()),
            (a, b) -> b,
            LinkedHashMap::new
          )
        );
    }
    if (raw instanceof MemorySection section) {
      return section
        .getValues(false)
        .entrySet()
        .stream()
        .collect(
          Collectors.toMap(
            Map.Entry::getKey,
            entry -> deepCopy(entry.getValue()),
            (a, b) -> b,
            LinkedHashMap::new
          )
        );
    }
    return null;
  }

  static Map<String, Object> deepCopyMap(Map<String, Object> original) {
    return original
      .entrySet()
      .stream()
      .collect(
        Collectors.toMap(
          Map.Entry::getKey,
          entry -> deepCopy(entry.getValue()),
          (a, b) -> b,
          LinkedHashMap::new
        )
      );
  }

  @SuppressWarnings("unchecked")
  static Object deepCopy(Object value) {
    if (value instanceof Map<?, ?> map) {
      return map
        .entrySet()
        .stream()
        .collect(
          Collectors.toMap(
            entry -> String.valueOf(entry.getKey()),
            entry -> deepCopy(entry.getValue()),
            (a, b) -> b,
            LinkedHashMap::new
          )
        );
    }
    if (value instanceof List<?> list) {
      return list
        .stream()
        .map(InventorySerializer::deepCopy)
        .collect(Collectors.toList());
    }
    return value;
  }

  public static void apply(
    Player player,
    SerializedInventory inventory,
    String kitName
  ) {
    ItemStack[] contents = deserialize(inventory, kitName);
    player.getInventory().clear();
    player.getInventory().setContents(contents);
  }
}
