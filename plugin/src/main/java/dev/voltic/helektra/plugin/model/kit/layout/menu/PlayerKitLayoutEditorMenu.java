package dev.voltic.helektra.plugin.model.kit.layout.menu;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.kit.IPlayerKitLayout;
import dev.voltic.helektra.api.model.kit.IPlayerKitLayoutService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.kit.Kit;
import dev.voltic.helektra.plugin.model.kit.serialization.InventorySerializer;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import dev.voltic.helektra.plugin.utils.menu.MenuFactory;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerKitLayoutEditorMenu extends InjectableMenu {

  private final IKitService kitService;
  private final IPlayerKitLayoutService layoutService;
  private final ProfileStateManager stateManager;
  private final MenuConfigHelper menuConfig;
  private final MenuFactory menuFactory;
  private final JavaPlugin plugin;

  private String kitName;
  private ItemStack[] originalLayout;
  private ItemStack[] playerBackupInventory;
  private ItemStack[] playerBackupArmor;
  private boolean alreadyClosed = false;

  @Inject
  public PlayerKitLayoutEditorMenu(
    MenuConfigHelper menuConfig,
    IKitService kitService,
    IPlayerKitLayoutService layoutService,
    ProfileStateManager stateManager,
    MenuFactory menuFactory,
    JavaPlugin plugin
  ) {
    super(menuConfig, "kit-layout-editor");
    this.menuConfig = menuConfig;
    this.kitService = kitService;
    this.layoutService = layoutService;
    this.stateManager = stateManager;
    this.menuFactory = menuFactory;
    this.plugin = plugin;
  }

  public void setupForKit(
    Player player,
    String kitName,
    ProfileState previousState
  ) {
    this.kitName = kitName;
    this.alreadyClosed = false;

    Optional<IKit> kitOpt = kitService.getKit(kitName);
    if (kitOpt.isEmpty()) {
      player.sendMessage(
        TranslationUtils.translate("kit.not-found", "kit", kitName)
      );
      return;
    }

    IKit kit = kitOpt.get();

    Optional<IPlayerKitLayout> layoutOpt = layoutService
      .getLayout(player.getUniqueId(), kitName)
      .join();

    if (layoutOpt.isPresent() && layoutOpt.get().hasCustomLayout()) {
      this.originalLayout = layoutOpt.get().getLayoutContents();
    } else {
      if (kit instanceof Kit pluginKit) {
        this.originalLayout = InventorySerializer.deserialize(
          pluginKit.getInventory(),
          kitName
        );
      } else {
        this.originalLayout = new ItemStack[36];
      }
    }

    backupPlayerInventory(player);
    applyKitToPlayerInventory(player);
    setup(player);
    open(player);
  }

  @Override
  public void setup(Player player) {
    setupActionButtons(player);
    setupFillers();
  }

  private void setupActionButtons(Player player) {
    MenuConfigHelper.MenuItemConfig saveButton = menuConfig.getItemConfig(
      menuPath,
      "save"
    );
    if (saveButton.exists()) {
      setItem(
        saveButton.getPosition(),
        new ItemBuilder(saveButton.getMaterial())
          .name(saveButton.getName())
          .lore(saveButton.getLore())
          .build(),
        event -> handleSave(player)
      );
    }

    MenuConfigHelper.MenuItemConfig backButton = menuConfig.getItemConfig(
      menuPath,
      "back"
    );
    if (backButton.exists()) {
      setItem(
        backButton.getPosition(),
        new ItemBuilder(backButton.getMaterial())
          .name(backButton.getName())
          .lore(backButton.getLore())
          .build(),
        event -> handleBack(player)
      );
    }

    MenuConfigHelper.MenuItemConfig resetButton = menuConfig.getItemConfig(
      menuPath,
      "reset"
    );
    if (resetButton.exists()) {
      setItem(
        resetButton.getPosition(),
        new ItemBuilder(resetButton.getMaterial())
          .name(resetButton.getName())
          .lore(resetButton.getLore())
          .build(),
        event -> handleReset(player)
      );
    }
  }

  private void backupPlayerInventory(Player player) {
    this.playerBackupInventory = player.getInventory().getContents().clone();
    this.playerBackupArmor = player.getInventory().getArmorContents().clone();
  }

  private void applyKitToPlayerInventory(Player player) {
    player.getInventory().clear();
    player.getInventory().setArmorContents(new ItemStack[4]);

    if (originalLayout != null) {
      for (int i = 0; i < Math.min(originalLayout.length, 36); i++) {
        ItemStack item = originalLayout[i];
        if (item != null && item.getType() != Material.AIR) {
          player.getInventory().setItem(i, item.clone());
        }
      }
    }

    player.updateInventory();
  }

  private void setupFillers() {
    MenuConfigHelper.MenuItemConfig fillerConfig = menuConfig.getItemConfig(
      menuPath,
      "filler"
    );
    if (!fillerConfig.exists()) {
      return;
    }

    ItemStack filler = new ItemBuilder(fillerConfig.getMaterial())
      .name(fillerConfig.getName())
      .build();

    for (int slot = 0; slot < this.getInventory().getSize(); slot++) {
      if (this.getInventory().getItem(slot) == null) {
        setItem(slot, filler, event -> event.setCancelled(true));
      }
    }
  }

  private void handleSave(Player player) {
    ItemStack[] newLayout = extractLayoutFromPlayerInventory(player);
    layoutService
      .saveLayout(player.getUniqueId(), kitName, newLayout)
      .thenRun(() -> {
        plugin
          .getServer()
          .getScheduler()
          .runTask(plugin, () -> {
            String message = menuConfig.getString(
              menuPath + ".messages.saved",
              "&aYour layout has been saved!"
            );
            player.sendMessage(TranslationUtils.translate(message));
            restorePlayerInventoryAndReturnToSelector(player);
          });
      });
  }

  private void handleReset(Player player) {
    layoutService
      .deleteLayout(player.getUniqueId(), kitName)
      .thenRun(() -> {
        plugin
          .getServer()
          .getScheduler()
          .runTask(plugin, () -> {
            String message = menuConfig.getString(
              menuPath + ".messages.reset",
              "&eLayout restored to default."
            );
            player.sendMessage(TranslationUtils.translate(message));
            restorePlayerInventoryAndReturnToSelector(player);
          });
      });
  }

  private void handleBack(Player player) {
    String message = menuConfig.getString(
      menuPath + ".messages.back",
      "&7Returning to kit selector..."
    );
    player.sendMessage(TranslationUtils.translate(message));
    restorePlayerInventoryAndReturnToSelector(player);
  }

  private void restorePlayerInventoryAndReturnToSelector(Player player) {
    if (alreadyClosed) {
      return;
    }
    alreadyClosed = true;

    player.closeInventory();
    restorePlayerInventory(player);
    stateManager.setState(player, ProfileState.LOBBY);

    plugin
      .getServer()
      .getScheduler()
      .runTask(plugin, () -> {
        menuFactory.openMenu(KitLayoutSelectorMenu.class, player);
      });
  }

  private void restorePlayerInventory(Player player) {
    if (playerBackupInventory != null) {
      player.getInventory().setContents(playerBackupInventory);
    }
    if (playerBackupArmor != null) {
      player.getInventory().setArmorContents(playerBackupArmor);
    }
    player.updateInventory();
  }

  private ItemStack[] extractLayoutFromPlayerInventory(Player player) {
    ItemStack[] playerInv = player.getInventory().getContents();
    ItemStack[] layout = new ItemStack[36];

    for (int i = 0; i < Math.min(playerInv.length, 36); i++) {
      ItemStack item = playerInv[i];
      if (item != null && item.getType() != Material.AIR) {
        layout[i] = item.clone();
      } else {
        layout[i] = null;
      }
    }

    return layout;
  }

  @Override
  protected void onClick(InventoryClickEvent event) {
    if (event.getClickedInventory() == null) {
      event.setCancelled(true);
      return;
    }

    if (event.getClickedInventory().equals(event.getView().getTopInventory())) {
      super.onClick(event);
    } else {
      event.setCancelled(false);
    }
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    super.onClose(event);
    Player player = (Player) event.getPlayer();

    if (alreadyClosed) {
      return;
    }
    alreadyClosed = true;

    restorePlayerInventory(player);
    stateManager.setState(player, ProfileState.LOBBY);
  }
}
