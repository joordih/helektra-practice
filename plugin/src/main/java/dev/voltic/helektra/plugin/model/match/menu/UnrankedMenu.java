package dev.voltic.helektra.plugin.model.match.menu;

import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper.MenuItemConfig;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnrankedMenu extends InjectableMenu {
    private final Helektra helektra;

    @Inject
    public UnrankedMenu(MenuConfigHelper menuConfig, Helektra helektra) {
        super(menuConfig, "unranked");
        this.helektra = helektra;
    }

    @Override
    public void setup(Player player) {
        setupMatchTypeItems();
    }

    private void setupMatchTypeItems() {
        List<String> itemKeys = menuConfig.getItemKeys(menuPath);
        
        for (String itemKey : itemKeys) {
            MenuItemConfig itemConfig = menuConfig.getItemConfig(menuPath, itemKey);
            if (!itemConfig.exists()) {
                continue;
            }

            MatchType matchType = parseMatchType(itemKey);
            if (matchType == null) {
                continue;
            }

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("type", matchType.getDisplayName());
            placeholders.put("min_players", String.valueOf(matchType.getMinParticipants()));
            placeholders.put("max_players", String.valueOf(matchType.getMaxParticipants()));

            setItem(itemConfig.getPosition(), 
                new ItemBuilder(itemConfig.getMaterial())
                    .name(itemConfig.getName())
                    .lore(itemConfig.getLore(placeholders))
                    .build(),
                event -> {
                    if (event.getWhoClicked() instanceof Player player) {
                        helektra.getMenuFactory().openMenu(KitSelectionMenu.class, player);
                    }
                });
        }
    }

    private MatchType parseMatchType(String itemKey) {
        return switch (itemKey.toUpperCase()) {
            case "DUEL" -> MatchType.DUEL;
            case "QUEUE" -> MatchType.QUEUE;
            case "FFA" -> MatchType.FFA;
            case "RANGE_ROVER" -> MatchType.RANGE_ROVER;
            case "PARTY_FFA" -> MatchType.PARTY_FFA;
            default -> null;
        };
    }
}
