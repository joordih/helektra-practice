package dev.voltic.helektra.plugin.utils.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.sound.MenuSoundUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import fr.mrmicky.fastinv.PaginatedFastInv;
import java.lang.reflect.Field;

public class DynamicMenu implements ConfigurableMenu {

  private final MenuConfigHelper config;
  private final String path;
  private final boolean paginated;
  private final FastInv menu;
  private final List<Integer> contentSlots;
  private int currentPageIndex = 0;
  private boolean hasCompletedInitialRender = false;

  public DynamicMenu(MenuConfigHelper config, String path) {
    this.config = config;
    this.path = path;
    this.paginated = config.isPaginated(path);
    this.contentSlots = config.getContentSlots(path);
    this.menu = createMenu();
  }

  private FastInv createMenu() {
    String title = config.getMenuTitle(path);
    int size = config.getMenuSize(path);
    return paginated ? new PaginatedFastInv(size, title) : new FastInv(size, title);
  }

  @Override
  public void setup(Player player) {
    Map<String, MenuItemConfig> items = loadItems();
    List<MenuItemConfig> dynamicItems = new ArrayList<>();

    if (config.contains(path + ".content.start-slot") && config.contains(path + ".content.end-slot")) {
      int start = config.getInt(path + ".content.start-slot", 10);
      int end = config.getInt(path + ".content.end-slot", 16);
      contentSlots.clear();
      contentSlots.addAll(IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList()));
    }

    for (var entry : items.entrySet()) {
      String key = entry.getKey();
      MenuItemConfig item = entry.getValue();

      if (!item.exists() || "filler".equalsIgnoreCase(key))
        continue;

      if (item.hasExplicitSlot() && (contentSlots.isEmpty() || !contentSlots.contains(item.getPrimarySlot()))) {
        setItem(item.getPrimarySlot(), item, player);
        continue;
      }

      dynamicItems.add(item);
    }

    if (paginated && menu instanceof PaginatedFastInv paginatedMenu) {

      if (!contentSlots.isEmpty()) {
        paginatedMenu.setContentSlots(contentSlots);
      }

      for (MenuItemConfig item : dynamicItems) {
        paginatedMenu.addContent(buildItem(item), e -> handleClick(player, item, e));
      }

      MenuItemConfig previous = items.get("pagination-previous");
      MenuItemConfig next = items.get("pagination-next");

      if (previous != null && previous.exists()) {
        paginatedMenu.setItem(previous.getPrimarySlot(), buildItem(previous), e -> {
          e.setCancelled(true);
          paginatedMenu.openPrevious();
          applyFiller(items);
        });
      }

      if (next != null && next.exists()) {
        paginatedMenu.setItem(next.getPrimarySlot(), buildItem(next), e -> {
          e.setCancelled(true);
          paginatedMenu.openNext();
          applyFiller(items);
        });
      }

    } else {
      int index = 0;
      for (MenuItemConfig item : dynamicItems) {
        int slot = !contentSlots.isEmpty()
            ? contentSlots.get(index++ % contentSlots.size())
            : index++;
        setItem(slot, item, player);
      }
    }

