package dev.voltic.helektra.plugin.model.match.menu.ffa;

import org.bukkit.entity.Player;

import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;

public class FFAMainMenu extends InjectableMenu {
    private final Helektra helektra;

    @Inject
    public FFAMainMenu(MenuConfigHelper menuConfig, Helektra helektra) {
        super(menuConfig, "ffa");
        this.helektra = helektra;
    }

    @Override
    public void setup(Player player) {
        boolean inParty = false;
        
        MenuItemConfig ffaItem = menuConfig.getItemConfig(menuPath, "ffa");
        if (ffaItem.exists()) {
            setItem(ffaItem.getPosition(),
                new ItemBuilder(ffaItem.getMaterial())
                    .name(ffaItem.getName())
                    .lore(ffaItem.getLore())
                    .build(),
                event -> {
                    if (event.getWhoClicked() instanceof Player p) {
                        openFFAKitMenu(p, FFAType.FFA);
                    }
                });
        }
        
        if (inParty) {
            MenuItemConfig partyFFAItem = menuConfig.getItemConfig(menuPath, "party_ffa");
            if (partyFFAItem.exists()) {
                setItem(partyFFAItem.getPosition(),
                    new ItemBuilder(partyFFAItem.getMaterial())
                        .name(partyFFAItem.getName())
                        .lore(partyFFAItem.getLore())
                        .build(),
                    event -> {
                        if (event.getWhoClicked() instanceof Player p) {
                            openFFAKitMenu(p, FFAType.PARTY_FFA);
                        }
                    });
            }
            
            MenuItemConfig rangeRoverItem = menuConfig.getItemConfig(menuPath, "range_rover");
            if (rangeRoverItem.exists()) {
                setItem(rangeRoverItem.getPosition(),
                    new ItemBuilder(rangeRoverItem.getMaterial())
                        .name(rangeRoverItem.getName())
                        .lore(rangeRoverItem.getLore())
                        .build(),
                    event -> {
                        if (event.getWhoClicked() instanceof Player p) {
                            openFFAKitMenu(p, FFAType.RANGE_ROVER);
                        }
                    });
            }
        }
    }

    private void openFFAKitMenu(Player player, FFAType type) {
        FFAKitMenu kitMenu = new FFAKitMenu(
            helektra.getInjector().getInstance(MenuConfigHelper.class),
            helektra.getInjector().getInstance(IKitService.class),
            helektra,
            type
        );
        kitMenu.setup(player);
        kitMenu.open(player);
    }

    public enum FFAType {
        FFA,
        PARTY_FFA,
        RANGE_ROVER
    }
}
