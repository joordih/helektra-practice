package dev.voltic.helektra.plugin.model.match.menu;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class UnrankedMenu extends InjectableMenu {

  private final IKitService kitService;
  private final IProfileService profileService;
  private final ProfileStateManager profileStateManager;

  @Inject
  public UnrankedMenu(
    MenuConfigHelper menuConfig,
    IKitService kitService,
    IProfileService profileService,
    ProfileStateManager profileStateManager
  ) {
    super(menuConfig, "unranked-kits");
    this.kitService = kitService;
    this.profileService = profileService;
    this.profileStateManager = profileStateManager;
  }

  @Override
  public void setup(Player player) {
    List<IKit> kits = kitService.getAllKits();
    AtomicInteger slot = new AtomicInteger(0);

    for (IKit kit : kits) {
      if (slot.get() >= getInventory().getSize()) break;

      Material material = XMaterial.matchXMaterial(
        kit.getIcon().getType()
      ).get();

      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("kit_name", kit.getDisplayName());
      placeholders.put("in_queue", String.valueOf(kit.getQueue()));
      placeholders.put("playing", String.valueOf(kit.getPlaying()));

      List<String> lore = new ArrayList<>(kit.getDescription());
      lore.add("");
      lore.add(
        TranslationUtils.translate("queue.in-queue", "count", kit.getQueue())
      );
      lore.add(
        TranslationUtils.translate("queue.playing", "count", kit.getPlaying())
      );
      lore.add("");
      lore.add(TranslationUtils.translate("queue.click-to-join"));

      setItem(
        slot.getAndIncrement(),
        new ItemBuilder(material)
          .name(
            TranslationUtils.translate(
              "queue.kit-name",
              "name",
              kit.getDisplayName()
            )
          )
          .lore(lore)
          .build(),
        event -> {
          if (event.getWhoClicked() instanceof Player p) {
            joinUnrankedQueue(p, kit);
          }
        }
      );
    }
  }

  private void joinUnrankedQueue(Player player, IKit kit) {
    profileService
      .getCachedProfile(player.getUniqueId())
      .ifPresentOrElse(
        profile -> {
          if (profile.getProfileState() != ProfileState.LOBBY) {
            player.sendMessage(
              TranslationUtils.translate("queue.already-in-queue")
            );
            player.closeInventory();
            return;
          }

          profileStateManager.setState(player, ProfileState.IN_QUEUE);
          kit.incrementQueue();

          player.sendMessage(
            TranslationUtils.translate(
              "queue.joined",
              "kit",
              kit.getDisplayName()
            )
          );
          player.closeInventory();
        },
        () -> {
          player.sendMessage(
            TranslationUtils.translate("error.profile-not-loaded")
          );
          player.closeInventory();
        }
      );
  }
}
