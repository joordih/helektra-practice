package dev.voltic.helektra.plugin.model.match.menu;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
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

public class RankedMenu extends InjectableMenu {

  private final IKitService kitService;
  private final IProfileService profileService;

  @Inject
  public RankedMenu(
    MenuConfigHelper menuConfig,
    IKitService kitService,
    IProfileService profileService
  ) {
    super(menuConfig, "ranked-kits");
    this.kitService = kitService;
    this.profileService = profileService;
  }

  @Override
  public void setup(Player player) {
    List<IKit> kits = kitService.getAllKits();
    AtomicInteger slot = new AtomicInteger(0);

    profileService
      .getCachedProfile(player.getUniqueId())
      .ifPresent(profile -> {
        for (IKit kit : kits) {
          if (slot.get() >= getInventory().getSize()) break;

          Material material = XMaterial.matchXMaterial(
            kit.getIcon().getType()
          ).get();

          int rating = 1000;

          Map<String, String> placeholders = new HashMap<>();
          placeholders.put("kit_name", kit.getDisplayName());
          placeholders.put("in_queue", String.valueOf(kit.getQueue()));
          placeholders.put("playing", String.valueOf(kit.getPlaying()));
          placeholders.put("rating", String.valueOf(rating));

          List<String> lore = new ArrayList<>(kit.getDescription());
          lore.add("");
          lore.add(
            TranslationUtils.translate("queue.your-rating", "rating", rating)
          );
          lore.add(
            TranslationUtils.translate(
              "queue.in-queue",
              "count",
              kit.getQueue()
            )
          );
          lore.add(
            TranslationUtils.translate(
              "queue.playing",
              "count",
              kit.getPlaying()
            )
          );
          lore.add("");
          lore.add(TranslationUtils.translate("queue.click-to-join"));

          setItem(
            slot.incrementAndGet(),
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
                joinRankedQueue(p, kit);
              }
            }
          );
        }
      });
  }

  private void joinRankedQueue(Player player, IKit kit) {
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

          if (
            !profile.getSettings().getPingMatchmaking().enabled() ||
            (player.getPing() >=
                profile.getSettings().getPingMatchmaking().min() &&
              player.getPing() <=
              profile.getSettings().getPingMatchmaking().max())
          ) {
            profile.setProfileState(ProfileState.IN_QUEUE);
            kit.incrementQueue();
            player.sendMessage(
              TranslationUtils.translate(
                "queue.joined-ranked",
                "kit",
                kit.getDisplayName()
              )
            );
            player.closeInventory();
          } else {
            player.sendMessage(
              TranslationUtils.translate("queue.ping-restriction")
            );
            player.closeInventory();
          }
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
