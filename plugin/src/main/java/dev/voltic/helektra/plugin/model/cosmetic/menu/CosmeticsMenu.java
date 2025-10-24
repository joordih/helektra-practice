package dev.voltic.helektra.plugin.model.cosmetic.menu;

import dev.voltic.helektra.api.model.cosmetic.CosmeticType;
import dev.voltic.helektra.api.model.cosmetic.ICosmetic;
import dev.voltic.helektra.api.model.cosmetic.ICosmeticCategory;
import dev.voltic.helektra.api.model.cosmetic.ICosmeticService;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.DynamicMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CosmeticsMenu extends DynamicMenu {

  private static final String MENU_PATH = "cosmetics";
  private static final String KEY_FILLER = "filler";
  private static final String KEY_BACK = "back";

  private final Helektra plugin;
  private final ICosmeticService cosmeticService;

  @Inject
  public CosmeticsMenu(MenuConfigHelper configHelper, Helektra plugin, ICosmeticService cosmeticService) {
    super(configHelper, MENU_PATH);
    this.plugin = plugin;
    this.cosmeticService = cosmeticService;
  }

  @Override
  public void setup(Player player) {
    Optional<IProfile> optionalProfile = plugin.getProfileService().getProfile(player.getUniqueId()).join();
    if (optionalProfile.isEmpty()) {
      player.sendMessage(TranslationUtils.translate("profile.error"));
      return;
    }
    render(player, optionalProfile.get());
  }

  private void render(Player player, IProfile profile) {
    clear();
    initializePagination();

    Map<String, MenuItemConfig> itemConfigs = getAllItemConfigs();
    List<ICosmeticCategory> categories = cosmeticService.getAllCategories();

    for (ICosmeticCategory category : categories) {
      ItemStack categoryItem = buildCategoryItem(profile, category);
      addDynamicItemWithHandler(categoryItem, e -> handleCategoryClick(player, profile, category, e));
    }

    setupBackButton(itemConfigs, e -> {
      e.setCancelled(true);
      player.closeInventory();
    });
    fillFiller(itemConfigs.get(KEY_FILLER));
    completeRefresh();
    setupPaginationItems(player);
  }

  private void setupBackButton(Map<String, MenuItemConfig> itemConfigs, Consumer<InventoryClickEvent> handler) {
    MenuItemConfig backConfig = itemConfigs.get(KEY_BACK);
    if (backConfig != null && backConfig.exists()) {
      ItemStack backItem = buildStatic(backConfig);
      setStaticItemWithHandler(backConfig.getPrimarySlot(), backItem, handler);
    }
  }


  private ItemStack buildCategoryItem(IProfile profile, ICosmeticCategory category) {
    Optional<XMaterial> materialOpt = XMaterial.matchXMaterial(category.getIconMaterial());
    ItemStack item = materialOpt.map(xMaterial -> new ItemBuilder(xMaterial.get()).build())
      .orElse(new ItemBuilder(XMaterial.CHEST.get()).build());

    String selectedCosmetic = profile.getCosmetics().getSelectedCosmeticId(category.getType());
    String statusText = selectedCosmetic != null
      ? cosmeticService.getCosmeticById(selectedCosmetic).map(ICosmetic::getDisplayName).orElse("&7None")
      : "&7None";

    List<String> lore = new ArrayList<>();
    lore.add(ColorUtils.translate("&7Select your " + category.getDisplayName()));
    lore.add("");
    lore.add(ColorUtils.translate("&eCurrent: " + statusText));
    lore.add("");
    lore.add(ColorUtils.translate("&eClick to browse!"));

    return new ItemBuilder(item).name(ColorUtils.translate(category.getDisplayName())).lore(lore).build();
  }

  private void handleCategoryClick(Player player, IProfile profile, ICosmeticCategory category, InventoryClickEvent event) {
    event.setCancelled(true);
    openCategoryMenu(player, profile, category);
  }

  private void openCategoryMenu(Player player, IProfile profile, ICosmeticCategory category) {
    clear();
    initializePagination();

    Map<String, MenuItemConfig> itemConfigs = getAllItemConfigs();
    List<ICosmetic> cosmetics = category.getCosmetics();

    for (ICosmetic cosmetic : cosmetics) {
      if (!cosmetic.isEnabled()) continue;
      ItemStack cosmeticItem = buildCosmeticItem(profile, cosmetic, category.getType());
      addDynamicItemWithHandler(cosmeticItem, e -> handleCosmeticClick(player, profile, cosmetic, e));
    }

    ItemStack noneItem = buildNoneItem(profile, category.getType());
    addDynamicItemWithHandler(noneItem, e -> handleNoneClick(player, profile, category.getType(), e));

    setupBackButton(itemConfigs, e -> {
      e.setCancelled(true);
      render(player, profile);
    });
    fillFiller(itemConfigs.get(KEY_FILLER));
    completeRefresh();
    setupPaginationItems(player);
  }


  private ItemStack buildCosmeticItem(IProfile profile, ICosmetic cosmetic, CosmeticType type) {
    Optional<XMaterial> materialOpt = XMaterial.matchXMaterial(cosmetic.getIconMaterial());
    ItemStack item = materialOpt.map(xMaterial -> new ItemBuilder(xMaterial.get()).build())
      .orElse(new ItemBuilder(XMaterial.PAPER.get()).build());

    boolean isSelected = cosmetic.getId().equals(profile.getCosmetics().getSelectedCosmeticId(type));

    List<String> lore = new ArrayList<>();
    lore.add("");
    if (isSelected) {
      lore.add(ColorUtils.translate("&a&l✔ Currently Selected"));
    } else {
      lore.add(ColorUtils.translate("&eClick to select!"));
    }

    return new ItemBuilder(item).name(ColorUtils.translate(cosmetic.getDisplayName())).lore(lore).build();
  }

  private ItemStack buildNoneItem(IProfile profile, CosmeticType type) {
    boolean isSelected = !profile.getCosmetics().hasSelectedCosmetic(type);

    List<String> lore = new ArrayList<>();
    lore.add(ColorUtils.translate("&7Disable cosmetic effects"));
    lore.add("");
    if (isSelected) {
      lore.add(ColorUtils.translate("&a&l✔ Currently Selected"));
    } else {
      lore.add(ColorUtils.translate("&eClick to select!"));
    }

    return new ItemBuilder(XMaterial.BARRIER.get())
      .name(ColorUtils.translate("&c&lNone"))
      .lore(lore)
      .build();
  }

  private void handleCosmeticClick(Player player, IProfile profile, ICosmetic cosmetic, InventoryClickEvent event) {
    event.setCancelled(true);
    profile.getCosmetics().setSelectedCosmetic(cosmetic.getType(), cosmetic.getId());
    plugin.getProfileService().saveProfile(profile);
    player.sendMessage(TranslationUtils.translate("cosmetics.selected", "cosmetic", cosmetic.getDisplayName()));
    openCategoryMenu(player, profile, cosmeticService.getCategoryByType(cosmetic.getType()).orElseThrow());
  }

  private void handleNoneClick(Player player, IProfile profile, CosmeticType type, InventoryClickEvent event) {
    event.setCancelled(true);
    profile.getCosmetics().clearSelection(type);
    plugin.getProfileService().saveProfile(profile);
    player.sendMessage(TranslationUtils.translate("cosmetics.disabled"));
    openCategoryMenu(player, profile, cosmeticService.getCategoryByType(type).orElseThrow());
  }

  private ItemStack buildStatic(MenuItemConfig itemConfig) {
    return new ItemBuilder(itemConfig.getMaterial()).name(itemConfig.getName()).lore(itemConfig.getLore()).build();
  }
}
