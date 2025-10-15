package dev.voltic.helektra.plugin.model.match.menu;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitSelectionMenu extends InjectableMenu {
    private final Helektra helektra;
    private Player currentPlayer;

    @Inject
    public KitSelectionMenu(MenuConfigHelper menuConfig, Helektra helektra) {
        super(menuConfig, "kit-selection");
        this.helektra = helektra;
    }

    @Override
    public void setup(Player player) {
        this.currentPlayer = player;
        setupKitItems();
    }

    private void setupKitItems() {
        List<IKit> kits = helektra.getAPI().getKitService().getAllKits();
        int position = 0;

        for (IKit kit : kits) {
            if (position >= getInventory().getSize()) {
                break;
            }

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("kit_name", kit.getName());
            placeholders.put("display_name", kit.getDisplayName());
            placeholders.put("queue", String.valueOf(kit.getQueue()));
            placeholders.put("playing", String.valueOf(kit.getPlaying()));

            setItem(position,
                new ItemBuilder(org.bukkit.Material.DIAMOND_SWORD)
                    .name(kit.getDisplayName())
                    .lore(kit.getDescription())
                    .build(),
                event -> {
                    currentPlayer.sendMessage(TranslationUtils.translate("match.kit-selected", "kit", kit.getName()));
                });

            position++;
        }
    }
}
