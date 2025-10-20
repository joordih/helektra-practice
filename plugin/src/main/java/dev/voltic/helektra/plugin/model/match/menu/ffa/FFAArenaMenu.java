package dev.voltic.helektra.plugin.model.match.menu.ffa;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.plugin.model.match.menu.ffa.FFAMainMenu.FFAType;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FFAArenaMenu extends InjectableMenu {
    private final IArenaService arenaService;
    private final IMatchService matchService;
    private final IKit selectedKit;
    private final FFAType ffaType;

    public FFAArenaMenu(MenuConfigHelper menuConfig, IArenaService arenaService, IMatchService matchService,
                        IKit selectedKit, FFAType ffaType) {
        super(menuConfig, "ffa-arena");
        this.arenaService = arenaService;
        this.matchService = matchService;
        this.selectedKit = selectedKit;
        this.ffaType = ffaType;
    }

    @Override
    public void setup(Player player) {
        List<Arena> arenas = arenaService.getArenasByKit(selectedKit.getName());
        int slot = 0;
        
        for (Arena arena : arenas) {
            if (slot >= getInventory().getSize()) break;
            
            Material material = XMaterial.matchXMaterial(arena.getType().name() + "_WOOL")
                .orElse(XMaterial.WHITE_WOOL)
                .get();
            
            List<String> lore = new ArrayList<>();
            lore.add(TranslationUtils.translate("ffa.arena-type", "type", arena.getType().name()));
            lore.add("");
            lore.add(TranslationUtils.translate("ffa.click-to-start"));
            
            setItem(slot++,
                new ItemBuilder(material)
                    .name(TranslationUtils.translate("ffa.arena-name", "name", arena.getDisplayName()))
                    .lore(lore)
                    .build(),
                event -> {
                    if (event.getWhoClicked() instanceof Player p) {
                        startFFA(p, arena);
                    }
                });
        }
    }

    private void startFFA(Player player, Arena arena) {
        MatchType matchType = switch (ffaType) {
            case PARTY_FFA -> MatchType.PARTY_FFA;
            case RANGE_ROVER -> MatchType.RANGE_ROVER;
            default -> MatchType.FFA;
        };
        
        matchService.createMatch(matchType, arena, selectedKit, Arrays.asList(player.getUniqueId()));
        player.sendMessage(TranslationUtils.translate("ffa.started", 
            "type", matchType.getDisplayName(),
            "kit", selectedKit.getDisplayName(),
            "arena", arena.getDisplayName()));
        player.closeInventory();
    }
}