    applyFiller(items);
  }

  private void setItem(int slot, MenuItemConfig config, Player player) {
    menu.setItem(slot, buildItem(config), e -> handleClick(player, config, e));
  }

  private void handleClick(Player player, MenuItemConfig config, InventoryClickEvent e) {
    e.setCancelled(true);
    MenuSoundUtils.playItemSound(player, config, path);

    String command = config.getRawString("command", "");
    if (!command.isBlank()) {
      player.closeInventory();
      player.performCommand(command.replace("{player}", player.getName()));
      return;
    }

    String messageKey = config.getRawString("message", "");
    if (!messageKey.isBlank()) {
      player.sendMessage(TranslationUtils.translate(messageKey));
    }
  }

  private ItemStack buildItem(MenuItemConfig config) {
    return new ItemBuilder(config.getMaterial())
        .name(config.getName())
        .lore(config.getLore())
        .build();
  }

  private void applyFiller(Map<String, MenuItemConfig> items) {
    MenuItemConfig filler = items.get("filler");
    if (filler == null || !filler.exists())
      return;

    ItemStack item = new ItemBuilder(filler.getMaterial())
        .name(filler.getName())
        .lore(filler.getLore())
        .build();

    int size = menu.getInventory().getSize();
    for (int i = 0; i < size; i++) {
      if (menu.getInventory().getItem(i) == null &&
          (contentSlots.isEmpty() || !contentSlots.contains(i))) {
        menu.setItem(i, item, e -> e.setCancelled(true));
      }
    }
  }

  private Map<String, MenuItemConfig> loadItems() {
    return config.getItemKeys(path).stream()
        .collect(Collectors.toMap(k -> k, k -> config.getItemConfig(path, k)));
  }

  public void open(Player player) {
    menu.open(player);
    MenuSoundUtils.playOpenSound(player, path);
  }

  public void clear() {
    if (hasCompletedInitialRender) {
      preserveCurrentPage();
    }
    if (paginated && menu instanceof PaginatedFastInv paginatedMenu) {
      paginatedMenu.clearContent();
      resetPaginationToFirstPage(paginatedMenu);
    }
    menu.getInventory().clear();
  }

  private void resetPaginationToFirstPage(PaginatedFastInv paginatedMenu) {
    try {
      Field pageField = PaginatedFastInv.class.getDeclaredField("page");
      pageField.setAccessible(true);
      pageField.set(paginatedMenu, 0);
    } catch (Exception ignored) {
    }
  }

  private void preserveCurrentPage() {
    if (paginated && menu instanceof PaginatedFastInv paginatedMenu) {
      try {
        Field pageField = PaginatedFastInv.class.getDeclaredField("page");
        pageField.setAccessible(true);
        currentPageIndex = (int) pageField.get(paginatedMenu);
      } catch (Exception ignored) {
        currentPageIndex = 0;
      }
    }
  }

  protected void restorePageAfterRefresh() {
    if (!paginated || !(menu instanceof PaginatedFastInv paginatedMenu)) return;
    if (currentPageIndex <= 0) return;
    
    for (int i = 0; i < currentPageIndex; i++) {
      paginatedMenu.openNext();
    }
  }

  public int getSize() {
    return menu.getInventory().getSize();
  }

  public void setStaticItem(MenuItemConfig config, ItemStack item, Player player) {
    int slot = config.getPrimarySlot();
    if (slot < 0 || slot >= getSize())
      return;
    menu.setItem(slot, item, e -> handleClick(player, config, e));
  }

  public void addDynamicItem(MenuItemConfig config, ItemStack item, Player player) {
    if (paginated && menu instanceof PaginatedFastInv paginatedMenu) {
      paginatedMenu.addContent(item, e -> handleClick(player, config, e));
    } else {
      int nextSlot = findNextAvailableContentSlot();
      if (nextSlot != -1) {
        menu.setItem(nextSlot, item, e -> handleClick(player, config, e));
      }
    }
  }

  private int findNextAvailableContentSlot() {
    if (contentSlots.isEmpty())
      return -1;
    for (int slot : contentSlots) {
      if (menu.getInventory().getItem(slot) == null) {
        return slot;
      }
    }
    return -1;
  }

  public void openMenu(Player player) {
    open(player);
  }

  public void fillFiller(MenuItemConfig filler) {
    if (filler == null || !filler.exists())
      return;
    ItemStack item = new ItemBuilder(filler.getMaterial())
        .name(filler.getName())
        .lore(filler.getLore())
        .build();
    for (int i = 0; i < getSize(); i++) {
      if (menu.getInventory().getItem(i) == null && (contentSlots.isEmpty() || !contentSlots.contains(i))) {
        menu.setItem(i, item, e -> e.setCancelled(true));
      }
    }
  }

  public Map<String, MenuItemConfig> getAllItemConfigs() {
    return loadItems();
  }

  protected void setStaticItemWithHandler(int slot, ItemStack item, java.util.function.Consumer<InventoryClickEvent> handler) {
    if (slot < 0 || slot >= getSize()) return;
    menu.setItem(slot, item, handler::accept);
  }

  protected void addDynamicItemWithHandler(ItemStack item, java.util.function.Consumer<InventoryClickEvent> handler) {
    if (paginated && menu instanceof PaginatedFastInv paginatedMenu) {
      paginatedMenu.addContent(item, handler::accept);
    } else {
      int nextSlot = findNextAvailableContentSlot();
      if (nextSlot != -1) {
        menu.setItem(nextSlot, item, handler::accept);
      }
    }
  }

  protected FastInv getMenu() {
    return menu;
  }

  protected void initializePagination() {
    if (!paginated || !(menu instanceof PaginatedFastInv paginatedMenu)) return;
    if (!contentSlots.isEmpty()) {
      paginatedMenu.setContentSlots(contentSlots);
    }
  }

  protected boolean isPaginated() {
    return paginated;
  }

  protected List<Integer> getContentSlots() {
    return contentSlots;
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
}
