package dev.voltic.helektra.plugin.model.match.menu;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;

public class MatchModeMenu extends InjectableMenu {
    private final Helektra helektra;

    @Inject
    public MatchModeMenu(MenuConfigHelper menuConfig, Helektra helektra) {
        super(menuConfig, "match-mode");
        this.helektra = helektra;
    }

    @Override
    public void setup(Player player) {
        setupModeItems();
    }

    private void setupModeItems() {
        MenuItemConfig unrankedItem = menuConfig.getItemConfig(menuPath, "unranked");
        if (unrankedItem.exists()) {
            setItem(unrankedItem.getPosition(),
                new ItemBuilder(unrankedItem.getMaterial())
                    .name(unrankedItem.getName())
                    .lore(unrankedItem.getLore())
                    .build(),
                event -> {
                    if (event.getWhoClicked() instanceof Player player) {
                        helektra.getMenuFactory().openMenu(UnrankedMenu.class, player);
                    }
                });
        }

        MenuItemConfig rankedItem = menuConfig.getItemConfig(menuPath, "ranked");
        if (rankedItem.exists()) {
            setItem(rankedItem.getPosition(),
                new ItemBuilder(rankedItem.getMaterial())
                    .name(rankedItem.getName())
                    .lore(rankedItem.getLore())
                    .build(),
                event -> {
                    if (event.getWhoClicked() instanceof Player player) {
                        helektra.getMenuFactory().openMenu(RankedMenu.class, player);
                    }
                });
        }
    }
}
