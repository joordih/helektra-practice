package dev.voltic.helektra.plugin.model.match.menu.duel;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuelArenaMenu extends InjectableMenu {
    private final IArenaService arenaService;
    private final Helektra helektra;
    private final IKit selectedKit;
    private final UUID targetPlayer;

    public DuelArenaMenu(MenuConfigHelper menuConfig, IArenaService arenaService, Helektra helektra, 
                         IKit selectedKit, UUID targetPlayer) {
        super(menuConfig, "duel-arena");
        this.arenaService = arenaService;
        this.helektra = helektra;
        this.selectedKit = selectedKit;
        this.targetPlayer = targetPlayer;
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
            lore.add(TranslationUtils.translate("duel.arena-type", "type", arena.getType().name()));
            lore.add("");
            lore.add(TranslationUtils.translate("duel.select-arena"));
            
            setItem(slot++,
                new ItemBuilder(material)
                    .name(TranslationUtils.translate("duel.arena-name", "name", arena.getDisplayName()))
                    .lore(lore)
                    .build(),
                event -> {
                    if (event.getWhoClicked() instanceof Player p) {
                        openRoundMenu(p, arena);
                    }
                });
        }
    }

    private void openRoundMenu(Player player, Arena arena) {
        DuelRoundMenu roundMenu = new DuelRoundMenu(
            helektra.getInjector().getInstance(MenuConfigHelper.class),
            helektra.getInjector().getInstance(dev.voltic.helektra.api.model.match.IMatchService.class),
            selectedKit,
            arena,
            targetPlayer
        );
        roundMenu.setup(player);
        roundMenu.open(player);
    }
}
