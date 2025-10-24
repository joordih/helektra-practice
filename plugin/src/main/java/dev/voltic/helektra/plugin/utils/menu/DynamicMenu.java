package dev.voltic.helektra.plugin.utils.menu;

import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.sound.MenuSoundUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import fr.mrmicky.fastinv.PaginatedFastInv;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class DynamicMenu implements ConfigurableMenu {

  private final MenuConfigHelper menuConfigHelper;
  private final String menuPath;
  private final boolean paginatedMenu;
  private final FastInv fastInventory;
  private final List<Integer> contentSlots = new ArrayList<>();
  private final List<Integer> borderSlots = new ArrayList<>();
  private int lastKnownPageIndex = 0;
  private boolean hasCompletedInitialRender = false;

  public DynamicMenu(MenuConfigHelper menuConfigHelper, String menuPath) {
    this.menuConfigHelper = menuConfigHelper;
    this.menuPath = menuPath;
    this.paginatedMenu = menuConfigHelper.isPaginated(menuPath);
    this.fastInventory = createInventory();
    initializeSlotLayout();
  }

  private FastInv createInventory() {
    String title = menuConfigHelper.getMenuTitle(menuPath);
    int size = menuConfigHelper.getMenuSize(menuPath);
    return paginatedMenu ? new PaginatedFastInv(size, title) : new FastInv(size, title);
  }

  private void initializeSlotLayout() {
    List<Integer> configuredSlots = new ArrayList<>(menuConfigHelper.getContentSlots(menuPath));
    boolean useAutomaticBorders = menuConfigHelper.getBoolean(menuPath + ".content.auto-borders", false);
    if (useAutomaticBorders) {
      borderSlots.addAll(calculateBorderSlots(fastInventory.getInventory().getSize()));
      configuredSlots.removeAll(borderSlots);
    }
    contentSlots.addAll(configuredSlots);
  }

  @Override
  public void setup(Player player) {
    Map<String, MenuItemConfig> configuredItems = loadConfiguredItems();

    List<Map.Entry<String, MenuItemConfig>> dynamicEntries = configuredItems.entrySet().stream()
      .filter(entry -> entry.getValue().exists())
      .filter(entry -> !entry.getKey().equalsIgnoreCase("filler"))
      .filter(entry -> !entry.getKey().equalsIgnoreCase("pagination-previous"))
      .filter(entry -> !entry.getKey().equalsIgnoreCase("pagination-next"))
      .collect(Collectors.toList());

    if (paginatedMenu && fastInventory instanceof PaginatedFastInv paginatedInventory) {
      if (!contentSlots.isEmpty()) {
        paginatedInventory.setContentSlots(contentSlots);
      }

      for (Map.Entry<String, MenuItemConfig> entry : dynamicEntries) {
        MenuItemConfig itemConfig = entry.getValue();
        paginatedInventory.addContent(buildItemFromConfig(itemConfig), click -> handleItemClick(player, itemConfig, click));
      }

      updatePaginationItems(paginatedInventory, configuredItems, player);
    } else {
      int index = 0;
      for (Map.Entry<String, MenuItemConfig> entry : dynamicEntries) {
        MenuItemConfig itemConfig = entry.getValue();
        int resolvedSlot = !contentSlots.isEmpty()
          ? contentSlots.get(index++ % contentSlots.size())
          : index++;
        fastInventory.setItem(resolvedSlot, buildItemFromConfig(itemConfig), click -> handleItemClick(player, itemConfig, click));
      }
    }

    applyFillerFromConfig(configuredItems);
  }

  private List<Integer> calculateBorderSlots(int inventorySize) {
    int columns = 9;
    int rows = inventorySize / columns;
    List<Integer> slots = new ArrayList<>();
    for (int slot = 0; slot < inventorySize; slot++) {
      int row = slot / columns;
      int col = slot % columns;
      if (row == 0 || row == rows - 1 || col == 0 || col == columns - 1) {
        slots.add(slot);
      }
    }
    return slots;
  }

  private void handleItemClick(Player player, MenuItemConfig itemConfig, InventoryClickEvent clickEvent) {
    clickEvent.setCancelled(true);
    MenuSoundUtils.playItemSound(player, itemConfig, menuPath);

    String configuredCommand = itemConfig.getRawString("command", "");
    if (!configuredCommand.isBlank()) {
      player.closeInventory();
      player.performCommand(configuredCommand.replace("{player}", player.getName()));
      return;
    }

    String configuredMessageKey = itemConfig.getRawString("message", "");
    if (!configuredMessageKey.isBlank()) {
      player.sendMessage(TranslationUtils.translate(configuredMessageKey));
    }
  }

  private ItemStack buildItemFromConfig(MenuItemConfig itemConfig) {
    return new ItemBuilder(itemConfig.getMaterial())
      .name(itemConfig.getName())
      .lore(itemConfig.getLore())
      .build();
  }

  private void updatePaginationItems(PaginatedFastInv paginatedInventory, Map<String, MenuItemConfig> configuredItems, Player player) {
    MenuItemConfig previousItemConfig = configuredItems.get("pagination-previous");
    MenuItemConfig nextItemConfig = configuredItems.get("pagination-next");

    int currentPage = getCurrentPageSafe(paginatedInventory);
    int maxPage = getLastPageSafe(paginatedInventory);
    int targetNextPage = Math.min(currentPage + 1, maxPage);
    int targetPreviousPage = Math.max(currentPage - 1, 1);

    if (previousItemConfig != null && previousItemConfig.exists()) {
      if (currentPage > 1) {
        Map<String, String> previousPlaceholders = buildPagePlaceholders(currentPage, maxPage, targetNextPage, targetPreviousPage, targetPreviousPage);
        ItemStack previousItem = buildItemWithPagePlaceholders(previousItemConfig, previousPlaceholders);
        paginatedInventory.setItem(previousItemConfig.getPrimarySlot(), previousItem, click -> {
          click.setCancelled(true);
          paginatedInventory.openPrevious();
          trackPageChange();
          updatePaginationItems(paginatedInventory, configuredItems, player);
        });
      } else {
        paginatedInventory.removeItem(previousItemConfig.getPrimarySlot());
      }
    }

    if (nextItemConfig != null && nextItemConfig.exists()) {
      if (currentPage < maxPage) {
        Map<String, String> nextPlaceholders = buildPagePlaceholders(currentPage, maxPage, targetNextPage, targetPreviousPage, targetNextPage);
        ItemStack nextItem = buildItemWithPagePlaceholders(nextItemConfig, nextPlaceholders);
        paginatedInventory.setItem(nextItemConfig.getPrimarySlot(), nextItem, click -> {
          click.setCancelled(true);
          paginatedInventory.openNext();
          trackPageChange();
          updatePaginationItems(paginatedInventory, configuredItems, player);
        });
      } else {
        paginatedInventory.removeItem(nextItemConfig.getPrimarySlot());
      }
    }
  }

  private Map<String, String> buildPagePlaceholders(int currentPage, int maxPage, int nextPage, int previousPage, int targetPage) {
    return Map.of(
      "page", String.valueOf(currentPage),
      "max-page", String.valueOf(maxPage),
      "next-page", String.valueOf(nextPage),
      "previous-page", String.valueOf(previousPage),
      "target-page", String.valueOf(targetPage)
    );
  }

  private ItemStack buildItemWithPagePlaceholders(MenuItemConfig itemConfig, Map<String, String> placeholders) {
    String resolvedName = replaceRawPlaceholders(itemConfig.getName(), placeholders);
    List<String> resolvedLore = itemConfig.getLore().stream()
      .map(line -> replaceRawPlaceholders(line, placeholders))
      .collect(Collectors.toList());

    return new ItemBuilder(itemConfig.getMaterial())
      .name(resolvedName)
      .lore(resolvedLore)
      .build();
  }

  private String replaceRawPlaceholders(String text, Map<String, String> placeholders) {
    String output = text;
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      output = output.replace("{" + entry.getKey() + "}", entry.getValue());
    }
    return output;
  }

  private void applyFillerFromConfig(Map<String, MenuItemConfig> configuredItems) {
    MenuItemConfig fillerItemConfig = configuredItems.get("filler");
    if (fillerItemConfig == null || !fillerItemConfig.exists()) {
      return;
    }
    fillFiller(fillerItemConfig);
  }

  public void fillFiller(MenuItemConfig fillerItemConfig) {
    ItemStack fillerItemStack = new ItemBuilder(fillerItemConfig.getMaterial())
      .name(fillerItemConfig.getName())
      .lore(fillerItemConfig.getLore())
      .build();

    int size = getInventorySize();

    for (int slot = 0; slot < size; slot++) {
      boolean isContentSlot = !contentSlots.isEmpty() && contentSlots.contains(slot);
      boolean isOccupied = fastInventory.getInventory().getItem(slot) != null;
      if (!isOccupied && !isContentSlot) {
        fastInventory.setItem(slot, fillerItemStack, click -> click.setCancelled(true));
      }
    }

    for (int borderSlot : borderSlots) {
      if (borderSlot >= 0 && borderSlot < size && fastInventory.getInventory().getItem(borderSlot) == null) {
        fastInventory.setItem(borderSlot, fillerItemStack, click -> click.setCancelled(true));
      }
    }
  }

  private Map<String, MenuItemConfig> loadConfiguredItems() {
    return menuConfigHelper.getItemKeys(menuPath)
      .stream()
      .collect(Collectors.toMap(key -> key, key -> menuConfigHelper.getItemConfig(menuPath, key)));
  }

  public Map<String, MenuItemConfig> getAllItemConfigs() {
    return loadConfiguredItems();
  }

  public void open(Player player) {
    fastInventory.open(player);
    MenuSoundUtils.playOpenSound(player, menuPath);
  }

  public void clear() {
    if (hasCompletedInitialRender) {
      preserveCurrentPage();
    }

    if (paginatedMenu && fastInventory instanceof PaginatedFastInv paginatedInventory) {
      paginatedInventory.clearContent();
      resetPaginationInternalPageIndex(paginatedInventory);
    }

    fastInventory.getInventory().clear();
  }

  private void resetPaginationInternalPageIndex(PaginatedFastInv paginatedInventory) {
    try {
      Field pageField = PaginatedFastInv.class.getDeclaredField("page");
      pageField.setAccessible(true);
      pageField.set(paginatedInventory, 1);
    } catch (Exception ignored) {
    }
  }

  private void preserveCurrentPage() {
    if (paginatedMenu && fastInventory instanceof PaginatedFastInv paginatedInventory) {
      try {
        lastKnownPageIndex = paginatedInventory.currentPage();
      } catch (Exception ignored) {
        lastKnownPageIndex = 1;
      }
    }
  }

  protected void restorePageAfterRefresh() {
    if (!paginatedMenu || !(fastInventory instanceof PaginatedFastInv paginatedInventory)) {
      return;
    }
    int maxPage = getLastPageSafe(paginatedInventory);
    int pageToOpen = Math.min(Math.max(lastKnownPageIndex, 1), maxPage);
    paginatedInventory.openPage(pageToOpen);
  }

  public int getInventorySize() {
    return fastInventory.getInventory().getSize();
  }

  public void setStaticItemWithHandler(int slotIndex, ItemStack itemStack, Consumer<InventoryClickEvent> clickHandler) {
    if (slotIndex < 0 || slotIndex >= getInventorySize()) {
      return;
    }
    fastInventory.setItem(slotIndex, itemStack, clickHandler::accept);
  }

  public void addDynamicItemWithHandler(ItemStack itemStack, Consumer<InventoryClickEvent> clickHandler) {
    if (paginatedMenu && fastInventory instanceof PaginatedFastInv paginatedInventory) {
      paginatedInventory.addContent(itemStack, clickHandler::accept);
    } else {
      int freeSlot = findNextAvailableContentSlot();
      if (freeSlot != -1) {
        fastInventory.setItem(freeSlot, itemStack, clickHandler::accept);
      }
    }
  }

  private int findNextAvailableContentSlot() {
    if (contentSlots.isEmpty()) {
      return -1;
    }
    for (int slot : contentSlots) {
      if (fastInventory.getInventory().getItem(slot) == null) {
        return slot;
      }
    }
    return -1;
  }

  public void initializePagination() {
    if (!paginatedMenu || !(fastInventory instanceof PaginatedFastInv paginatedInventory)) {
      return;
    }
    if (!contentSlots.isEmpty()) {
      paginatedInventory.setContentSlots(contentSlots);
    }
  }

  public void setupPaginationItems(Player player) {
    if (!paginatedMenu || !(fastInventory instanceof PaginatedFastInv paginatedInventory)) {
      return;
    }
    updatePaginationItems(paginatedInventory, getAllItemConfigs(), player);
    applyFillerFromConfig(getAllItemConfigs());
  }

  protected void completeRefresh() {
    if (hasCompletedInitialRender) {
      restorePageAfterRefresh();
    }
    hasCompletedInitialRender = true;
  }

  protected void trackPageChange() {
    preserveCurrentPage();
  }

  private int getCurrentPageSafe(PaginatedFastInv paginatedInventory) {
    try {
      return paginatedInventory.currentPage();
    } catch (Exception ignored) {
      return 1;
    }
  }

  private int getLastPageSafe(PaginatedFastInv paginatedInventory) {
    try {
      return Math.max(paginatedInventory.lastPage(), 1);
    } catch (Exception ignored) {
      return 1;
    }
  }
}
